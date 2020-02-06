package model.GBT;

import model.Classifier;
import model.SparkHandler;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.ml.classification.*;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class SparkHandlerGBT extends SparkHandler {
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
			writer.write(((GBTClassificationModel) model.getModel()).toDebugString());
			writer.close();
		}

		new File(String.format("%s/hintBased/records/", folderPath)).mkdirs();
		for (Classifier model:recordClassifiersHintBased) {
			classifierFile = new File(String.format("%s/hintBased/records/%s/%s.txt", folderPath, model.getName(), model.getName()));
			classifierFile.getParentFile().mkdirs();
			classifierFile.createNewFile();
			writer = new BufferedWriter(new FileWriter(classifierFile));
			writer.write(((GBTClassificationModel) model.getModel()).toDebugString());
			writer.close();
		}

		new File(String.format("%s/hintFree/attributes/", folderPath)).mkdirs();
		for (Classifier model:attributeClassifiersHintFree) {
			classifierFile = new File(String.format("%s/hintFree/attributes/%s/%s.txt", folderPath, model.getName(), model.getName()));
			classifierFile.getParentFile().mkdirs();
			classifierFile.createNewFile();
			writer = new BufferedWriter(new FileWriter(classifierFile));
			writer.write(((GBTClassificationModel) model.getModel()).toDebugString());
			writer.close();
		}

		new File(String.format("%s/hintFree/records/", folderPath)).mkdirs();
		for (Classifier model:recordClassifiersHintFree) {
			classifierFile = new File(String.format("%s/hintFree/records/%s/%s.txt", folderPath, model.getName(), model.getName()));
			classifierFile.getParentFile().mkdirs();
			classifierFile.createNewFile();
			writer = new BufferedWriter(new FileWriter(classifierFile));
			writer.write(((GBTClassificationModel) model.getModel()).toDebugString());
			writer.close();
		}
	}


	@Override
	public GBTClassificationModel loadClassifier(String path) {return GBTClassificationModel.load(path); }

	@Override
	public Classifier createClassifier() {
		return new ClassifierGBT();
	}

	@Override
	public void saveClassifier(ClassificationModel classifier, String path) throws IOException {
		((GBTClassificationModel)classifier).save(path);
	}

	protected GBTClassificationModel trainBinaryClassifier(String trainingFilePath, String slotClass) {
		assert context != null;
		assert trainingFilePath != null;
		assert slotClass != null;

		String lossType;
		Integer maxBins;
		Integer maxDepth;
		Double minInfoGain;
		Integer minInstancesPerNode;
		Double subSamplingRate;
		Double stepSize;
		Boolean cacheNodesId;
		Integer checkPointInterval;
		String impurity;
		String featureSubsetStrategy;
		Integer seed;
		Integer maxMemoryInMB;


		final GBTClassificationModel result;
		GBTClassifier trainer;
		JavaRDD<String> csv;
		Dataset<Row> trainingData;

		csv = context.textFile(trainingFilePath);
		trainingData = csvToDataset(csv, slotClass);
		trainer = new GBTClassifier().setLabelCol("label").setFeaturesCol("features");

		if(params.containsKey("stepSize")){
			stepSize = (Double) params.get("stepSize");
			trainer = trainer.setStepSize(stepSize);
		}

		if(params.containsKey("seed")){
			seed = (Integer) params.get("seed");
			trainer = trainer.setSeed(seed);
		}

		if(params.containsKey("maxBins")){
			maxBins = (Integer) params.get("maxBins");
			trainer = trainer.setMaxBins(maxBins);
		}

		if(params.containsKey("maxDepth")){
			maxDepth = (Integer) params.get("maxDepth");
			trainer = trainer.setMaxDepth(maxDepth);
		}

		if(params.containsKey("lossType")){
			lossType = (String) params.get("lossType");
			trainer = trainer.setLossType(lossType);
		}

		if(params.containsKey("minInfoGain")){
			minInfoGain = (Double) params.get("minInfoGain");
			trainer = trainer.setMinInfoGain(minInfoGain);
		}

		if(params.containsKey("minInstancesPerNode")){
			minInstancesPerNode = (Integer) params.get("minInstancesPerNode");
			trainer = trainer.setMinInstancesPerNode(minInstancesPerNode);
		}

		if(params.containsKey("subSamplingRate")){
			subSamplingRate = (Double) params.get("subSamplingRate");
			trainer = trainer.setSubsamplingRate(subSamplingRate);
		}

		if(params.containsKey("cacheNodesId")){
			cacheNodesId = (Boolean) params.get("cacheNodesId");
			trainer = trainer.setCacheNodeIds(cacheNodesId);
		}

		if(params.containsKey("checkPointInterval")){
			checkPointInterval = (Integer) params.get("checkPointInterval");
			trainer = trainer.setCheckpointInterval(checkPointInterval);
		}

		if(params.containsKey("impurity")){
			impurity = (String) params.get("impurity");
			trainer = trainer.setImpurity(impurity);
		}

		if(params.containsKey("maxMemoryInMB")){
			maxMemoryInMB = (Integer) params.get("maxMemoryInMB");
			trainer = trainer.setMaxMemoryInMB(maxMemoryInMB);
		}

		if(params.containsKey("featureSubsetStrategy")){
			featureSubsetStrategy = (String) params.get("featureSubsetStrategy");
			trainer = trainer.setFeatureSubsetStrategy(featureSubsetStrategy);
		}

		System.out.println(String.format("Training classifier for class %s", slotClass));
		result = trainer.fit(trainingData);
		System.out.println(result.toString());
		return result;
	}
}