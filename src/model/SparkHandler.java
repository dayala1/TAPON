package model;

import dataset.Attribute;
import dataset.Record;
import dataset.Slot;
import featuresCalculation.ClassesConfiguration;
import featuresCalculation.FeaturesVector;
import jersey.repackaged.com.google.common.collect.Lists;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.ml.classification.*;
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
import org.json.simple.parser.ParseException;
import utils.FileUtilsCust;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

public abstract class SparkHandler implements Serializable {
	protected static final long serialVersionUID = 4424575592980018563L;

	// Constructors---------------------------------------------------
	public SparkHandler(){
		this.featureNamesAttributesHF = new HashMap<>();
		this.featureNamesAttributesHB = new HashMap<>();
		this.featureNamesRecordsHF = new HashMap<>();
		this.featureNamesRecordsHB = new HashMap<>();
		params = new HashMap<>();
	}

	// Properties-----------------------------------------------------
	protected String tablesFolder;
	protected String classifiersFolder;
	protected String classesFilePath;
	protected Integer numberOfFeatures;
	// no default. Using 40
	// More trees +quality and +time. Using 100
	protected ClassesConfiguration classesConfiguration;
	protected Map<Integer, String> featureNamesAttributesHF;
	protected Map<Integer, String> featureNamesAttributesHB;
	protected Map<Integer, String> featureNamesRecordsHF;
	protected Map<Integer, String> featureNamesRecordsHB;
	protected Map<String, Object> params;

	public void setParam(String param, Object value){
		params.put(param, value);
	}

	public Object getParam(String param){
		return params.get(param);
	}

	public Map<Integer, String> getFeatureNamesAttributesHF() {
		return featureNamesAttributesHF;
	}

	public Map<Integer, String> getFeatureNamesAttributesHB() {
		return featureNamesAttributesHB;
	}

	public Map<Integer, String> getFeatureNamesRecordsHF() {
		return featureNamesRecordsHF;
	}

	public Map<Integer, String> getFeatureNamesRecordsHB() {
		return featureNamesRecordsHB;
	}

	public List<Classifier> getAttributeClassifiersHintFree() {
		return attributeClassifiersHintFree;
	}

	public List<Classifier> getRecordClassifiersHintFree() {
		return recordClassifiersHintFree;
	}

	public List<Classifier> getAttributeClassifiersHintBased() {
		return attributeClassifiersHintBased;
	}

	public List<Classifier> getRecordClassifiersHintBased() {
		return recordClassifiersHintBased;
	}

	public MultilayerPerceptronClassificationModel getAttributeMulticlassClassifierHintFree() {
		return attributeMulticlassClassifierHintFree;
	}

	public MultilayerPerceptronClassificationModel getRecordMulticlassClassifierHintFree() {
		return recordMulticlassClassifierHintFree;
	}

	public MultilayerPerceptronClassificationModel getAttributeMulticlassClassifierHintBased() {
		return attributeMulticlassClassifierHintBased;
	}

	public MultilayerPerceptronClassificationModel getRecordMulticlassClassifierHintBased() {
		return recordMulticlassClassifierHintBased;
	}

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

	public ClassesConfiguration getClassesConfiguration() {
		return classesConfiguration;
	}

	public void setClassesConfiguration(ClassesConfiguration classesConfiguration) {
		assert classesConfiguration != null;

		this.classesConfiguration = classesConfiguration;
	}

	// Internal state-------------------------------------------------

	protected final static Level loggerLevel = Level.ERROR;
	// Here start the parameters for the multiclass classifier
	protected final Integer numIterations = 200;
	protected final int[] intermediaryLayers = {};
	protected volatile static JavaSparkContext context;
	protected volatile static SparkContext sparkContext;
	protected volatile static SparkSession sparkSession;
	protected volatile static Integer numUsers = 0;
	protected List<Classifier> attributeClassifiersHintFree;
	protected List<Classifier> recordClassifiersHintFree;
	protected List<Classifier> attributeClassifiersHintBased;
	protected List<Classifier> recordClassifiersHintBased;
	protected MultilayerPerceptronClassificationModel attributeMulticlassClassifierHintFree;
	protected MultilayerPerceptronClassificationModel recordMulticlassClassifierHintFree;
	protected MultilayerPerceptronClassificationModel attributeMulticlassClassifierHintBased;
	protected MultilayerPerceptronClassificationModel recordMulticlassClassifierHintBased;

	static {
		Logger.getLogger("org").setLevel(loggerLevel);
		Logger.getLogger("akka").setLevel(loggerLevel);
	}

	// Interface methods----------------------------------------------

	public synchronized void closeContext() {
		numUsers--;
		if(numUsers == 0) {
			context.close();
		}
	}

	public void loadFeatureNames() throws IOException {
		File tableFile;
		String header;
		BufferedReader reader;
		List<String> featureList;

		tableFile = new File(String.format("%s/trainingTablesHint/attributes.csv", tablesFolder));
		reader = new BufferedReader(new FileReader(tableFile));
		header = reader.readLine();
		featureList = Arrays.asList(header.trim().split(","));
		featureList = featureList.subList(1, featureList.size()-1);
		for (int i = 0; i < featureList.size(); i++) {
			featureNamesAttributesHB.put(i, featureList.get(i));
		}

		tableFile = new File(String.format("%s/trainingTablesNoHint/attributes.csv", tablesFolder));
		reader = new BufferedReader(new FileReader(tableFile));
		header = reader.readLine();
		featureList = Arrays.asList(header.trim().split(","));
		featureList = featureList.subList(1, featureList.size()-1);
		for (int i = 0; i < featureList.size(); i++) {
			featureNamesAttributesHF.put(i, featureList.get(i));
		}

		tableFile = new File(String.format("%s/trainingTablesHint/records.csv", tablesFolder));
		reader = new BufferedReader(new FileReader(tableFile));
		header = reader.readLine();
		featureList = Arrays.asList(header.trim().split(","));
		featureList = featureList.subList(1, featureList.size()-1);
		for (int i = 0; i < featureList.size(); i++) {
			featureNamesRecordsHB.put(i, featureList.get(i));
		}

		tableFile = new File(String.format("%s/trainingTablesNoHint/records.csv", tablesFolder));
		reader = new BufferedReader(new FileReader(tableFile));
		header = reader.readLine();
		featureList = Arrays.asList(header.trim().split(","));
		featureList = featureList.subList(1, featureList.size()-1);
		for (int i = 0; i < featureList.size(); i++) {
			featureNamesRecordsHF.put(i, featureList.get(i));
		}
	}

	public abstract void writeClassifiersString(String folderPath) throws IOException;

	public abstract ClassificationModel loadClassifier(String path);

	public abstract Classifier createClassifier();

	public abstract void saveClassifier(ClassificationModel classifier, String path) throws IOException;

	public void loadClassifiers(boolean hints){
		assert classifiersFolder != null;

		File classifiersFolderFile;
		File[] classifiersFiles;
		ClassificationModel cm;
		String classifierPath;
		Classifier model;
		int i;

		i = 0;
		if (!hints) {
			this.attributeClassifiersHintFree = Lists.newArrayList();
			this.recordClassifiersHintFree = Lists.newArrayList();

			classifiersFolderFile = new File(String.format("%s/classifiersNoHint/attributes", classifiersFolder));
			classifiersFiles = classifiersFolderFile.listFiles();
			for (File file : classifiersFiles) {
				System.out.println("Loading classifier" + file.getName());
				cm = loadClassifier(file.getAbsolutePath());
				model = createClassifier();
				model.setModel(cm);
				model.setName(file.getName());
				attributeClassifiersHintFree.add(model);
				System.out.println(String.format("Loaded classifier %s", i));
				i++;
			}

			classifiersFolderFile = new File(String.format("%s/classifiersNoHint/records", classifiersFolder));
			classifiersFiles = classifiersFolderFile.listFiles();
			for (File file : classifiersFiles) {
				cm = loadClassifier(file.getAbsolutePath());
				model = createClassifier();
				model.setModel(cm);
				model.setName(file.getName());
				recordClassifiersHintFree.add(model);
				System.out.println(String.format("Loaded classifier %s", i));
				i++;
			}
			classifierPath = String.format("%s/classifiersNoHint/attributes_multiclass", classifiersFolder);
			attributeMulticlassClassifierHintFree = MultilayerPerceptronClassificationModel.load(classifierPath);
			classifierPath = String.format("%s/classifiersNoHint/records_multiclass", classifiersFolder);
			if (recordClassifiersHintFree.size() > 0) {
				recordMulticlassClassifierHintFree = MultilayerPerceptronClassificationModel.load(classifierPath);
			}
		} else {
			this.attributeClassifiersHintBased = Lists.newArrayList();
			this.recordClassifiersHintBased = Lists.newArrayList();

			classifiersFolderFile = new File(String.format("%s/classifiersHint/attributes", classifiersFolder));
			classifiersFiles = classifiersFolderFile.listFiles();
			for (File file : classifiersFiles) {
				cm = loadClassifier(file.getAbsolutePath());
				model = createClassifier();
				model.setModel(cm);
				model.setName(file.getName());
				attributeClassifiersHintBased.add(model);
				System.out.println(String.format("Loaded classifier %s", i));
				i++;
			}

			classifiersFolderFile = new File(String.format("%s/classifiersHint/records", classifiersFolder));
			classifiersFiles = classifiersFolderFile.listFiles();
			for (File file : classifiersFiles) {
				cm = loadClassifier(file.getAbsolutePath());
				model = createClassifier();
				model.setModel(cm);
				model.setName(file.getName());
				recordClassifiersHintBased.add(model);
				System.out.println(String.format("Loaded classifier %s", i));
				i++;
			}

			classifierPath = String.format("%s/classifiersHint/attributes_multiclass", classifiersFolder);
			attributeMulticlassClassifierHintBased = MultilayerPerceptronClassificationModel.load(classifierPath);
			classifierPath = String.format("%s/classifiersHint/records_multiclass", classifiersFolder);
			if (recordClassifiersHintBased.size() > 0) {
				recordMulticlassClassifierHintBased = MultilayerPerceptronClassificationModel.load(classifierPath);
			}
		}
	}

	public synchronized void createNewContext() {
		numUsers++;
		if(numUsers == 1) {
			SparkConf sparkConf;
			sparkConf = new SparkConf();
			sparkConf.setAppName("SparkHandler");
			sparkConf.setMaster(sparkConf.get("master", "local[*]"));
			sparkConf.set("spark.network.timeout", "99999999");
			context = new JavaSparkContext(sparkConf);
			sparkContext = context.sc();
			sparkSession = new SparkSession(sparkContext);
		}
		while(sparkSession==null){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public Pair<String, LinkedHashMap<String, Double>> classify(Slot slot, boolean useProbabilities, boolean hints) throws IOException, ParseException {
		return classify(slot, useProbabilities, hints, true);
	}

	public Pair<String, LinkedHashMap<String, Double>> classify(Slot slot, boolean useProbabilities, boolean hints, boolean useMulticlass)
			throws IOException, ParseException {
		assert slot != null;
		assert this.classifiersFolder != null;

		Pair<String, LinkedHashMap<String, Double>> result;
		String predictedClass;
		MultilayerPerceptronClassificationModel model;
		String slotClass;
		LinkedHashMap<String, Double> classificationsMap;
		LinkedHashMap<String, Double> ranking;
		Double[] classificationsTemp;
		double[] classifications;
		Vector vector;
		Integer prediction;

		if (slot instanceof Attribute) {
			if (hints) {
				model = attributeMulticlassClassifierHintBased;
			} else {
				model = attributeMulticlassClassifierHintFree;
			}
		} else {
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
		for (Double d : classificationsTemp) {
			classifications[i] = d;
			i++;
		}

		ranking = new LinkedHashMap<>();
		classificationsMap.entrySet().stream().sorted(Entry.<String, Double> comparingByValue().reversed())
				.forEachOrdered(x -> ranking.put(x.getKey(), x.getValue()));

		//If we want to use propositionalization, we apply the multiclass classifier. Otherwise, we just take the class with the most probability. Obviously yields invalid results if probabilities are not used
		if(useMulticlass){
			vector = Vectors.dense(classifications);
			prediction = (int)model.predict(vector);
			if (slot instanceof Attribute) {
				predictedClass = classesConfiguration.getAttributeClassesMapping().inverse().get(prediction);
			} else {
				predictedClass = classesConfiguration.getRecordClassesMapping().inverse().get(prediction);
			}
		} else {
			predictedClass = new ArrayList<>(ranking.keySet()).get(0);
		}

		result = Pair.of(predictedClass, ranking);

		return result;
	}

	public void createBinaryClassifiers(boolean hints) throws IOException, ParseException{
		assert this.tablesFolder != null;
		assert this.classifiersFolder != null;

		File outFile;
		File tableFile;
		String firstLine;
		ClassificationModel model;

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

		// Attributes
		if (hints) {
			tableFile = new File(String.format("%s/trainingTablesHint/attributes.csv", tablesFolder));
		} else {
			tableFile = new File(String.format("%s/trainingTablesNoHint/attributes.csv", tablesFolder));
		}
		for (String attributeClass : classesConfiguration.getAttributeClasses()) {
			model = trainBinaryClassifier(tableFile.getAbsolutePath(), attributeClass);
			if (hints) {
				saveClassifier(model, String.format("%s/classifiersHint/attributes/%s", classifiersFolder, attributeClass));
			} else {
				saveClassifier(model, String.format("%s/classifiersNoHint/attributes/%s", classifiersFolder, attributeClass));
			}
		}
		// Records
		/*
		if (hints) {
			tableFile = new File(String.format("%s/trainingTablesHint/records.csv", tablesFolder));
		} else {
			tableFile = new File(String.format("%s/trainingTablesNoHint/records.csv", tablesFolder));
		}
		for (String recordClass : classesConfiguration.getRecordClasses()) {
			model = trainBinaryClassifier(tableFile.getAbsolutePath(), recordClass);
			if (hints) {
				saveClassifier(model, String.format("%s/classifiersHint/records/%s", classifiersFolder, recordClass));
			} else {
				saveClassifier(model, String.format("%s/classifiersNoHint/records/%s", classifiersFolder, recordClass));
			}
		}*/
	}

	public double[] applyClassifiers(Slot slot, boolean useProbabilities, boolean hints) {
		assert slot != null;
		assert this.classifiersFolder != null;

		double[] result;
		List<Classifier> models;
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
		for (Classifier model : models) {
			ClassificationModel classifier = model.getModel();
			if (useProbabilities && classifier instanceof ProbabilisticClassificationModel) {
				classification = ((ProbabilisticClassificationModel)classifier).predictProbability(vector).toArray()[1];
			} else {
				classification = classifier.predict(vector);
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
		List<Classifier> models;
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

		for (Classifier model : models) {
			ClassificationModel classifier = model.getModel();
			if (useProbabilities && classifier instanceof ProbabilisticClassificationModel) {
				classification = ((ProbabilisticClassificationModel)classifier).predictProbability(vector).toArray()[1];
			} else {
				classification = classifier.predict(vector);
			}
			result.put(model.getName(), classification);
		}

		return result;
	}

	public void createMulticlassClassifierCsv(boolean useProbabilities, boolean hints)
			throws IOException, ParseException {
		assert this.tablesFolder != null;
		assert this.classifiersFolder != null;

		String filePath;
		File csvFile;
		String newCsvPath;
		List<ClassificationModel> models;
		ClassificationModel model;
		String slotClass;
		List<String> featureList;
		List<String> classifierNames;
		double[] featureValues;
		double slotClassDouble;
		Vector vector;
		File classifiersFolderFile;
		File[] classifiersFiles;
		FileWriter writer;
		CSVPrinter csvPrinter;
		double classification;
		String line;
		String elementId;

		// Attributes
		if (hints) {
			filePath = String.format("%s/trainingTablesHint/attributes.csv", tablesFolder);
			newCsvPath = String.format("%s/trainingTablesHint/attributes_Multiclass.csv", tablesFolder);
		} else {
			filePath = String.format("%s/trainingTablesNoHint/attributes.csv", tablesFolder);
			newCsvPath = String.format("%s/trainingTablesNoHint/attributes_Multiclass.csv", tablesFolder);
		}

		writer = new FileWriter(newCsvPath, true);
		csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withRecordSeparator("\n"));
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
			model = loadClassifier(file.getAbsolutePath());
			models.add(model);
		}
		classifierNames.add("class");
		csvPrinter.printRecord(classifierNames);
		FileUtilsCust.addLine(newCsvPath, classifierNames);

		try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
			br.readLine();
			while ((line = br.readLine()) != null) {
				featureList = Arrays.asList(line.trim().split(","));
				elementId = featureList.get(0);
				featureList = featureList.subList(1, featureList.size());
				featureValues = new double[featureList.size() - 1];
				slotClass = featureList.get(featureList.size() - 1);
				for (int i = 0; i < featureList.size() - 1; i++) {
					featureValues[i] = Double.valueOf(featureList.get(i));
				}
				vector = Vectors.dense(featureValues);

				slotClassDouble = classesConfiguration.getAttributeClassesMapping().get(slotClass);

				// Classifiers application. Can't use external methods due to
				// how they are created.
				featureList = new ArrayList<String>();
				featureList.add(elementId);
				for (ClassificationModel MPCModel : models) {
					if (useProbabilities && MPCModel instanceof ProbabilisticClassificationModel) {
						classification = ((ProbabilisticClassificationModel)MPCModel).predictProbability(vector).toArray()[1];
					} else {
						classification = MPCModel.predict(vector);
					}
					featureList.add(String.valueOf(classification));
				}
				featureList.add(String.valueOf(slotClassDouble));
				csvPrinter.printRecord(featureList);
				System.out.println(String.format("Added line: %s", featureList));
			}
		}
		csvPrinter.close();

		// Records
		/*
		if (hints) {
			filePath = String.format("%s/trainingTablesHint/records.csv", tablesFolder);
			newCsvPath = String.format("%s/trainingTablesHint/records_Multiclass.csv", tablesFolder);
		} else {
			filePath = String.format("%s/trainingTablesNoHint/records.csv", tablesFolder);
			newCsvPath = String.format("%s/trainingTablesNoHint/records_Multiclass.csv", tablesFolder);
		}
		writer = new FileWriter(newCsvPath, true);
		csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withRecordSeparator("\n"));
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
			model = loadClassifier(file.getAbsolutePath());
			models.add(model);
		}
		classifierNames.add("class");
		csvPrinter.printRecord(classifierNames);
		try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
			br.readLine();
			while ((line = br.readLine()) != null) {

				featureList = Arrays.asList(line.trim().split(","));
				elementId = featureList.get(0);
				featureList = featureList.subList(1, featureList.size());
				featureValues = new double[featureList.size() - 1];
				slotClass = featureList.get(featureList.size() - 1);
				for (int i = 0; i < featureList.size() - 1; i++) {
					featureValues[i] = Double.valueOf(featureList.get(i));
				}
				vector = Vectors.dense(featureValues);

				slotClassDouble = classesConfiguration.getRecordClassesMapping().get(slotClass);

				// Classifiers application. Can't use external methods due to
				// how they are created.
				featureList = new ArrayList<String>();
				featureList.add(elementId);
				for (ClassificationModel MPCModel : models) {
					if (useProbabilities && MPCModel instanceof ProbabilisticClassificationModel) {
						classification =  ((ProbabilisticClassificationModel)MPCModel).predictProbability(vector).toArray()[1];;
					} else {
						classification = MPCModel.predict(vector);
					}
					featureList.add(String.valueOf(classification));
				}
				featureList.add(String.valueOf(slotClassDouble));
				csvPrinter.printRecord(featureList);
				System.out.println(String.format("Added line: %s", featureList));
			}
		}*/
		csvPrinter.close();

	}

	public void trainMulticlassClassifier(boolean hints) throws IOException, ParseException {
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

		trainer = new MultilayerPerceptronClassifier().setMaxIter(numIterations).setLabelCol("label")
				.setFeaturesCol("features");

		// Atributes
		numClasses = classesConfiguration.getAttributeClasses().size();
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
		numberOfFeatures = firstLine.split(",").length - 2;
		layers = new int[] { numberOfFeatures };
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

		// Records
		/*
		numClasses = classesConfiguration.getRecordClasses().size();
		if (numClasses > 0) {
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
			numberOfFeatures = firstLine.split(",").length - 2;
			layers = new int[] { numberOfFeatures };
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
		*/
	}

	// Ancillary methods----------------------------------------------

	protected abstract ClassificationModel trainBinaryClassifier(String trainingFilePath, String slotClass);

	protected Dataset<Row> csvToDataset(JavaRDD<String> data, final String slotClass) {
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

			protected static final long serialVersionUID = -2554198184785931531L;

			@Override
			public Row call(String alert) {
				Row result;
				List<String> featureList;
				double[] featureValues;
				String featureClass;
				Double featureClassDouble;
				Vector vector;

				featureList = Arrays.asList(alert.trim().split(","));
				featureList = featureList.subList(1, featureList.size());
				featureValues = new double[featureList.size() - 1];
				featureClass = featureList.get(featureList.size() - 1);
				for (int i = 0; i < featureList.size() - 1; i++) {
					featureValues[i] = Double.valueOf(featureList.get(i));
				}
				vector = Vectors.dense(featureValues);
				featureClassDouble = featureClass.equals(slotClass) ? 1.0 : 0.0;
				result = RowFactory.create(vector, featureClassDouble);
				return result;
			}
		});

		result = sparkSession.createDataFrame(rowsRDD, structType);
		return result;
	}

	protected Dataset<Row> csvToDataset(JavaRDD<String> data, final Map<String, Double> classesMapping) {
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

			protected static final long serialVersionUID = -2554198184785931531L;

			@Override
			public Row call(String alert) {
				Row result;
				List<String> featureList;
				double[] featureValues;
				String featureClass;
				Double featureClassDouble;
				Vector vector;

				featureList = Arrays.asList(alert.trim().split(","));
				featureList = featureList.subList(1, featureList.size());
				featureValues = new double[featureList.size() - 1];
				featureClass = featureList.get(featureList.size() - 1);
				for (int i = 0; i < featureList.size() - 1; i++) {
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

	protected Dataset<Row> csvToDataset(JavaRDD<String> data) {
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

			protected static final long serialVersionUID = -2554198184785931531L;

			@Override
			public Row call(String alert) {
				Row result;
				List<String> featureList;
				double[] featureValues;
				String featureClass;
				Double featureClassDouble;
				Vector vector;

				featureList = Arrays.asList(alert.trim().split(","));
				featureList = featureList.subList(1, featureList.size());
				featureValues = new double[featureList.size() - 1];
				featureClass = featureList.get(featureList.size() - 1);
				for (int i = 0; i < featureList.size() - 1; i++) {
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

	protected Vector featuresVectorToVector(FeaturesVector featuresVector) {
		assert featuresVector != null;

		Vector result;
		double[] doubleFeatureValues;
		List<String> featureValues;
		double value;
		String stringValue;

		featureValues = featuresVector.getRawValues();
		if (featuresVector.getVectorClass() != null) {
			featureValues = featureValues.subList(0, featureValues.size() - 1);
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