package model.logisticRegression;

import model.ModelHandler;
import model.SparkHandler;
import model.randomForest.SparkHandlerRandomForest;

import java.util.Map;

public class ModelHandlerLogisticRegression extends ModelHandler {
	// Constructors---------------------------------------------------

    @Override
    public SparkHandler initializeSparkHandler() {
        return new SparkHandlerLogisticRegression();
    }

	// Interface methods----------------------------------------------

	protected void setParams(Map<String, String> params) {
		assert sparkHandler != null;
		assert params != null;

        //sparkHandler.setParam("tol", Double.valueOf(params.getOrDefault("tol", "0.1")));
        //sparkHandler.setParam("fitIntercept", Boolean.valueOf(params.getOrDefault("fitIntercept", "true")));
        //sparkHandler.setParam("maxIter", Integer.valueOf(params.getOrDefault("maxIter", "50")));
        //sparkHandler.setParam("regParam", Double.valueOf(params.getOrDefault("regParam", "0.3")));
        //sparkHandler.setParam("elasticNetParam", Double.valueOf(params.getOrDefault("elasticNetParam", "0.8")));
	}

}
