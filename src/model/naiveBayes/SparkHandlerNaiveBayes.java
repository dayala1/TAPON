package model.naiveBayes;

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

public class SparkHandlerNaiveBayes extends SparkHandler {
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
			writer.write(((NaiveBayesModel) model.getModel()).toString());
			writer.close();
		}

		new File(String.format("%s/hintBased/records/", folderPath)).mkdirs();
		for (Classifier model:recordClassifiersHintBased) {
			classifierFile = new File(String.format("%s/hintBased/records/%s/%s.txt", folderPath, model.getName(), model.getName()));
			classifierFile.getParentFile().mkdirs();
			classifierFile.createNewFile();
			writer = new BufferedWriter(new FileWriter(classifierFile));
			writer.write(((NaiveBayesModel) model.getModel()).toString());
			writer.close();
		}

		new File(String.format("%s/hintFree/attributes/", folderPath)).mkdirs();
		for (Classifier model:attributeClassifiersHintFree) {
			classifierFile = new File(String.format("%s/hintFree/attributes/%s/%s.txt", folderPath, model.getName(), model.getName()));
			classifierFile.getParentFile().mkdirs();
			classifierFile.createNewFile();
			writer = new BufferedWriter(new FileWriter(classifierFile));
			writer.write(((NaiveBayesModel) model.getModel()).toString());
			writer.close();
		}

		new File(String.format("%s/hintFree/records/", folderPath)).mkdirs();
		for (Classifier model:recordClassifiersHintFree) {
			classifierFile = new File(String.format("%s/hintFree/records/%s/%s.txt", folderPath, model.getName(), model.getName()));
			classifierFile.getParentFile().mkdirs();
			classifierFile.createNewFile();
			writer = new BufferedWriter(new FileWriter(classifierFile));
			writer.write(((NaiveBayesModel) model.getModel()).toString());
			writer.close();
		}
	}


	@Override
	public NaiveBayesModel loadClassifier(String path) {return NaiveBayesModel.load(path); }

	@Override
	public Classifier createClassifier() {
		return new ClassifierNaiveBayes();
	}

	@Override
	public void saveClassifier(ClassificationModel classifier, String path) throws IOException {
		((NaiveBayesModel)classifier).save(path);
	}

	protected NaiveBayesModel trainBinaryClassifier(String trainingFilePath, String slotClass) {
		assert context != null;
		assert trainingFilePath != null;
		assert slotClass != null;

		String modelType;
		Double smoothing;
		String weightCol;

		final NaiveBayesModel result;
		NaiveBayes trainer;
		JavaRDD<String> csv;
		Dataset<Row> trainingData;

		csv = context.textFile(trainingFilePath);
		trainingData = csvToDataset(csv, slotClass);
		trainer = new NaiveBayes().setLabelCol("label").setFeaturesCol("features");

		if(params.containsKey("modelType")){
			modelType = (String) params.get("modelType");
			trainer = trainer.setModelType(modelType);
		}

		if(params.containsKey("smoothing")){
			smoothing = (Double) params.get("smoothing");
			trainer = trainer.setSmoothing(smoothing);
		}

		if(params.containsKey("weightCol")){
			weightCol = (String) params.get("weightCol");
			trainer = trainer.setWeightCol(weightCol);
		}

		System.out.println(String.format("Training classifier for class %s", slotClass));
		result = trainer.fit(trainingData);
		System.out.println(result.toString());
		return result;
	}
}