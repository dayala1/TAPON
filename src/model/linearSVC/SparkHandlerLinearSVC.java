package model.linearSVC;

import com.google.common.primitives.Doubles;
import model.Classifier;
import model.SparkHandler;
import model.logisticRegression.ClassifierLogisticRegression;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.ml.classification.*;
import org.apache.spark.ml.linalg.Matrix;
import org.apache.spark.ml.linalg.Vector;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class SparkHandlerLinearSVC extends SparkHandler {
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
			writer.write(((LinearSVCModel) model.getModel()).toString());
			writer.close();
		}

		new File(String.format("%s/hintBased/records/", folderPath)).mkdirs();
		for (Classifier model:recordClassifiersHintBased) {
			classifierFile = new File(String.format("%s/hintBased/records/%s/%s.txt", folderPath, model.getName(), model.getName()));
			classifierFile.getParentFile().mkdirs();
			classifierFile.createNewFile();
			writer = new BufferedWriter(new FileWriter(classifierFile));
			writer.write(((LinearSVCModel) model.getModel()).toString());
			writer.close();
		}

		new File(String.format("%s/hintFree/attributes/", folderPath)).mkdirs();
		for (Classifier model:attributeClassifiersHintFree) {
			classifierFile = new File(String.format("%s/hintFree/attributes/%s/%s.txt", folderPath, model.getName(), model.getName()));
			classifierFile.getParentFile().mkdirs();
			classifierFile.createNewFile();
			writer = new BufferedWriter(new FileWriter(classifierFile));
			writer.write(((LinearSVCModel) model.getModel()).toString());
			writer.close();
		}

		new File(String.format("%s/hintFree/records/", folderPath)).mkdirs();
		for (Classifier model:recordClassifiersHintFree) {
			classifierFile = new File(String.format("%s/hintFree/records/%s/%s.txt", folderPath, model.getName(), model.getName()));
			classifierFile.getParentFile().mkdirs();
			classifierFile.createNewFile();
			writer = new BufferedWriter(new FileWriter(classifierFile));
			writer.write(((LinearSVCModel) model.getModel()).toString());
			writer.close();
		}
	}


	@Override
	public LinearSVCModel loadClassifier(String path) {return LinearSVCModel.load(path); }

	@Override
	public Classifier createClassifier() {
		return new ClassifierLinearSVC();
	}

	@Override
	public void saveClassifier(ClassificationModel classifier, String path) throws IOException {
		((LinearSVCModel)classifier).save(path);
	}

	// Ancillary methods----------------------------------------------

	protected LinearSVCModel trainBinaryClassifier(String trainingFilePath, String slotClass) {
		assert context != null;
		assert trainingFilePath != null;
		assert slotClass != null;

		Integer maxIter;
		Integer aggregationDepth;
		Boolean fitIntercept;
		Double regParam;
		Boolean standardization;
		Double threshold;
		Double tol;
		String weightCol;


		final LinearSVCModel result;
		LinearSVC trainer;
		JavaRDD<String> csv;
		Dataset<Row> trainingData;
		csv = context.textFile(trainingFilePath);
		trainingData = csvToDataset(csv, slotClass);
		trainer = new LinearSVC().setLabelCol("label").setFeaturesCol("features");
		if(params.containsKey("maxIter")){
			maxIter = (Integer) params.get("maxIter");
			trainer = trainer.setMaxIter(maxIter);
		}

		if(params.containsKey("aggregationDepth")){
			aggregationDepth = (Integer) params.get("aggregationDepth");
			trainer = trainer.setAggregationDepth(aggregationDepth);
		}

		if(params.containsKey("fitIntercept")){
			fitIntercept = (Boolean) params.get("fitIntercept");
			trainer = trainer.setFitIntercept(fitIntercept);
		}

		if(params.containsKey("regParam")){
			regParam = (Double) params.get("regParam");
			trainer = trainer.setRegParam(regParam);
		}

		if(params.containsKey("standardization")){
			standardization = (Boolean) params.get("standardization");
			trainer = trainer.setStandardization(standardization);
		}

		if(params.containsKey("threshold")){
			threshold = (Double) params.get("threshold");
			trainer = trainer.setThreshold(threshold);
		}

		if(params.containsKey("tol")){
			tol = (Double) params.get("tol");
			trainer = trainer.setTol(tol);
		}

		if(params.containsKey("weightCol")){
			weightCol = (String) params.get("weightCol");
			trainer = trainer.setWeightCol(weightCol);
		}

		System.out.println(String.format("Training linear SVC classifier for class %s", slotClass));
		result = trainer.fit(trainingData);
		System.out.println(result.toString());
		return result;
	}
}