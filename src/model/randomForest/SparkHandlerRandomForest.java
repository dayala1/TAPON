package model.randomForest;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import model.Classifier;
import model.SparkHandler;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.ml.classification.*;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import utils.RandomForestParsing.RandomForestParser;
import utils.RandomForestParsing.Tree;

public class SparkHandlerRandomForest extends SparkHandler {
	private static final long serialVersionUID = 4424575592980018563L;

	// Interface methods----------------------------------------------


	public void writeClassifiersString(String folderPath) throws IOException {
		File classifierFile;
		BufferedWriter writer;

		for (model.Classifier model:attributeClassifiersHintBased) {
			classifierFile = new File(String.format("%s/hintBased/attributes/%s/%s.txt", folderPath, model.getName(), model.getName()));
			classifierFile.getParentFile().mkdirs();
			classifierFile.createNewFile();
			writer = new BufferedWriter(new FileWriter(classifierFile));
			writer.write(((RandomForestClassificationModel)model.getModel()).toDebugString());
			writer.close();

			//Trees parsing
			List<javafx.util.Pair<String, List<String>>> treeStrings;
			treeStrings = RandomForestParser.separateTrees(classifierFile);
			List<Tree> trees = treeStrings.stream().map(t -> new Tree(t, featureNamesAttributesHB)).collect(Collectors.toList());
			RandomForestParser.makeTreeViz(trees, classifierFile.getParent());
		}

		new File(String.format("%s/hintBased/records/", folderPath)).mkdirs();
		for (model.Classifier model:recordClassifiersHintBased) {
			classifierFile = new File(String.format("%s/hintBased/records/%s/%s.txt", folderPath, model.getName(), model.getName()));
			classifierFile.getParentFile().mkdirs();
			classifierFile.createNewFile();
			writer = new BufferedWriter(new FileWriter(classifierFile));
			writer.write(((RandomForestClassificationModel)model.getModel()).toDebugString());
			writer.close();

			//Trees parsing
			List<javafx.util.Pair<String, List<String>>> treeStrings;
			treeStrings = RandomForestParser.separateTrees(classifierFile);
			List<Tree> trees = treeStrings.stream().map(t -> new Tree(t, featureNamesRecordsHB)).collect(Collectors.toList());
			RandomForestParser.makeTreeViz(trees, classifierFile.getParent());
		}

		new File(String.format("%s/hintFree/attributes/", folderPath)).mkdirs();
		for (model.Classifier model:attributeClassifiersHintFree) {
			classifierFile = new File(String.format("%s/hintFree/attributes/%s/%s.txt", folderPath, model.getName(), model.getName()));
			classifierFile.getParentFile().mkdirs();
			classifierFile.createNewFile();
			writer = new BufferedWriter(new FileWriter(classifierFile));
			writer.write(((RandomForestClassificationModel)model.getModel()).toDebugString());
			writer.close();

			//Trees parsing
			List<javafx.util.Pair<String, List<String>>> treeStrings;
			treeStrings = RandomForestParser.separateTrees(classifierFile);
			List<Tree> trees = treeStrings.stream().map(t -> new Tree(t, featureNamesAttributesHF)).collect(Collectors.toList());
			RandomForestParser.makeTreeViz(trees, classifierFile.getParent());
		}

		new File(String.format("%s/hintFree/records/", folderPath)).mkdirs();
		for (Classifier model:recordClassifiersHintFree) {
			classifierFile = new File(String.format("%s/hintFree/records/%s/%s.txt", folderPath, model.getName(), model.getName()));
			classifierFile.getParentFile().mkdirs();
			classifierFile.createNewFile();
			writer = new BufferedWriter(new FileWriter(classifierFile));
			writer.write(((RandomForestClassificationModel)model.getModel()).toDebugString());
			writer.close();

			//Trees parsing
			List<javafx.util.Pair<String, List<String>>> treeStrings;
			treeStrings = RandomForestParser.separateTrees(classifierFile);
			List<Tree> trees = treeStrings.stream().map(t -> new Tree(t, featureNamesRecordsHF)).collect(Collectors.toList());
			RandomForestParser.makeTreeViz(trees, classifierFile.getParent());
		}
	}


	@Override
	public ClassificationModel loadClassifier(String path) {
		return RandomForestClassificationModel.load(path);
	}

	@Override
	public Classifier createClassifier() {
		return new ClassifierRandomForest();
	}

	@Override
	public void saveClassifier(ClassificationModel classifier, String path) throws IOException {
		((RandomForestClassificationModel)classifier).save(path);
	}

	// Ancillary methods----------------------------------------------

	protected RandomForestClassificationModel trainBinaryClassifier(String trainingFilePath, String slotClass) {
		assert context != null;
		assert trainingFilePath != null;
		assert slotClass != null;

		Integer numTrees;
		Integer maxBins;
		Integer maxDepth;
		Double minInfoGain;
		Integer minInstancesPerNode;
		Double subSamplingRate;
		Boolean cacheNodesId;
		Integer checkPointInterval;
		String impurity;
		String featureSubsetStrategy;
		Integer seed;


		final RandomForestClassificationModel result;
		RandomForestClassifier trainer;
		JavaRDD<String> csv;
		Dataset<Row> trainingData;

		csv = context.textFile(trainingFilePath);
		trainingData = csvToDataset(csv, slotClass);
		trainer = new RandomForestClassifier().setLabelCol("label").setFeaturesCol("features");

		if(params.containsKey("numTrees")){
			numTrees = (Integer) params.get("numTrees");
			trainer = trainer.setNumTrees(numTrees);
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

		if(params.containsKey("numTrees")){
			numTrees = (Integer) params.get("numTrees");
			trainer = trainer.setNumTrees(numTrees);
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

		if(params.containsKey("featureSubsetStrategy")){
			featureSubsetStrategy = (String) params.get("featureSubsetStrategy");
			trainer = trainer.setFeatureSubsetStrategy(featureSubsetStrategy);
		}

		System.out.println(String.format("Training classifier for class %s", slotClass));
		System.out.println(trainingData);
		result = trainer.fit(trainingData);
		System.out.println(result.toString());
		return result;
	}
}