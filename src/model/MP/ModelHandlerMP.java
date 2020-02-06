package model.MP;

import model.ModelHandler;
import model.SparkHandler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ModelHandlerMP extends ModelHandler {
	// Constructors---------------------------------------------------

    @Override
    public SparkHandler initializeSparkHandler() {
        return new SparkHandlerMP();
    }

	// Interface methods----------------------------------------------

	protected void setParams(Map<String, String> params) {
        List<Integer> layers = new ArrayList<Integer>();
		assert sparkHandler != null;
		assert params != null;
        FileReader reader;
        BufferedReader bufferedReader;
        String firstLine;
	}

}
