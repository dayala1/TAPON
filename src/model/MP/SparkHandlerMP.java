package model.MP;

import model.Classifier;
import model.SparkHandler;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.ml.classification.*;
import org.apache.spark.ml.linalg.Vector;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SparkHandlerMP extends SparkHandler {
	private static final long serialVersionUID = 4424575592980018563L;

	// Interface methods----------------------------------------------


	public void writeClassifiersString(String folderPath) throws IOException {

		File classifierFile;
		BufferedWriter writer;

		for (Classifier model:attributeClassifiersHintBased) {
			classifierFile = new File(String.format("%s/hintBased/attributes/%s/%s.txt", folderPath, model.getName(), model.getName()));
			classifierFile.getParentFile().mkdirs();
			classifierFile.createNewFile();
			writer = new BufferedWriter(new FileWriter(classifierFile));
			writer.write(((MultilayerPerceptronClassificationModel) model.getModel()).toString());
			writer.close();
		}

		new File(String.format("%s/hintBased/records/", folderPath)).mkdirs();
		for (Classifier model:recordClassifiersHintBased) {
			classifierFile = new File(String.format("%s/hintBased/records/%s/%s.txt", folderPath, model.getName(), model.getName()));
			classifierFile.getParentFile().mkdirs();
			classifierFile.createNewFile();
			writer = new BufferedWriter(new FileWriter(classifierFile));
			writer.write(((MultilayerPerceptronClassificationModel) model.getModel()).toString());
			writer.close();
		}

		new File(String.format("%s/hintFree/attributes/", folderPath)).mkdirs();
		for (Classifier model:attributeClassifiersHintFree) {
			classifierFile = new File(String.format("%s/hintFree/attributes/%s/%s.txt", folderPath, model.getName(), model.getName()));
			classifierFile.getParentFile().mkdirs();
			classifierFile.createNewFile();
			writer = new BufferedWriter(new FileWriter(classifierFile));
			writer.write(((MultilayerPerceptronClassificationModel) model.getModel()).toString());
			writer.close();
		}

		new File(String.format("%s/hintFree/records/", folderPath)).mkdirs();
		for (Classifier model:recordClassifiersHintFree) {
			classifierFile = new File(String.format("%s/hintFree/records/%s/%s.txt", folderPath, model.getName(), model.getName()));
			classifierFile.getParentFile().mkdirs();
			classifierFile.createNewFile();
			writer = new BufferedWriter(new FileWriter(classifierFile));
			writer.write(((MultilayerPerceptronClassificationModel) model.getModel()).toString());
			writer.close();
		}
	}


	@Override
	public MultilayerPerceptronClassificationModel loadClassifier(String path) {return MultilayerPerceptronClassificationModel.load(path); }

	@Override
	public Classifier createClassifier() {
		return new ClassifierMP();
	}

	@Override
	public void saveClassifier(ClassificationModel classifier, String path) throws IOException {
		((MultilayerPerceptronClassificationModel)classifier).save(path);
	}

	@Override
	public void createBinaryClassifiers(boolean hints) throws IOException, ParseException {
		assert this.tablesFolder != null;
		assert this.classifiersFolder != null;

		File outFile;
		File tableFile;
		FileReader reader;
		BufferedReader bufferedReader;
		String firstLine;
		ClassificationModel model;
		List<Integer> layers;

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

		//We need to configure the layers of the neural network. For this, we need how many features there are, since that is the number of neurons in the first layer.
		reader = new FileReader(tableFile);
		bufferedReader = new BufferedReader(reader);
		firstLine = bufferedReader.readLine();
		bufferedReader.close();
		numberOfFeatures = firstLine.split(",").length - 1;
		layers = new ArrayList<>();
		layers.add(numberOfFeatures);
		layers.add(numberOfFeatures/2);
		layers.add(5);
		layers.add(2);
		params.put("layers", layers);

		for (String attributeClass : classesConfiguration.getAttributeClasses()) {
			model = trainBinaryClassifier(tableFile.getAbsolutePath(), attributeClass);
			if (hints) {
				saveClassifier(model, String.format("%s/classifiersHint/attributes/%s", classifiersFolder, attributeClass));
			} else {
				saveClassifier(model, String.format("%s/classifiersNoHint/attributes/%s", classifiersFolder, attributeClass));
			}
		}
		// Records
		if (hints) {
			tableFile = new File(String.format("%s/trainingTablesHint/records.csv", tablesFolder));
		} else {
			tableFile = new File(String.format("%s/trainingTablesNoHint/records.csv", tablesFolder));
		}

		//The same again, but with records, which have a different number of features
		reader = new FileReader(tableFile);
		bufferedReader = new BufferedReader(reader);
		firstLine = bufferedReader.readLine();
		bufferedReader.close();
		numberOfFeatures = firstLine.split(",").length - 1;
		layers = new ArrayList<>();
		layers.add(numberOfFeatures);
		layers.add(numberOfFeatures/2);
		layers.add(5);
		layers.add(2);
		params.put("layers", layers);

		for (String recordClass : classesConfiguration.getRecordClasses()) {
			model = trainBinaryClassifier(tableFile.getAbsolutePath(), recordClass);
			if (hints) {
				saveClassifier(model, String.format("%s/classifiersHint/records/%s", classifiersFolder, recordClass));
			} else {
				saveClassifier(model, String.format("%s/classifiersNoHint/records/%s", classifiersFolder, recordClass));
			}
		}
	}

	protected MultilayerPerceptronClassificationModel trainBinaryClassifier(String trainingFilePath, String slotClass) {
		assert context != null;
		assert trainingFilePath != null;
		assert slotClass != null;

		List<Integer> layers;
		Integer maxIter;
		Long seed;
		Double stepSize;
		Double tol;
		Vector initialWeights;
		Integer blockSize;
		String solver;

		final MultilayerPerceptronClassificationModel result;
		MultilayerPerceptronClassifier trainer;
		JavaRDD<String> csv;
		Dataset<Row> trainingData;

		csv = context.textFile(trainingFilePath);
		trainingData = csvToDataset(csv, slotClass);
		trainer = new MultilayerPerceptronClassifier().setLabelCol("label").setFeaturesCol("features");

		if(params.containsKey("layers")){
			layers = (List<Integer>) params.get("layers");
			trainer = trainer.setLayers(layers.stream().mapToInt(i -> i).toArray());
		}

		if(params.containsKey("maxIter")){
			maxIter = (Integer) params.get("maxIter");
			trainer = trainer.setMaxIter(maxIter);
		}

		if(params.containsKey("seed")){
			seed = (Long) params.get("seed");
			trainer = trainer.setSeed(seed);
		}

		if(params.containsKey("stepSize")){
			stepSize = (Double) params.get("stepSize");
			trainer = trainer.setStepSize(stepSize);
		}

		if(params.containsKey("lossType")){
			tol = (Double) params.get("tol");
			trainer = trainer.setTol(tol);
		}

		if(params.containsKey("initialWeights")){
			initialWeights = (Vector) params.get("initialWeights");
			trainer = trainer.setInitialWeights(initialWeights);
		}

		if(params.containsKey("blockSize")){
			blockSize = (Integer) params.get("blockSize");
			trainer = trainer.setBlockSize(blockSize);
		}

		if(params.containsKey("solver")){
			solver = (String) params.get("solver");
			trainer = trainer.setSolver(solver);
		}

		System.out.println(String.format("Training classifier for class %s", slotClass));
		result = trainer.fit(trainingData);
		System.out.println(result.toString());
		return result;
	}
}