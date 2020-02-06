package model.naiveBayes;

import model.Classifier;
import org.apache.spark.ml.classification.GBTClassificationModel;
import org.apache.spark.ml.classification.NaiveBayesModel;

public class ClassifierNaiveBayes implements Classifier<NaiveBayesModel> {
	
	private static final long serialVersionUID = 4966109125913354099L;

	//Properties-----------------------------------------------------
	private NaiveBayesModel model;
	private String name;
	public NaiveBayesModel getModel() {
		return model;
	}
	public void setModel(NaiveBayesModel model) {
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
