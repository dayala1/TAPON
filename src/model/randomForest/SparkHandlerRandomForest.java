package model.randomForest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.ml.classification.MultilayerPerceptronClassificationModel;
import org.apache.spark.ml.classification.MultilayerPerceptronClassifier;
import org.apache.spark.ml.classification.RandomForestClassificationModel;
import org.apache.spark.ml.classification.RandomForestClassifier;
import org.apache.spark.ml.linalg.Vector;
import org.apache.spark.ml.linalg.VectorUDT;
import org.apache.spark.ml.linalg.Vectors;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import dataset.Attribute;
import dataset.Record;
import dataset.Slot;
import featuresCalculation.FeaturesVector;
import jersey.repackaged.com.google.common.collect.Lists;
import utils.FileUtils;

public class SparkHandlerRandomForest implements Serializable{
	private static final long serialVersionUID = 4424575592980018563L;

	//Constructors---------------------------------------------------

	//Properties-----------------------------------------------------
	private String tablesFolder;
	private String classifiersFolder;
	private String classesFilePath;
	private Integer numberOfFeatures;
	
	public String getTablesFolder() {
		return tablesFolder;
	}

	public void setTablesFolder(String trainingTablesFolder) {
		assert trainingTablesFolder != null;
	
		this.tablesFolder = trainingTablesFolder;
	}

	public String getClassifiersFolder() {
		return classifiersFolder;
	}

	public void setClassifiersFolder(String classifiersFolder) {
		assert classifiersFolder != null;
	
		this.classifiersFolder = classifiersFolder;
	}
	
	public String getClassesFilePath() {
		return classesFilePath;
	}

	public void setClassesFilePath(String classesFilePath) {
		assert classesFilePath != null;
	
		this.classesFilePath = classesFilePath;
	}
	
	public Integer getNumberOfFeatures() {
		return numberOfFeatures;
	}

	public void setNumberOfFeatures(Integer numberOfFeatures) {
		this.numberOfFeatures = numberOfFeatures;
	}

	//Internal state-------------------------------------------------

	private final static Level loggerLevel = Level.ERROR;
	//all these measures have default values, but they can be changed.
	//"gini" or "entropy"
	private final static String impurity = "gini";
	//no defaule
	private final static Integer maxDepth = 40;
	//default = 32
	private final static Integer maxBins = 32;
	//So far, no justification anywhere...
	private final static Double minInfoGain = 0.01;
	//Helps with deep trees
	private final static Boolean useNodeIdCache = true;
	//More trees +quality and +time
	private final Integer numTrees = 100;
	//Here start the parameters for the multiclass classifier
	private final Integer numIterations = 500;
	private final int[] intermediaryLayers = {};
	private static JavaSparkContext context;
	private static SparkContext sparkContext;
	private static SparkSession sparkSession;
	private List<ClassifierRandomForest> attributeClassifiersHintFree;
	private List<ClassifierRandomForest> recordClassifiersHintFree;
	private List<ClassifierRandomForest> attributeClassifiersHintBased;
	private List<ClassifierRandomForest> recordClassifiersHintBased;
	private MultilayerPerceptronClassificationModel attributeMulticlassClassifierHintFree;
	private MultilayerPerceptronClassificationModel recordMulticlassClassifierHintFree;
	private MultilayerPerceptronClassificationModel attributeMulticlassClassifierHintBased;
	private MultilayerPerceptronClassificationModel recordMulticlassClassifierHintBased;
	
	static {
		Logger.getLogger("org").setLevel(loggerLevel);
		Logger.getLogger("akka").setLevel(loggerLevel);
	}
	
	
	//Interface methods----------------------------------------------
	
	public void closeContext() {
		context.close();
	}
	
	public void loadClassifiers(boolean hints) {
		assert classifiersFolder != null;
		
		File classifiersFolderFile;
		File[] classifiersFiles;
		RandomForestClassificationModel cm;
		String classifierPath;
		ClassifierRandomForest model;
		
		if (!hints) {
			this.attributeClassifiersHintFree = Lists.newArrayList();
			this.recordClassifiersHintFree = Lists.newArrayList();
			
			classifiersFolderFile = new File(String.format("%s/classifiersNoHint/attributes", classifiersFolder));
	        classifiersFiles = classifiersFolderFile.listFiles();
	        for (File file : classifiersFiles) {
	     	   cm = RandomForestClassificationModel.load(file.getAbsolutePath());
	     	   model = new ClassifierRandomForest();
	     	   model.setModel(cm);
	     	   model.setName(file.getName());
	    	   attributeClassifiersHintFree.add(model);
	        }
	        
	        classifiersFolderFile = new File(String.format("%s/classifiersNoHint/records", classifiersFolder));
	        classifiersFiles = classifiersFolderFile.listFiles();
	        for (File file : classifiersFiles) {
	        	cm = RandomForestClassificationModel.load(file.getAbsolutePath());
	        	model = new ClassifierRandomForest();
	        	model.setModel(cm);
	        	model.setName(file.getName());
	        	recordClassifiersHintFree.add(model);
	        }
	        
	        classifierPath =  String.format("%s/classifiersNoHint/attributes_multiclass", classifiersFolder);
	        attributeMulticlassClassifierHintFree = MultilayerPerceptronClassificationModel.load(classifierPath);
	        classifierPath =  String.format("%s/classifiersNoHint/records_multiclass", classifiersFolder);
	        if(recordClassifiersHintFree.size()>0){
	        	recordMulticlassClassifierHintFree = MultilayerPerceptronClassificationModel.load(classifierPath);
	        }
		} else {
			this.attributeClassifiersHintBased = Lists.newArrayList();
			this.recordClassifiersHintBased = Lists.newArrayList();
			
			classifiersFolderFile = new File(String.format("%s/classifiersHint/attributes", classifiersFolder));
	        classifiersFiles = classifiersFolderFile.listFiles();
	        for (File file : classifiersFiles) {
	     	   cm = RandomForestClassificationModel.load(file.getAbsolutePath());
	     	   model = new ClassifierRandomForest();
	     	   model.setModel(cm);
	     	   model.setName(file.getName());
	    	   attributeClassifiersHintBased.add(model);
	        }
	        
	        classifiersFolderFile = new File(String.format("%s/classifiersHint/records", classifiersFolder));
	        classifiersFiles = classifiersFolderFile.listFiles();
	        for (File file : classifiersFiles) {
	        	cm = RandomForestClassificationModel.load(file.getAbsolutePath());
	        	model = new ClassifierRandomForest();
	        	model.setModel(cm);
	        	model.setName(file.getName());
	        	recordClassifiersHintBased.add(model);
	        }
	        
	        classifierPath =  String.format("%s/classifiersHint/attributes_multiclass", classifiersFolder);
	        attributeMulticlassClassifierHintBased = MultilayerPerceptronClassificationModel.load(classifierPath);
	        classifierPath =  String.format("%s/classifiersHint/records_multiclass", classifiersFolder);
	        if(recordClassifiersHintBased.size()>0){
	        	recordMulticlassClassifierHintBased = MultilayerPerceptronClassificationModel.load(classifierPath);
	        }
		}
	}
	
	public void createNewContext() {
		SparkConf sparkConf;
		sparkConf = new SparkConf();
		sparkConf.setAppName("SparkHandler");
		sparkConf.setMaster(sparkConf.get("master", "local[*]"));
		sparkConf.set("spark.network.timeout", "99999999");
		context = new JavaSparkContext(sparkConf);
		sparkContext = context.sc();
		sparkSession = new SparkSession(sparkContext);
	}
	
	public Pair<String, LinkedHashMap<String, Double>> classify(Slot slot, boolean useProbabilities, boolean hints) throws IOException, ParseException {
		assert slot != null;
		assert this.classifiersFolder != null;
		
		Pair<String, LinkedHashMap<String, Double>> result;
		String predictedClass;
		MultilayerPerceptronClassificationModel model;
		List<String> attributeClasses;
		List<String> recordClasses;
		Map<Double, String> classesMapping;
		String slotClass;
		LinkedHashMap<String, Double> classificationsMap;
		LinkedHashMap<String, Double> ranking;
		Double[] classificationsTemp;
		double[] classifications;
		Vector vector;
		double prediction;
		
		attributeClasses = new ArrayList<String>();
		recordClasses = new ArrayList<String>();
		
		addClasses(attributeClasses, recordClasses);
		
		classesMapping = new HashMap<Double, String>();
		if (slot instanceof Attribute) {
			for (int i = 0; i < attributeClasses.size(); i++) {
				slotClass = attributeClasses.get(i);
				classesMapping.put((double)i, slotClass);
			}
			if (hints) {
				model = attributeMulticlassClassifierHintBased;
			} else {
				model = attributeMulticlassClassifierHintFree;
			}
		} else {
			for (int i = 0; i < recordClasses.size(); i++) {
				slotClass = recordClasses.get(i);
				classesMapping.put((double)i, slotClass);
			}
			if (hints) {
				model = recordMulticlassClassifierHintBased;
			} else {
				model = recordMulticlassClassifierHintFree;
			}
		}
		
		classificationsMap = applyClassifiersMap(slot, useProbabilities, hints);
		classificationsTemp = classificationsMap.values().toArray(new Double[0]);
		
		classifications = new double[classificationsTemp.length];
		int i = 0;
		for(Double d : classificationsTemp) {
		  classifications[i] = (double) d;
		  i++;
		}
		
		ranking = new LinkedHashMap<>();
		classificationsMap.entrySet()
			.stream()
			.sorted(Entry.<String, Double>comparingByValue().reversed())
			.forEachOrdered(x -> ranking.put(x.getKey(), x.getValue()));
		
		vector = Vectors.dense(classifications);
		prediction = model.predict(vector);
		
		predictedClass = classesMapping.get(prediction);
		
		result = Pair.of(predictedClass, ranking);
		
		return result;
	}
	
	public LinkedHashMap<String, Double> classifyWithProbabilities(Slot slot, int numberOfPredictions, boolean hints) throws IOException, ParseException {
		assert slot != null;
		assert this.classifiersFolder != null;

		LinkedHashMap<String, Double> result;
		Map<String, Double> probabilities;
		List<String> attributeClasses;
		List<String> recordClasses;
		String slotClass;
		double[] classifications;
		Vector vector;
		double prediction;
		
		attributeClasses = new ArrayList<String>();
		recordClasses = new ArrayList<String>();
		result = new LinkedHashMap<String, Double>();
		
		addClasses(attributeClasses, recordClasses);
		
		probabilities = applyClassifiersMap(slot, true, hints);
		probabilities.entrySet()
				.stream()
				.sorted(Entry.<String, Double>comparingByValue().reversed())
				.limit(numberOfPredictions)
				.forEachOrdered(x -> result.put(x.getKey(), x.getValue()));
		
		return result;
	}
	
	public void createBinaryClassifiers(boolean hints) throws IOException, ParseException {
		assert this.tablesFolder != null;
		assert this.classifiersFolder != null;
		
		File outFile;
		File tableFile;
		String firstLine;
		RandomForestClassificationModel model;
		List<String> recordClasses;
		List<String> attributeClasses;
		
		attributeClasses = new ArrayList<String>();
		recordClasses = new ArrayList<String>();
		
		addClasses(attributeClasses, recordClasses);
		
		if (hints) {
			outFile = new File(String.format("%s/classifiersHint/attributes", classifiersFolder));
		} else {
			outFile = new File(String.format("%s/classifiersNoHint/attributes", classifiersFolder));
		}
		outFile.mkdirs();
		
		if (hints) {
			outFile = new File(String.format("%s/classifiersHint/records", classifiersFolder));
		} else {
			outFile = new File(String.format("%s/classifiersNoHint/records", classifiersFolder));
		}
		outFile.mkdirs();
		
		//Attributes
		if (hints) {
			tableFile = new File(String.format("%s/trainingTablesHint/attributes.csv", tablesFolder));
		} else {
			tableFile = new File(String.format("%s/trainingTablesNoHint/attributes.csv", tablesFolder));
		}
		for (String attributeClass : attributeClasses) {
	    	model = trainBinaryClassifier(tableFile.getAbsolutePath(), attributeClass, numTrees);
	    	if (hints) {
	    		model.save(String.format("%s/classifiersHint/attributes/%s", classifiersFolder, attributeClass));
			} else {
				model.save(String.format("%s/classifiersNoHint/attributes/%s", classifiersFolder, attributeClass));
			}
		}
		//Records
		if (hints) {
			tableFile = new File(String.format("%s/trainingTablesHint/records.csv", tablesFolder));
		} else {
			tableFile = new File(String.format("%s/trainingTablesNoHint/records.csv", tablesFolder));
		}
		for (String recordClass : recordClasses) {
			model = trainBinaryClassifier(tableFile.getAbsolutePath(), recordClass, numTrees);
			if (hints) {
	    		model.save(String.format("%s/classifiersHint/records/%s", classifiersFolder, recordClass));
			} else {
				model.save(String.format("%s/classifiersNoHint/records/%s", classifiersFolder, recordClass));
			}
		}
		
	}
	
	public double[] applyClassifiers(Slot slot, boolean useProbabilities, boolean hints) {
		assert slot != null;
		assert this.classifiersFolder != null;
		
		double[] result;
		List<ClassifierRandomForest> models;
		double classification;
		List<Double> classifications;
		Vector vector;
		
		vector = featuresVectorToVector(slot.getFeaturesVector());
		if (slot instanceof Record) {
			if (hints) {
				models = recordClassifiersHintBased;
			} else {
				models = recordClassifiersHintFree;
			}
		} else {
			if (hints) {
				models = attributeClassifiersHintBased;
			} else {
				models = attributeClassifiersHintFree;
			}
		}
		
		classifications = new ArrayList<Double>();
		for (ClassifierRandomForest model: models) {
			if (useProbabilities) {
				classification = model.getModel().predictProbability(vector).toArray()[1];
			} else {
				classification = model.getModel().predict(vector);
			}
	    	classifications.add(classification);
		}
		result = new double[classifications.size()];
		for (int i = 0; i < classifications.size(); i++) {
			result[i] = classifications.get(i);
		}
		
		return result;
	}
	
	public LinkedHashMap<String, Double> applyClassifiersMap(Slot slot, boolean useProbabilities, boolean hints) {
		assert slot != null;
		assert this.classifiersFolder != null;
		
		LinkedHashMap<String, Double> result;
		List<ClassifierRandomForest> models;
		double classification;
		Vector vector;
		
		result = new LinkedHashMap<String, Double>();
		vector = featuresVectorToVector(slot.getFeaturesVector());
		if (slot instanceof Record) {
			if (hints) {
				models = recordClassifiersHintBased;
			} else {
				models = recordClassifiersHintFree;
			}
		} else {
			if (hints) {
				models = attributeClassifiersHintBased;
			} else {
				models = attributeClassifiersHintFree;
			}
		}
		for (ClassifierRandomForest model : models) {
			if (useProbabilities) {
				classification = model.getModel().predictProbability(vector).toArray()[1];
			} else {
				classification = model.getModel().predict(vector);
			}
	    	result.put(model.getName(), classification);
		}
		
		return result;
	}
	
	
	public void createMulticlassClassifierCsv(boolean useProbabilities, boolean hints) throws IOException, ParseException {
		assert this.tablesFolder != null;
		assert this.classifiersFolder != null;
		
		String filePath;
		File csvFile;
		String newCsvPath;
		List<RandomForestClassificationModel> models;
		RandomForestClassificationModel model;
		List<String> attributeClasses;
		List<String> recordClasses;
		Map<String, Double> classesMapping;
		String slotClass;
		List<String> featureList;
		List<String> classifierNames;
		double[] featureValues;
		double slotClassDouble;
		Vector vector;
		File classifiersFolderFile;
		File[] classifiersFiles;
		double classification;
		String line;
		String elementId;
		
		attributeClasses = new ArrayList<String>();
		recordClasses = new ArrayList<String>();
		
		addClasses(attributeClasses, recordClasses);
		classesMapping = new HashMap<String, Double>();
		
		for (int i = 0; i < attributeClasses.size(); i++) {
			slotClass = attributeClasses.get(i);
			classesMapping.put(slotClass, (double)i);
		}
		for (int i = 0; i < recordClasses.size(); i++) {
			slotClass = recordClasses.get(i);
			classesMapping.put(slotClass, (double)i);
		}
		
		//Attributes
		if (hints) {
			filePath = String.format("%s/trainingTablesHint/attributes.csv", tablesFolder);
			newCsvPath = String.format("%s/trainingTablesHint/attributes_Multiclass.csv", tablesFolder);
		} else {
			filePath = String.format("%s/trainingTablesNoHint/attributes.csv", tablesFolder);
			newCsvPath = String.format("%s/trainingTablesNoHint/attributes_Multiclass.csv", tablesFolder);
		}
		
		FileUtils.createCSV(newCsvPath);
		csvFile = new File(filePath);
		if (hints) {
			classifiersFolderFile = new File(String.format("%s/classifiersHint/attributes", classifiersFolder));
		} else {
			classifiersFolderFile = new File(String.format("%s/classifiersNoHint/attributes", classifiersFolder));
		}
        classifiersFiles = classifiersFolderFile.listFiles();
        models = Lists.newArrayList();
        classifierNames = new ArrayList<String>();
        classifierNames.add("id");
        for (File file : classifiersFiles) {
     	   classifierNames.add(file.getName());
     	   model = RandomForestClassificationModel.load(file.getAbsolutePath());
     	   models.add(model);
        }
        classifierNames.add("class");
        FileUtils.addLine(newCsvPath, classifierNames);
        
		try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
		    br.readLine();
		    while ((line = br.readLine()) != null) {
	            featureList = Arrays.asList(line.trim().split(","));
	            elementId = featureList.get(0);
	            featureList = featureList.subList(1, featureList.size());
	            featureValues = new double[featureList.size()-1];
	            slotClass = featureList.get(featureList.size()-1);
	            for(int i = 0; i<featureList.size()-1; i++) {
	            	featureValues[i] = Double.valueOf(featureList.get(i));
	            }
	           vector = Vectors.dense(featureValues);
	           
	           slotClassDouble = classesMapping.get(slotClass);
				
	           //Classifiers application. Can't use external methods due to how they are created.
	           featureList = new ArrayList<String>();
	           featureList.add(elementId);
	           for (RandomForestClassificationModel MPCModel : models) {
	        	   if (useProbabilities) {
	        		   classification = MPCModel.predictProbability(vector).toArray()[1];
	        	   } else {
	        		   classification = MPCModel.predict(vector);
	        	   }
	        	   featureList.add(String.valueOf(classification));
	           }
	           featureList.add(String.valueOf(slotClassDouble));
	           FileUtils.addLine(newCsvPath, featureList);
	           System.out.println(String.format("Added line: %s", featureList));
		    }
		}
		
		//Records
		if (hints) {
			filePath = String.format("%s/trainingTablesHint/records.csv", tablesFolder);
			newCsvPath = String.format("%s/trainingTablesHint/records_Multiclass.csv", tablesFolder);
		} else {
			filePath = String.format("%s/trainingTablesNoHint/records.csv", tablesFolder);
			newCsvPath = String.format("%s/trainingTablesNoHint/records_Multiclass.csv", tablesFolder);
		}
		FileUtils.createCSV(newCsvPath);
		csvFile = new File(filePath);
		if (hints) {
			classifiersFolderFile = new File(String.format("%s/classifiersHint/records", classifiersFolder));
		} else {
			classifiersFolderFile = new File(String.format("%s/classifiersNoHint/records", classifiersFolder));
		}
		models = Lists.newArrayList();
        classifiersFiles = classifiersFolderFile.listFiles();
        classifierNames = new ArrayList<String>();
        classifierNames.add("id");
        for (File file : classifiersFiles) {
     	   classifierNames.add(file.getName());
     	   model = RandomForestClassificationModel.load(file.getAbsolutePath());
    	   models.add(model);
        }
        classifierNames.add("class");
        FileUtils.addLine(newCsvPath, classifierNames);
		try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
		    br.readLine();
		    while ((line = br.readLine()) != null) {
	        	
	            featureList = Arrays.asList(line.trim().split(","));
	            elementId = featureList.get(0);
	            featureList = featureList.subList(1, featureList.size());
	            featureValues = new double[featureList.size()-1];
	            slotClass = featureList.get(featureList.size()-1);
	            for(int i = 0; i<featureList.size()-1; i++) {
	            	featureValues[i] = Double.valueOf(featureList.get(i));
	            }
	           vector = Vectors.dense(featureValues);
	           
	           slotClassDouble = classesMapping.get(slotClass);
				
	           //Classifiers application. Can't use external methods due to how they are created.
	           featureList = new ArrayList<String>();
	           featureList.add(elementId);
	           for (RandomForestClassificationModel MPCModel : models) {
	        	   classification = MPCModel.predict(vector);
	        	   featureList.add(String.valueOf(classification));
	           }
	           featureList.add(String.valueOf(slotClassDouble));
	           FileUtils.addLine(newCsvPath, featureList);
	           System.out.println(String.format("Added line: %s", featureList));
		    }
		}
		
	}

	public void trainMulticlassClassifier(boolean hints) throws IOException, ParseException {
		List<String> attributeClasses;
		List<String> recordClasses;
		File tableFile;
		FileReader reader;
		BufferedReader bufferedReader;
		String firstLine;
		Integer numberOfFeatures;
		Integer numClasses;
		int[] layers;
		String trainingFilePath;
		JavaRDD<String> csv; 
		Dataset<Row> data;
		MultilayerPerceptronClassifier trainer;
		MultilayerPerceptronClassificationModel model;
		
		attributeClasses = new ArrayList<String>();
		recordClasses = new ArrayList<String>();
		trainer = new MultilayerPerceptronClassifier().setMaxIter(numIterations).setLabelCol("label").setFeaturesCol("features");
		addClasses(attributeClasses, recordClasses);
		
		//Atributes
		numClasses = attributeClasses.size();
		if (hints) {
			trainingFilePath = String.format("%s/trainingTablesHint/attributes_Multiclass.csv", tablesFolder);
		} else {
			trainingFilePath = String.format("%s/trainingTablesNoHint/attributes_Multiclass.csv", tablesFolder);
		}
		csv = context.textFile(trainingFilePath);
		tableFile = new File(trainingFilePath);
		reader = new FileReader(tableFile);
		bufferedReader = new BufferedReader(reader);
		firstLine = bufferedReader.readLine();
		bufferedReader.close();
		numberOfFeatures = firstLine.split(",").length-2;
		layers = new int[] {numberOfFeatures};
		layers = ArrayUtils.addAll(layers, intermediaryLayers);
		layers = ArrayUtils.add(layers, numClasses);
		data = csvToDataset(csv);
		trainer = trainer.setLayers(layers);
		model = trainer.fit(data);
		if (hints) {
			model.save(String.format("%s/classifiersHint/attributes_multiclass", classifiersFolder));
		} else {
			model.save(String.format("%s/classifiersNoHint/attributes_multiclass", classifiersFolder));
		}
		
		
		//Records
		numClasses = recordClasses.size();
		if(numClasses > 0){
			if (hints) {
				trainingFilePath = String.format("%s/trainingTablesHint/records_Multiclass.csv", tablesFolder);
			} else {
				trainingFilePath = String.format("%s/trainingTablesNoHint/records_Multiclass.csv", tablesFolder);
			}
			csv = context.textFile(trainingFilePath);
			tableFile = new File(trainingFilePath);
			reader = new FileReader(tableFile);
			bufferedReader = new BufferedReader(reader);
			firstLine = bufferedReader.readLine();
			bufferedReader.close();
			numberOfFeatures = firstLine.split(",").length-2;
			layers = new int[] {numberOfFeatures};
			layers = ArrayUtils.addAll(layers, intermediaryLayers);
			layers = ArrayUtils.add(layers, numClasses);
			data = csvToDataset(csv);
			trainer = trainer.setLayers(layers);
			model = trainer.fit(data);
			if (hints) {
				model.save(String.format("%s/classifiersHint/records_multiclass", classifiersFolder));
			} else {
				model.save(String.format("%s/classifiersNoHint/records_multiclass", classifiersFolder));
			}
		}
	
	}
	
	//Ancillary methods----------------------------------------------
	
	private RandomForestClassificationModel trainBinaryClassifier(String trainingFilePath, String slotClass, Integer numTrees) {
		assert context != null;
		assert trainingFilePath != null;
		assert numTrees != null;
		assert numTrees > 0;
		assert slotClass != null;
		
		final RandomForestClassificationModel result;
		RandomForestClassifier trainer;
		JavaRDD<String> csv; 
		Dataset<Row> trainingData;
		
		csv = context.textFile(trainingFilePath);
		trainingData = csvToDataset(csv, slotClass);
		trainer = new RandomForestClassifier()
				  .setNumTrees(numTrees)
				  .setSeed(1234)
				  .setLabelCol("label")
				  .setFeaturesCol("features");
		System.out.println(String.format("Training classifier for class %s", slotClass));
		result = trainer.fit(trainingData);
		System.out.println(result.toString());
		return result;
	}
	
	private Dataset<Row> csvToDataset(JavaRDD<String> data, final String slotClass) {
		assert data != null;
		
		Dataset<Row> result;
		JavaRDD<Row> rowsRDD;
		final String header;
		StructField structField;
		StructField[] structFields;
		StructType structType;
		
		structFields = new StructField[2];
		structField = DataTypes.createStructField("features", new VectorUDT(), false);
		structFields[0] = structField;
		structField = DataTypes.createStructField("label", DataTypes.DoubleType, false);
		structFields[1] = structField;
		structType = new StructType(structFields);
		
		header = data.first();
		data = data.filter(new Function<String, Boolean>() {
			@Override
			public Boolean call(String v1) throws Exception {
				assert v1 != null;
				
				Boolean result;
				
				result = !v1.equals(header);
				
				return result;
			}
		});
		rowsRDD = data.map(new Function<String, Row>() {

			private static final long serialVersionUID = -2554198184785931531L;

			@Override public Row call(String alert) {
	        	Row result;
	        	List<String> featureList;
	        	double[] featureValues;
	        	String featureClass;
	        	Double featureClassDouble;
	        	Vector vector;
	        	
	            featureList = Arrays.asList(alert.trim().split(","));
	            featureList = featureList.subList(1, featureList.size());
	            featureValues = new double[featureList.size()-1];
	            featureClass = featureList.get(featureList.size()-1);
	            for(int i = 0; i<featureList.size()-1; i++) {
	            	featureValues[i] = Double.valueOf(featureList.get(i));
	            }
	           vector = Vectors.dense(featureValues);
	           featureClassDouble = featureClass.equals(slotClass)? 1.0 : 0.0;
	           result = RowFactory.create(vector, featureClassDouble);
	           return result;
	        }
	    });
		
		result = sparkSession.createDataFrame(rowsRDD, structType);
		return result;
	}
	
	private Dataset<Row> csvToDataset(JavaRDD<String> data, final Map<String, Double> classesMapping) {
		assert data != null;
		
		Dataset<Row> result;
		JavaRDD<Row> rowsRDD;
		final String header;
		StructField structField;
		StructField[] structFields;
		StructType structType;
		
		structFields = new StructField[2];
		structField = DataTypes.createStructField("features", new VectorUDT(), false);
		structFields[0] = structField;
		structField = DataTypes.createStructField("label", DataTypes.DoubleType, false);
		structFields[1] = structField;
		structType = new StructType(structFields);
		
		header = data.first();
		data = data.filter(new Function<String, Boolean>() {
			@Override
			public Boolean call(String v1) throws Exception {
				assert v1 != null;
				
				Boolean result;
				
				result = !v1.equals(header);
				
				return result;
			}
		});
		rowsRDD = data.map(new Function<String, Row>() {

			private static final long serialVersionUID = -2554198184785931531L;

			@Override public Row call(String alert) {
	        	Row result;
	        	List<String> featureList;
	        	double[] featureValues;
	        	String featureClass;
	        	Double featureClassDouble;
	        	Vector vector;
	        	
	            featureList = Arrays.asList(alert.trim().split(","));
	            featureList = featureList.subList(1, featureList.size());
	            featureValues = new double[featureList.size()-1];
	            featureClass = featureList.get(featureList.size()-1);
	            for(int i = 0; i<featureList.size()-1; i++) {
	            	featureValues[i] = Double.valueOf(featureList.get(i));
	            }
	           vector = Vectors.dense(featureValues);
	           featureClassDouble = classesMapping.get(featureClass);
	           result = RowFactory.create(vector, featureClassDouble);
	           return result;
	        }
	    });
		
		result = sparkSession.createDataFrame(rowsRDD, structType);
		return result;
	}
	
	private Dataset<Row> csvToDataset(JavaRDD<String> data) {
		assert data != null;
		
		Dataset<Row> result;
		JavaRDD<Row> rowsRDD;
		final String header;
		StructField structField;
		StructField[] structFields;
		StructType structType;
		
		structFields = new StructField[2];
		structField = DataTypes.createStructField("features", new VectorUDT(), false);
		structFields[0] = structField;
		structField = DataTypes.createStructField("label", DataTypes.DoubleType, false);
		structFields[1] = structField;
		structType = new StructType(structFields);
		
		header = data.first();
		data = data.filter(new Function<String, Boolean>() {
			@Override
			public Boolean call(String v1) throws Exception {
				assert v1 != null;
				
				Boolean result;
				
				result = !v1.equals(header);
				
				return result;
			}
		});
		rowsRDD = data.map(new Function<String, Row>() {

			private static final long serialVersionUID = -2554198184785931531L;

			@Override public Row call(String alert) {
	        	Row result;
	        	List<String> featureList;
	        	double[] featureValues;
	        	String featureClass;
	        	Double featureClassDouble;
	        	Vector vector;
	        	
	            featureList = Arrays.asList(alert.trim().split(","));
	            featureList = featureList.subList(1, featureList.size());
	            featureValues = new double[featureList.size()-1];
	            featureClass = featureList.get(featureList.size()-1);
	            for(int i = 0; i<featureList.size()-1; i++) {
	            	featureValues[i] = Double.valueOf(featureList.get(i));
	            }
	           vector = Vectors.dense(featureValues);
	           featureClassDouble = Double.valueOf(featureClass);
	           result = RowFactory.create(vector, featureClassDouble);
	           return result;
	        }
	    });
		
		result = sparkSession.createDataFrame(rowsRDD, structType);
		return result;
	}
	
	private void addClasses(List<String> emptyAttributeClasses, List<String> emptyRecordClasses) throws IOException, ParseException {
		assert this.classesFilePath != null;
		
		JSONParser jsonParser;
		File classesFile;
		FileReader fileReader;
		JSONObject jsonObject;
		List<String> recordClasses;
		List<String> attributeClasses;
		
		jsonParser = new JSONParser();
		classesFile = new File(String.format("%s", classesFilePath));
		fileReader = new FileReader(classesFile);
		jsonObject = (JSONObject)jsonParser.parse(fileReader);
		recordClasses = (List<String>)jsonObject.get("recordClasses");
		attributeClasses = (List<String>)jsonObject.get("attributeClasses");
		
		emptyAttributeClasses.addAll(attributeClasses);
		emptyRecordClasses.addAll(recordClasses);
	}
	
	private Vector featuresVectorToVector(FeaturesVector featuresVector) {
		assert featuresVector != null;
		
		Vector result;
		double[] doubleFeatureValues;
		List<String> featureValues;
		double value;
		String stringValue;
		
		featureValues = featuresVector.getRawValues();
		if(featuresVector.getVectorClass() != null) {
			featureValues = featureValues.subList(0, featureValues.size()-1);
		}
		doubleFeatureValues = new double[featureValues.size()];
		for (int i = 0; i < featureValues.size(); i++) {
			stringValue = featureValues.get(i);
			value = Double.valueOf(stringValue);
			doubleFeatureValues[i] = value;
		}
		result = Vectors.dense(doubleFeatureValues);
		
		return result;
	}
		
}