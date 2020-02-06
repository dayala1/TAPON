package main;

import dataset.Dataset;
import dataset.Record;
import dataset.Slot;
import featuresCalculation.DatasetFeaturesCalculator;
import featuresCalculation.FeaturesGroup;
import featuresCalculation.featureGroups.attribute.*;
import featuresCalculation.featureGroups.featurable.Hint_MinimumTreeDistanceGroup;
import featuresCalculation.featureGroups.record.DummyRecordGroup;
import featuresCalculation.featureGroups.record.Hint_DensityOfSlotGroup;
import featuresCalculation.featureGroups.record.Hint_NumberOfSlotsRecordGroup;
import featuresCalculation.featureGroups.record.NumberOfChildrenGroup;
import featuresCalculation.featureGroups.slot.Hint_DensityOfBrothersGroup;
import featuresCalculation.featureGroups.slot.NodeDepthGroup;
import featuresCalculation.featureGroups.slot.NumberOfBrothersGroup;
import model.ModelHandler;
import model.randomForest.ModelHandlerRandomForest;
import utils.ClockMonitor;
import utils.DatasetReader;
import utils.FileUtilsCust;

import java.util.*;

public class ModelCreationDriver_temp {

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
		CosineDistanceGroup cosineDistanceGroup;
		NgramSimilarityGroup ngramSimilarityGroup;
		//OliveiraDaSilvaGroup oliveiraDaSilvaGroup;
		Hint_NumberOfSlotsRecordGroup numberOfSlotsRecordGroup;
		CommonPrefixSuffixLengthGroup commonPrefixSuffixLengthGroup;
		Hint_DensityOfSlotGroup densityOfSlotsGroup;
		NodeDepthGroup nodeDepthGroup;
		EditDistanceGroup editDistanceGroup;
		Hint_MinimumTreeDistanceGroup minimumTreeDistanceGroup;
		NumberOfBrothersGroup numberOfBrothersGroup;
		Hint_DensityOfBrothersGroup densityofBrothersGroup;
		NumericValueGroup numericValueGroup;
		DummyRecordGroup dummyRecordGroup;

		/*TyperScoreGroup typerScoreGroup;
		Pham_DistributionSimilarityGroup distributionSimilarityGroup;
		Pham_JaccardGroup jaccardGroup;*/
		NumberOfChildrenGroup numberOfChildrenGroup;

		DatasetFeaturesCalculator datasetFeaturesCalculator;

		//MODEL APPLICATION
		List<Dataset> trainingDatasets;
		DatasetReader datasetReader;
		String classifiersTablesRoot;
		String datasetsRoot;
		String datasetsPath;
		Boolean createSecondModel;
		Boolean useMulticlass;
		ModelHandler modelHandler;

		clock = new ClockMonitor();

		//MODEL CREATION
		//MODEL APPLICATION
		String[] domains = new String[]{"Articles", "Awards", "Countries", "Courseware", "Dev8d", "DigitalEconomy", "Edubase", "Epo", "Epsrc", "Restaurants"};
		datasetReader = new DatasetReader();

		Integer numberOfDomains = domains.length;
		classifiersTablesRoot = "E:/model/testsKushHintBased";
		Integer numberOfFolds = 8;
		datasetsRoot = "E:/Documents/US/Tesis/datasets";
		createSecondModel = true;
		useMulticlass = true;
		Set<Integer> trainingFolds;

		for (int i = 1; i < 6; i++) {
			trainingFolds = new HashSet<>();
			for (int j = i; j < i+numberOfFolds; j++) {
				trainingFolds.add(j%10+1);
			}
			System.out.println(trainingFolds);

			modelHandler = new ModelHandlerRandomForest();
			modelHandler.setClassifiersRootFolder(String.format("%s/%s-folds/%s/classifiersAndTables/modelClassifiers", classifiersTablesRoot, numberOfFolds, i));
			modelHandler.setTablesRootFolder(String.format("%s/%s-folds/%s/classifiersAndTables/modelTables", classifiersTablesRoot, numberOfFolds, i));

			slotFeaturesGroups = new HashSet<>();
			hintSlotFeaturesGroups = new HashSet<>();
			featurableFeaturesGroups = new HashSet<>();
			hintFeaturableFeaturesGroups = new HashSet<>();

			characterDensityGroup = new Kushmerick_CharacterDensityGroup();
			tokenDensityGroup = new Kushmerick_TokenDensityGroup();
			numberOfOccurrencesGroup = new Kushmerick_NumberOfOccurrencesGroup();
			meanWordLengthGroup = new Kushmerick_MeanWordLengthGroup();
			numberOfSlotsRecordGroup = new Hint_NumberOfSlotsRecordGroup();
			//commonPrefixSuffixLengthGroup = new CommonPrefixSuffixLengthGroup();
			densityOfSlotsGroup = new Hint_DensityOfSlotGroup();
			//nodeDepthGroup = new NodeDepthGroup();
			//editDistanceGroup = new EditDistanceGroup();
			//ngramSimilarityGroup = new NgramSimilarityGroup();
			//cosineDistanceGroup = new CosineDistanceGroup();
			//numberOfBrothersGroup = new NumberOfBrothersGroup();
			minimumTreeDistanceGroup = new Hint_MinimumTreeDistanceGroup();
			densityofBrothersGroup = new Hint_DensityOfBrothersGroup();
			//numericValueGroup = new NumericValueGroup();
			dummyRecordGroup = new DummyRecordGroup();

			//numberOfChildrenGroup = new NumberOfChildrenGroup();
			/*distributionSimilarityGroup = new Pham_DistributionSimilarityGroup();
			jaccardGroup = new Pham_JaccardGroup();
			typerScoreGroup = new TyperScoreGroup();*/


			//slotFeaturesGroups.add(ngramSimilarityGroup);
			//slotFeaturesGroups.add(cosineDistanceGroup);
			slotFeaturesGroups.add(characterDensityGroup);
			slotFeaturesGroups.add(tokenDensityGroup);
			slotFeaturesGroups.add(numberOfOccurrencesGroup);
			slotFeaturesGroups.add(meanWordLengthGroup);
			//slotFeaturesGroups.add(commonPrefixSuffixLengthGroup);
			//slotFeaturesGroups.add(nodeDepthGroup);
			//slotFeaturesGroups.add(editDistanceGroup);
			//slotFeaturesGroups.add(numberOfBrothersGroup);
			//slotFeaturesGroups.add(numericValueGroup);

			//slotFeaturesGroups.add(numberOfChildrenGroup);
			/*slotFeaturesGroups.add(typerScoreGroup);
			slotFeaturesGroups.add(distributionSimilarityGroup);
			slotFeaturesGroups.add(jaccardGroup);
			*/
			slotFeaturesGroups.add(dummyRecordGroup);
			hintSlotFeaturesGroups.add(numberOfSlotsRecordGroup);
			hintSlotFeaturesGroups.add(densityOfSlotsGroup);
			hintSlotFeaturesGroups.add(densityofBrothersGroup);
			hintFeaturableFeaturesGroups.add(minimumTreeDistanceGroup);


			trainingDatasets = new ArrayList<Dataset>();

			for (String domain : domains) {
				for (Integer trainingFold : trainingFolds) {
					datasetsPath = String.format("%s/%s/%s",datasetsRoot, domain, trainingFold);
					datasetReader.addDataset(datasetsPath, 1.0, trainingDatasets);
				}
			}

			modelHandler.setFeaturableFeaturesGroups(featurableFeaturesGroups);
			modelHandler.setSlotFeaturesGroups(slotFeaturesGroups);
			modelHandler.setHintsFeaturableFeaturesGroups(hintFeaturableFeaturesGroups);
			modelHandler.setHintsSlotFeaturesGroups(hintSlotFeaturesGroups);
			clock.start();
			modelHandler.trainModel(trainingDatasets, new HashMap<>(), createSecondModel, useMulticlass);
			clock.stop();

			FileUtilsCust.createCSV(String.format("%s/trainingTime.csv",modelHandler.getClassifiersRootFolder()));
			FileUtilsCust.addLine(String.format("%s/trainingTime.csv",modelHandler.getClassifiersRootFolder()), clock.getCPUTime());
			modelHandler.closeContext();
		}


		//

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
