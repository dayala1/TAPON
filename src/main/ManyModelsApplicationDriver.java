package main;

import dataset.Dataset;
import dataset.Record;
import dataset.Slot;
import model.GBT.ModelHandlerGBT;
import model.ModelHandler;
import model.linearSVC.ModelHandlerLinearSVC;
import model.logisticRegression.ModelHandlerLogisticRegression;
import model.randomForest.ModelHandlerRandomForest;
import utils.ClockMonitor;
import utils.DatasetReader;
import utils.FileUtilsCust;
import utils.LuceneUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ManyModelsApplicationDriver {
	
	public static double incorrect = 0.0;
	public static double correct = 0.0;

	public static void main(String[] args) throws Exception {
		ClockMonitor clock;
		
		//MODEL CREATION
		Dataset dataset;
		
		//MODEL APPLICATION
		Set<Integer> testingFolds;
		List<Dataset> testingDatasets;
		DatasetReader datasetReader;
		String classifiersTablesRoot;
		String resultsRoot;
		String datasetsRoot;
		String[] domains;
		String datasetsPath;
		ModelHandler modelHandler;
		String resultsPath;
		Boolean useMulticlass;
		clock = new ClockMonitor();
		Integer numberOfFolds;
		
		
		//MODEL APPLICATION
		datasetReader = new DatasetReader();
		testingDatasets = new ArrayList<Dataset>();
		modelHandler = new ModelHandlerRandomForest();
		//LuceneUtils.loadIdfMap("E:/model/testsSoruHintBased/idfMap");
		domains = new String[]{"TAPON-MT"};
		classifiersTablesRoot = "E:/model/testJSS";
		resultsRoot = "E:/model/testJSS";
		datasetsRoot = "E:/Documents/US/Tesis/datasets";
		useMulticlass = true;
		numberOfFolds = 9;
		modelHandler.createNewContext();
		for (int i = 1; i < 10; i++) {
			testingFolds = new HashSet<>();
			for (int j = i+numberOfFolds; j < i+10; j++) {
				testingFolds.add(j%10+1);
			}
			System.out.println(testingFolds);
			System.out.println("Loading model");
			modelHandler.setClassifiersRootFolder(String.format("%s/%s-folds/%s/classifiersAndTables/modelClassifiers", classifiersTablesRoot, numberOfFolds, i));
			modelHandler.setTablesRootFolder(String.format("%s/%s-folds/%s/classifiersAndTables/modelTables", classifiersTablesRoot, numberOfFolds, i));
			modelHandler.loadFeaturesCalculators();
			modelHandler.loadClassifiers(false);
			//modelHandler.loadClassifiers(true);
			System.out.println("Loading datasets");
			testingDatasets.clear();
			for (String domain : domains) {
				for (int j = 1; j <= 10; j++) {
					if(testingFolds.contains(j)) {
						System.out.println(String.format("Adding fold %s", j));
						datasetsPath = String.format("%s/%s/%s", datasetsRoot, domain, j);
						datasetReader.addDataset(datasetsPath, 1.0, testingDatasets);
					}
				}
			}

			resultsPath = String.format("%s/%s-folds/%s/results", resultsRoot, numberOfFolds, i);

			System.out.println("Starting testing");
			clock.start();
			//testingDatasets = testingDatasets.subList(3491,5000);
			for (Dataset testingDataset : testingDatasets) {
				modelHandler.refineHintsUnlabelledDataset(testingDataset, useMulticlass);
				checkHints(testingDataset);
				modelHandler.saveResults(testingDataset, String.format("%s/results/1-iterations", resultsPath));
				/*modelHandler.refineHintsOnce(testingDataset);
				checkHints(testingDataset);
				System.out.println("Finished dataset");
				modelHandler.saveResults(testingDataset, String.format("%s/results/2-iterations", resultsPath));*/
			}
			clock.stop();
			FileUtilsCust.createCSV(String.format("%s/results/1-iterations/applicationTime.csv", resultsPath));
			FileUtilsCust.addLine(String.format("%s/results/1-iterations/applicationTime.csv", resultsPath), clock.getCPUTime());
			modelHandler.resetFolderCount();
		}

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
