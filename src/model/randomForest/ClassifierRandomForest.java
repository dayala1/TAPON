package model.randomForest;

import java.io.Serializable;

import org.apache.spark.ml.classification.RandomForestClassificationModel;

public class ClassifierRandomForest implements Serializable{
	
	private static final long serialVersionUID = 4966109125913354099L;

	//Properties-----------------------------------------------------
	private RandomForestClassificationModel model;
	private String name;
	public RandomForestClassificationModel getModel() {
		return model;
	}
	public void setModel(RandomForestClassificationModel model) {
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
