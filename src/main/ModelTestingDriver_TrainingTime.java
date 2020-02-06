package main;
import java.util.*;

import dataset.Dataset;
import dataset.Record;
import dataset.Slot;
import featuresCalculation.DatasetFeaturesCalculator;
import featuresCalculation.FeaturesGroup;
import featuresCalculation.featureGroups.attribute.CharacterDensityGroup;
import featuresCalculation.featureGroups.attribute.CommonPrefixSuffixLengthGroup;
import featuresCalculation.featureGroups.attribute.EditDistanceGroup;
import featuresCalculation.featureGroups.attribute.NumberOfOccurrencesGroup;
import featuresCalculation.featureGroups.attribute.NumericValueGroup;
import featuresCalculation.featureGroups.attribute.TokenDensityGroup;
import featuresCalculation.featureGroups.attribute.TyperScoreGroup;
import featuresCalculation.featureGroups.featurable.Hint_MinimumTreeDistanceGroup;
import featuresCalculation.featureGroups.record.Hint_DensityOfSlotGroup;
import featuresCalculation.featureGroups.record.Hint_NumberOfSlotsRecordGroup;
import featuresCalculation.featureGroups.record.NumberOfChildrenGroup;
import featuresCalculation.featureGroups.slot.Hint_DensityOfBrothersGroup;
import featuresCalculation.featureGroups.slot.NodeDepthGroup;
import featuresCalculation.featureGroups.slot.NumberOfBrothersGroup;
import featuresCalculation.featureGroups.slot.OliveiraDaSilvaGroup;
import model.randomForest.ModelHandlerRandomForest;
import utils.ClockMonitor;
import utils.DatasetReader;
import utils.FileUtilsCust;

public class ModelTestingDriver_TrainingTime {
	
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
		CharacterDensityGroup characterDensityGroup;
		TokenDensityGroup tokenDensityGroup;
		NumberOfOccurrencesGroup numberOfOccurrencesGroup;
		OliveiraDaSilvaGroup oliveiraDaSilvaGroup;
		Hint_NumberOfSlotsRecordGroup numberOfSlotsRecordGroup;
		CommonPrefixSuffixLengthGroup commonPrefixSuffixLengthGroup;
		Hint_DensityOfSlotGroup densityOfSlotsGroup;
		NodeDepthGroup nodeDepthGroup;
		EditDistanceGroup editDistanceGroup;
		TyperScoreGroup typerScoreGroup;
		Hint_MinimumTreeDistanceGroup minimumTreeDistanceGroup;
		NumberOfBrothersGroup numberOfBrothersGroup;
		NumberOfChildrenGroup numberOfChildrenGroup;
		Hint_DensityOfBrothersGroup densityofBrothersGroup;
		NumericValueGroup numericValueGroup;
		DatasetFeaturesCalculator datasetFeaturesCalculator;
		List<String> trainingDatasetsFolders;
		List<Integer> includedFolds;
		
		//MODEL APPLICATION
		List<Dataset> trainingDatasets;
		List<Dataset> testingDatasets;
		DatasetReader datasetReader;
		String classifiersTablesRoot;
		String resultsRoot;
		String datasetsRoot;
		String[] domains;
		Integer numberOfDomains;
		String datasetsPath;
		ModelHandlerRandomForest modelHandler;
		String resultsPath;
		int numberOfFolds;
		
		clock = new ClockMonitor();
		
		
		//MODEL APPLICATION
		
		domains = Arrays.copyOfRange(args, 4, args.length);
		numberOfDomains = domains.length;
		classifiersTablesRoot = args[0];
		resultsRoot = args[1];
		datasetsRoot = args[2];
		numberOfFolds = Integer.valueOf(args[3]);
		//for(int numberOfFolds=10; numberOfFolds>0; numberOfFolds--){
		for (int i = 1; i < 11; i++) {
			slotFeaturesGroups = new HashSet<FeaturesGroup>();
			hintSlotFeaturesGroups = new HashSet<FeaturesGroup>();
			characterDensityGroup = new CharacterDensityGroup();
			tokenDensityGroup = new TokenDensityGroup();
			numberOfOccurrencesGroup = new NumberOfOccurrencesGroup();
			oliveiraDaSilvaGroup = new OliveiraDaSilvaGroup();
			numberOfSlotsRecordGroup = new Hint_NumberOfSlotsRecordGroup();
			commonPrefixSuffixLengthGroup = new CommonPrefixSuffixLengthGroup();
			densityOfSlotsGroup = new Hint_DensityOfSlotGroup();
			nodeDepthGroup = new NodeDepthGroup();
			editDistanceGroup = new EditDistanceGroup();
			typerScoreGroup = new TyperScoreGroup();
			numberOfBrothersGroup = new NumberOfBrothersGroup();
			numberOfChildrenGroup = new NumberOfChildrenGroup();
			trainingDatasetsFolders = new ArrayList<String>();
			featurableFeaturesGroups = new HashSet<FeaturesGroup>();
			hintFeaturableFeaturesGroups = new HashSet<FeaturesGroup>();
			minimumTreeDistanceGroup = new Hint_MinimumTreeDistanceGroup();
			densityofBrothersGroup = new Hint_DensityOfBrothersGroup();
			numericValueGroup = new NumericValueGroup();
			datasetReader = new DatasetReader();
			trainingDatasets = new ArrayList<Dataset>();
			testingDatasets = new ArrayList<Dataset>();
			modelHandler = new ModelHandlerRandomForest();
			includedFolds = new ArrayList<>();
			//Tomar los folds que se usar�n para entrenar
			for (int j = 0; j < numberOfFolds; j++) {
				includedFolds.add(((i+j)%10)+1);
			}
			modelHandler.setClassifiersRootFolder(String.format("%s/classifiersAndTables/modelClassifiers/%s-domains/NOF-%s/%s", classifiersTablesRoot, numberOfDomains, numberOfFolds, i));
			modelHandler.setTablesRootFolder(String.format("%s/classifiersAndTables/modelTables/%s-domains/NOF-%s/%s", classifiersTablesRoot, numberOfDomains, numberOfFolds, i));
			
			slotFeaturesGroups.add(characterDensityGroup);
			slotFeaturesGroups.add(tokenDensityGroup);
			slotFeaturesGroups.add(numberOfOccurrencesGroup);
			//slotFeaturesGroups.add(oliveiraDaSilvaGroup);
			slotFeaturesGroups.add(commonPrefixSuffixLengthGroup);
			slotFeaturesGroups.add(nodeDepthGroup);
			slotFeaturesGroups.add(editDistanceGroup);
			slotFeaturesGroups.add(typerScoreGroup);
			slotFeaturesGroups.add(numberOfBrothersGroup);
			slotFeaturesGroups.add(numberOfChildrenGroup);
			slotFeaturesGroups.add(numericValueGroup);
			hintSlotFeaturesGroups.add(numberOfSlotsRecordGroup);
			hintSlotFeaturesGroups.add(densityOfSlotsGroup);
			hintSlotFeaturesGroups.add(densityofBrothersGroup);
			hintFeaturableFeaturesGroups.add(minimumTreeDistanceGroup);
			
			trainingDatasetsFolders = new ArrayList<String>();
			
			for (String domain : domains) {
				for (int k = 1; k < 11; k++) {
					datasetsPath = String.format("%s/Datasets/%s/%s",datasetsRoot, domain, k);
					if (includedFolds.contains(k)) {
						datasetReader.addDataset(datasetsPath, 1.0, trainingDatasets);
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
			FileUtilsCust.createCSV(String.format("%s/1-iteration/%s-domains/NOF-%s/%s/trainingTime.csv", resultsPath, numberOfDomains, numberOfFolds, i));
			FileUtilsCust.addLine(String.format("%s/1-iteration/%s-domains/NOF-%s/%s/trainingTime.csv", resultsPath, numberOfDomains, numberOfFolds, i), modelHandler.getFirstModelTrainTime());
			modelHandler.closeContext();
		}
		//}
		
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
