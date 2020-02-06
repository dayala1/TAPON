package model.MP;

import model.Classifier;
import org.apache.spark.ml.classification.GBTClassificationModel;
import org.apache.spark.ml.classification.MultilayerPerceptronClassificationModel;

public class ClassifierMP implements Classifier<MultilayerPerceptronClassificationModel> {
	
	private static final long serialVersionUID = 4966109125913354099L;

	//Properties-----------------------------------------------------
	private MultilayerPerceptronClassificationModel model;
	private String name;
	public MultilayerPerceptronClassificationModel getModel() {
		return model;
	}
	public void setModel(MultilayerPerceptronClassificationModel model) {
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
