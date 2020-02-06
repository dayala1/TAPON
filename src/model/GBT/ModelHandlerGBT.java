package model.GBT;

import model.ModelHandler;
import model.SparkHandler;

import java.util.Map;

public class ModelHandlerGBT extends ModelHandler {
	// Constructors---------------------------------------------------

    @Override
    public SparkHandler initializeSparkHandler() {
        return new SparkHandlerGBT();
    }

	// Interface methods----------------------------------------------

	protected void setParams(Map<String, String> params) {
		assert sparkHandler != null;
		assert params != null;
        sparkHandler.setParam("maxIter", Integer.valueOf(params.getOrDefault("maxIter", "20")));
	}

}
