package model.logisticRegression;

import model.Classifier;
import org.apache.spark.ml.classification.LogisticRegressionModel;
import org.apache.spark.ml.classification.RandomForestClassificationModel;

public class ClassifierLogisticRegression implements Classifier<LogisticRegressionModel> {
	
	private static final long serialVersionUID = 4966109125913354099L;

	//Properties-----------------------------------------------------
	private LogisticRegressionModel model;
	private String name;
	public LogisticRegressionModel getModel() {
		return model;
	}
	public void setModel(LogisticRegressionModel model) {
		assert model != null;
		this.model = model;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		assert name != null;
	
		this.name = name;
	}
	
	//Internal state-------------------------------------------------

	//Interface methods----------------------------------------------

	//Ancillary methods----------------------------------------------
}
