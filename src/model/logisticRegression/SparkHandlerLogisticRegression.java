package model.logisticRegression;

import com.google.common.primitives.Doubles;
import model.Classifier;
import model.SparkHandler;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.ml.classification.*;
import org.apache.spark.ml.linalg.Matrix;
import org.apache.spark.ml.linalg.Vector;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import utils.RandomForestParsing.RandomForestParser;
import utils.RandomForestParsing.Tree;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class SparkHandlerLogisticRegression extends SparkHandler {
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
			writer.write(model.toString());
			writer.write(((LogisticRegressionModel) model.getModel()).coefficientMatrix().toString());
			writer.close();
		}

		new File(String.format("%s/hintBased/records/", folderPath)).mkdirs();
		for (Classifier model:recordClassifiersHintBased) {
			classifierFile = new File(String.format("%s/hintBased/records/%s/%s.txt", folderPath, model.getName(), model.getName()));
			classifierFile.getParentFile().mkdirs();
			classifierFile.createNewFile();
			writer = new BufferedWriter(new FileWriter(classifierFile));
			writer.write(model.toString());
			writer.write(((LogisticRegressionModel) model.getModel()).coefficientMatrix().toString());
			writer.close();
		}

		new File(String.format("%s/hintFree/attributes/", folderPath)).mkdirs();
		for (Classifier model:attributeClassifiersHintFree) {
			classifierFile = new File(String.format("%s/hintFree/attributes/%s/%s.txt", folderPath, model.getName(), model.getName()));
			classifierFile.getParentFile().mkdirs();
			classifierFile.createNewFile();
			writer = new BufferedWriter(new FileWriter(classifierFile));
			writer.write(model.toString());
			writer.write(((LogisticRegressionModel) model.getModel()).coefficientMatrix().toString());
			writer.close();
		}

		new File(String.format("%s/hintFree/records/", folderPath)).mkdirs();
		for (Classifier model:recordClassifiersHintFree) {
			classifierFile = new File(String.format("%s/hintFree/records/%s/%s.txt", folderPath, model.getName(), model.getName()));
			classifierFile.getParentFile().mkdirs();
			classifierFile.createNewFile();
			writer = new BufferedWriter(new FileWriter(classifierFile));
			writer.write(model.toString());
			writer.write(((LogisticRegressionModel) model.getModel()).coefficientMatrix().toString());
			writer.close();
		}
	}


	@Override
	public ClassificationModel loadClassifier(String path) {
		return LogisticRegressionModel.load(path);
	}

	@Override
	public Classifier createClassifier() {
		return new ClassifierLogisticRegression();
	}

	@Override
	public void saveClassifier(ClassificationModel classifier, String path) throws IOException {
		((LogisticRegressionModel)classifier).save(path);
	}

	// Ancillary methods----------------------------------------------

	protected LogisticRegressionModel trainBinaryClassifier(String trainingFilePath, String slotClass) {
		assert context != null;
		assert trainingFilePath != null;
		assert slotClass != null;

		Integer maxIter;
		Integer aggregationDepth;
		Double elasticNetParam;
		String family;
		Boolean fitIntercept;
		Double regParam;
		LogisticRegressionModel initialModel;
		Matrix lowerBoundsOnCoefficients;
		Vector lowerBoundsOnIntercepts;
		Matrix upperBoundsOnCoefficients;
		Vector upperBoundsOnIntercepts;
		Boolean standardization;
		Double threshold;
		List<Double> thresholds;
		Double tol;
		String weightCol;


		final LogisticRegressionModel result;
		LogisticRegression trainer;
		JavaRDD<String> csv;
		Dataset<Row> trainingData;
		csv = context.textFile(trainingFilePath);
		trainingData = csvToDataset(csv, slotClass);
		trainer = new LogisticRegression().setLabelCol("label").setFeaturesCol("features");
		if(params.containsKey("maxIter")){
			maxIter = (Integer) params.get("maxIter");
			trainer = trainer.setMaxIter(maxIter);
		}

		if(params.containsKey("elasticNetParam")){
			elasticNetParam = (Double) params.get("elasticNetParam");
			trainer = trainer.setElasticNetParam(elasticNetParam);
		}

		if(params.containsKey("family")){
			family = (String) params.get("family");
			trainer = trainer.setFamily(family);
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

		if(params.containsKey("initialModel")){
			initialModel = (LogisticRegressionModel) params.get("initialModel");
			trainer = trainer.setInitialModel(initialModel);
		}

		if(params.containsKey("lowerBoundsOnCoefficients")){
			lowerBoundsOnCoefficients = (Matrix) params.get("lowerBoundsOnCoefficients");
			trainer = trainer.setLowerBoundsOnCoefficients(lowerBoundsOnCoefficients);
		}

		if(params.containsKey("lowerBoundsOnIntercepts")){
			lowerBoundsOnIntercepts = (Vector) params.get("lowerBoundsOnIntercepts");
			trainer = trainer.setLowerBoundsOnIntercepts(lowerBoundsOnIntercepts);
		}

		if(params.containsKey("upperBoundsOnCoefficients")){
			upperBoundsOnCoefficients = (Matrix) params.get("upperBoundsOnCoefficients");
			trainer = trainer.setUpperBoundsOnCoefficients(upperBoundsOnCoefficients);
		}

		if(params.containsKey("upperBoundsOnIntercepts")){
			upperBoundsOnIntercepts = (Vector) params.get("upperBoundsOnIntercepts");
			trainer = trainer.setUpperBoundsOnIntercepts(upperBoundsOnIntercepts);
		}

		if(params.containsKey("standardization")){
			standardization = (Boolean) params.get("standardization");
			trainer = trainer.setStandardization(standardization);
		}

		if(params.containsKey("threshold")){
			threshold = (Double) params.get("threshold");
			trainer = trainer.setThreshold(threshold);
		}

		if(params.containsKey("thresholds")){
			thresholds = (List<Double>) params.get("thresholds");
			trainer = trainer.setThresholds(Doubles.toArray(thresholds));
		}

		if(params.containsKey("tol")){
			tol = (Double) params.get("tol");
			trainer = trainer.setTol(tol);
		}

		if(params.containsKey("weightCol")){
			weightCol = (String) params.get("weightCol");
			trainer = trainer.setWeightCol(weightCol);
		}

		System.out.println(String.format("Training logistic regression classifier for class %s", slotClass));
		result = trainer.fit(trainingData);
		System.out.println(result.toString());
		return result;
	}
}