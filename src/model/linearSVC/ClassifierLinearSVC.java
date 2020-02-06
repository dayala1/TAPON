package model.linearSVC;

import model.Classifier;
import org.apache.spark.ml.classification.LinearSVC;
import org.apache.spark.ml.classification.LinearSVCModel;
import org.apache.spark.mllib.classification.SVMModel;

public class ClassifierLinearSVC implements Classifier<LinearSVCModel> {
	
	private static final long serialVersionUID = 4966109125913354099L;

	//Properties-----------------------------------------------------
	private LinearSVCModel model;
	private String name;
	public LinearSVCModel getModel() {
		return model;
	}
	public void setModel(LinearSVCModel model) {
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
