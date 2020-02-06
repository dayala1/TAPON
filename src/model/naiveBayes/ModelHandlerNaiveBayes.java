package model.naiveBayes;

import model.ModelHandler;
import model.SparkHandler;

import java.util.Map;

public class ModelHandlerNaiveBayes extends ModelHandler {
	// Constructors---------------------------------------------------

    @Override
    public SparkHandler initializeSparkHandler() {
        return new SparkHandlerNaiveBayes();
    }

	// Interface methods----------------------------------------------

	protected void setParams(Map<String, String> params) {
		assert sparkHandler != null;
		assert params != null;
	}

}
