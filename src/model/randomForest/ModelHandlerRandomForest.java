package model.randomForest;

import java.util.*;

import model.ModelHandler;
import model.SparkHandler;

public class ModelHandlerRandomForest extends ModelHandler {
	// Constructors---------------------------------------------------

    @Override
    public SparkHandler initializeSparkHandler() {
        return new SparkHandlerRandomForest();
    }

	// Interface methods----------------------------------------------

	protected void setParams(Map<String, String> params) {
		assert sparkHandler != null;
		assert params != null;

		sparkHandler.setParam("numTrees", Integer.valueOf(params.getOrDefault("numTrees", "20")));
		sparkHandler.setParam("maxBins", Integer.valueOf(params.getOrDefault("maxBins", "32")));
		sparkHandler.setParam("maxDepth", Integer.valueOf(params.getOrDefault("maxDepth", "5")));
	}

}
