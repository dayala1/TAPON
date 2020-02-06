package main;
import java.util.*;

import dataset.Dataset;
import dataset.Record;
import dataset.Slot;
import model.ModelHandler;
import model.logisticRegression.ModelHandlerLogisticRegression;
import model.randomForest.ModelHandlerRandomForest;
import utils.ClockMonitor;
import utils.DatasetReader;
import utils.FileUtilsCust;

public class ModelApplicationDriver {
	
	public static double incorrect = 0.0;
	public static double correct = 0.0;

	public static void main(String[] args) throws Exception {
		ClockMonitor clock;
		
		//MODEL CREATION
		Dataset dataset;
		
		//MODEL APPLICATION
		List<Dataset> trainingDatasets;
		List<Dataset> testingDatasets;
		DatasetReader datasetReader;
		String classifiersTablesRoot;
		String resultsRoot;
		String datasetsRoot;
		String[] domains;
		int testingFoldNumber;
		Integer numberOfDomains;
		String datasetsPath;
		ModelHandler modelHandler;
		String resultsPath;
		Boolean useMulticlass;
		clock = new ClockMonitor();


		//MODEL APPLICATION
		datasetReader = new DatasetReader();
		testingDatasets = new ArrayList<Dataset>();
		modelHandler = new ModelHandlerRandomForest();
		
		domains = new String[]{"Awards-end-2017-without-abstract-summary"};
		classifiersTablesRoot = "E:/model/FullResults/9-folds/0/classifiersAndTables";
		resultsRoot = "E:/model/FullResults/9-folds/0";
		datasetsRoot = "E:/Documents/US/Tesis/datasets_hidden";
		useMulticlass = true;
		Set<Integer> testingDomains = new HashSet<>();
		testingDomains.add(1);
		//testingDomains.add(2);
		//testingDomains.add(3);
		//testingDomains.add(4);
		//testingDomains.add(5);
		//testingDomains.add(6);
		//testingDomains.add(7);
		//testingDomains.add(8);
		//testingDomains.add(9);
		//testingDomains.add(10);

		System.out.println("Loading model");
		modelHandler.setClassifiersRootFolder(String.format("%s/modelClassifiers", classifiersTablesRoot));
		modelHandler.setTablesRootFolder(String.format("%s/modelTables", classifiersTablesRoot));
		modelHandler.loadFeaturesCalculators();
		modelHandler.createNewContext();
		modelHandler.loadClassifiers(false);
		modelHandler.loadClassifiers(true);

		System.out.println("Loading datasets");
		for (String domain : domains) {
			for (int i = 1; i <= 10; i++) {
				if(testingDomains.contains(i)) {
					datasetsPath = String.format("%s/%s/%s", datasetsRoot, domain, i);
					datasetReader.addDataset(datasetsPath, 1.0, testingDatasets);
				}
			}
		}
		
		resultsPath = String.format("%s/results", resultsRoot);

		System.out.println("Starting testing");
		clock.start();
		//testingDatasets = testingDatasets.subList(0,500);
		for (Dataset testingDataset : testingDatasets) {
			modelHandler.refineHintsUnlabelledDataset(testingDataset, useMulticlass);
			checkHints(testingDataset);
			modelHandler.saveResults(testingDataset, String.format("%s/results/1-iterations", resultsPath));
			modelHandler.refineHintsOnce(testingDataset);
			checkHints(testingDataset);
			System.out.println("Finished dataset");
			modelHandler.saveResults(testingDataset, String.format("%s/results/2-iterations", resultsPath));
		}
		clock.stop();
		FileUtilsCust.createCSV(String.format("%s/results/1-iterations/applicationTime.csv", resultsPath));
		FileUtilsCust.addLine(String.format("%s/results/1-iterations/applicationTime.csv", resultsPath), clock.getCPUTime());
		modelHandler.resetFolderCount();
		for (int i = 0; i < 0; i++) {
			for (Dataset testingDataset : testingDatasets) {
				modelHandler.refineHintsOnce(testingDataset);
				checkHints(testingDataset);
				System.out.println("");
				//System.out.println("finished dataset");
				modelHandler.saveResults(testingDataset, String.format("%s/results/%s-iterations", resultsPath, i+2));
			}
			clock.stop();
			FileUtilsCust.createCSV(String.format("%s/results/%s-iterations/applicationTime.csv", resultsPath, i+2));
			FileUtilsCust.addLine(String.format("%s/results/%s-iterations/applicationTime.csv", resultsPath, i+2), clock.getCPUTime());
			modelHandler.resetFolderCount();
		}
		/*
		//CONVERGENCE
		for (Dataset testingDataset : testingDatasets) {
			modelHandler.classifySlotsUntilConvergence(testingDataset, 7);
			checkHints(testingDataset);
			//System.out.println("finished dataset");
			modelHandler.saveResults(testingDataset, String.format("%s/results/%s-domains/fold-%s/", resultsPath, numberOfDomains, testingFoldNumber));
		}
		*/
		
		modelHandler.closeContext();
	}
	
	public static void checkHints(Dataset dataset) {
		assert dataset != null;
		List<Slot> children;
		
		children = dataset.getSlots();
		for (Slot child : children) {
			checkHints(child);
		}
	}
	
	public static void checkHints(Slot slot) {
		assert slot != null;
		List<Slot> children;
		
		//System.out.println(String.format("Slot of class %s classified as %s", slot.getSlotClass(), slot.getHint()));
		
		if(slot.getSlotClass().equals(slot.getHint())) {
			correct++;
		} else {
			incorrect++;
		}
		
		if (slot instanceof Record) {
			children = ((Record)slot).getSlots();
			for (Slot child : children) {
				checkHints(child);
			}
		}
	}
	
}
