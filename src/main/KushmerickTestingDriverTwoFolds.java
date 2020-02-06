package main;
import java.util.*;

import dataset.Dataset;
import dataset.Record;
import dataset.Slot;
import featuresCalculation.DatasetFeaturesCalculator;
import featuresCalculation.FeaturesGroup;
import featuresCalculation.featureGroups.attribute.Kushmerick_CharacterDensityGroup;
import featuresCalculation.featureGroups.attribute.Kushmerick_MeanWordLengthGroup;
import featuresCalculation.featureGroups.attribute.Kushmerick_NumberOfOccurrencesGroup;
import featuresCalculation.featureGroups.attribute.Kushmerick_TokenDensityGroup;
import featuresCalculation.featureGroups.record.NumberOfChildrenGroup;
import model.randomForest.ModelHandlerRandomForestOneIteration;
import utils.ClockMonitor;
import utils.DatasetReader;
import utils.FileUtilsCust;

public class KushmerickTestingDriverTwoFolds {
	
	public static double incorrect = 0.0;
	public static double correct = 0.0;

	public static void main(String[] args) throws Exception {
		ClockMonitor clock;
		
		//MODEL CREATION
		Dataset dataset;
		Set<FeaturesGroup> slotFeaturesGroups;
		Set<FeaturesGroup> featurableFeaturesGroups;
		Set<FeaturesGroup> hintSlotFeaturesGroups;
		Set<FeaturesGroup> hintFeaturableFeaturesGroups;
		Kushmerick_CharacterDensityGroup characterDensityGroup;
		Kushmerick_TokenDensityGroup tokenDensityGroup;
		Kushmerick_NumberOfOccurrencesGroup numberOfOccurrencesGroup;
		Kushmerick_MeanWordLengthGroup meanWordLengthGroup;
		NumberOfChildrenGroup numberOfChildrenGroup;
		DatasetFeaturesCalculator datasetFeaturesCalculator;
		List<String> trainingDatasetsFolders;
		
		//MODEL APPLICATION
		List<Dataset> trainingDatasets;
		List<Dataset> testingDatasets;
		DatasetReader datasetReader;
		String classifiersTablesRoot;
		String resultsRoot;
		String datasetsRoot;
		String[] domains;
		int testingFoldNumber;
		int testingFoldNumber2;
		Integer numberOfDomains;
		String datasetsPath;
		ModelHandlerRandomForestOneIteration modelHandler;
		String resultsPath;
		
		clock = new ClockMonitor();
		
		//MODEL CREATION
		slotFeaturesGroups = new HashSet<FeaturesGroup>();
		hintSlotFeaturesGroups = new HashSet<FeaturesGroup>();
		characterDensityGroup = new Kushmerick_CharacterDensityGroup();
		tokenDensityGroup = new Kushmerick_TokenDensityGroup();
		numberOfOccurrencesGroup = new Kushmerick_NumberOfOccurrencesGroup();
		meanWordLengthGroup = new Kushmerick_MeanWordLengthGroup();
		numberOfChildrenGroup = new NumberOfChildrenGroup();
		trainingDatasetsFolders = new ArrayList<String>();
		featurableFeaturesGroups = new HashSet<FeaturesGroup>();
		hintFeaturableFeaturesGroups = new HashSet<FeaturesGroup>();
		
		//MODEL APPLICATION
		datasetReader = new DatasetReader();
		trainingDatasets = new ArrayList<Dataset>();
		testingDatasets = new ArrayList<Dataset>();
		modelHandler = new ModelHandlerRandomForestOneIteration();
		
		domains = Arrays.copyOfRange(args, 5, args.length);
		numberOfDomains = domains.length;
		classifiersTablesRoot = args[0];
		resultsRoot = args[1];
		datasetsRoot = args[2];
		testingFoldNumber = Integer.valueOf(args[3]);
		testingFoldNumber2 = Integer.valueOf(args[4]);
		
		modelHandler.setClassifiersRootFolder(String.format("%s/classifiersAndTables/modelClassifiers/%s-domains/fold-%s-%s", classifiersTablesRoot, numberOfDomains, testingFoldNumber, testingFoldNumber2));
		modelHandler.setTablesRootFolder(String.format("%s/classifiersAndTables/modelTables/%s-domains/fold-%s-%s", classifiersTablesRoot, numberOfDomains, testingFoldNumber, testingFoldNumber2));
		
		slotFeaturesGroups.add(characterDensityGroup);
		slotFeaturesGroups.add(tokenDensityGroup);
		slotFeaturesGroups.add(numberOfOccurrencesGroup);
		slotFeaturesGroups.add(meanWordLengthGroup);
		slotFeaturesGroups.add(numberOfChildrenGroup);
		
		trainingDatasetsFolders = new ArrayList<String>();
		
		for (String domain : domains) {
			for (int i = 1; i < 11; i++) {
				datasetsPath = String.format("%s/Datasets/%s/%s",datasetsRoot, domain, i);
				if (i == testingFoldNumber || i == testingFoldNumber2) {
					datasetReader.addDataset(datasetsPath, 1.0, trainingDatasets);
				} else {
					datasetReader.addDataset(datasetsPath, 1.0, testingDatasets);
				}
			}
		}
		
		resultsPath = String.format("%s/results", resultsRoot);
		modelHandler.setFeaturableFeaturesGroups(featurableFeaturesGroups);
		modelHandler.setSlotFeaturesGroups(slotFeaturesGroups);
		modelHandler.setHintsFeaturableFeaturesGroups(hintFeaturableFeaturesGroups);
		modelHandler.setHintsSlotFeaturesGroups(hintSlotFeaturesGroups);
		clock.start();
		modelHandler.trainModel(trainingDatasets, new HashMap<>());
		clock.stop();
		FileUtilsCust.createCSV(String.format("%s/results/%s-domains/fold-%s-%s/trainingTime.csv", resultsPath, numberOfDomains, testingFoldNumber, testingFoldNumber2));
		FileUtilsCust.addLine(String.format("%s/results/%s-domains/fold-%s-%s/trainingTime.csv", resultsPath, numberOfDomains, testingFoldNumber, testingFoldNumber2), clock.getCPUTime());
		modelHandler.createNewContext();
		
		clock.start();
		modelHandler.loadClassifiers(false);
		System.out.println("Starting testing");
		for (Dataset testingDataset : testingDatasets) {
			modelHandler.refineHintsUnlabelledDataset(testingDataset);
			checkHints(testingDataset);
			modelHandler.saveResults(testingDataset, String.format("%s/results/%s-domains/fold-%s-%s/1-iterations", resultsPath, numberOfDomains, testingFoldNumber, testingFoldNumber2));
		}
		clock.stop();
		FileUtilsCust.createCSV(String.format("%s/results/%s-domains/fold-%s-%s/1-iterations/applicationTime.csv", resultsPath, numberOfDomains, testingFoldNumber, testingFoldNumber2));
		FileUtilsCust.addLine(String.format("%s/results/%s-domains/fold-%s-%s/1-iterations/applicationTime.csv", resultsPath, numberOfDomains, testingFoldNumber, testingFoldNumber2), clock.getCPUTime());
		
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
