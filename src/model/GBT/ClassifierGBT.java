package model.GBT;

import model.Classifier;
import org.apache.spark.ml.classification.GBTClassificationModel;
import org.apache.spark.ml.classification.LinearSVCModel;

public class ClassifierGBT implements Classifier<GBTClassificationModel> {
	
	private static final long serialVersionUID = 4966109125913354099L;

	//Properties-----------------------------------------------------
	private GBTClassificationModel model;
	private String name;
	public GBTClassificationModel getModel() {
		return model;
	}
	public void setModel(GBTClassificationModel model) {
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
