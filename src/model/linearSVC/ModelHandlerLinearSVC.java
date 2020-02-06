package model.linearSVC;

import model.ModelHandler;
import model.SparkHandler;
import model.logisticRegression.SparkHandlerLogisticRegression;

import java.util.Map;

public class ModelHandlerLinearSVC extends ModelHandler {
	// Constructors---------------------------------------------------

    @Override
    public SparkHandler initializeSparkHandler() {
        return new SparkHandlerLinearSVC();
    }

	// Interface methods----------------------------------------------

	protected void setParams(Map<String, String> params) {
		assert sparkHandler != null;
		assert params != null;
		sparkHandler.setParam("maxIter", Integer.valueOf(params.getOrDefault("maxIter", "20")));
        sparkHandler.setParam("tol", Double.valueOf(params.getOrDefault("tol", "0.0001")));
        sparkHandler.setParam("aggregationDepth", Integer.valueOf(params.getOrDefault("aggregationDepth", "2")));
	}

}
