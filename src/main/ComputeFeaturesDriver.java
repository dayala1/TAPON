package main;

import dataset.Dataset;
import dataset.Record;
import dataset.Slot;
import featuresCalculation.DatasetFeaturesCalculator;
import featuresCalculation.FeaturesGroup;
import featuresCalculation.featureGroups.attribute.*;
import featuresCalculation.featureGroups.classesPair.NameDistanceGroup;
import featuresCalculation.featureGroups.featurable.Hint_MinimumTreeDistanceGroup;
import featuresCalculation.featureGroups.record.Hint_DensityOfSlotGroup;
import featuresCalculation.featureGroups.record.Hint_NumberOfSlotsRecordGroup;
import featuresCalculation.featureGroups.record.NumberOfChildrenGroup;
import featuresCalculation.featureGroups.slot.Hint_DensityOfBrothersGroup;
import featuresCalculation.featureGroups.slot.NodeDepthGroup;
import featuresCalculation.featureGroups.slot.NumberOfBrothersGroup;
import featuresCalculation.featureGroups.slot.SourceGroup;
import model.ModelHandler;
import model.linearSVC.ModelHandlerLinearSVC;
import org.apache.commons.io.FileUtils;
import org.spark_project.guava.collect.Lists;
import utils.ClockMonitor;
import utils.DatasetReader;
import utils.FileUtilsCust;

import java.io.File;
import java.util.*;

public class ComputeFeaturesDriver {

	public static double incorrect = 0.0;
	public static double correct = 0.0;

	public static void main(String[] args) throws Exception {
		ClockMonitor clock;
		
		//MODEL CREATION
		Set<FeaturesGroup> slotFeaturesGroups;
		Set<FeaturesGroup> featurableFeaturesGroups;
		Set<FeaturesGroup> hintSlotFeaturesGroups;
		Set<FeaturesGroup> hintFeaturableFeaturesGroups;
		Set<FeaturesGroup> classesPairFeaturesGroups;

		CharacterDensityGroup characterDensityGroup = new CharacterDensityGroup();
		TokenDensityGroup tokenDensityGroup = new TokenDensityGroup();
		NumberOfOccurrencesGroup numberOfOccurrencesGroup = new NumberOfOccurrencesGroup();
		//OliveiraDaSilvaGroup oliveiraDaSilvaGroup;
		//Hint_NumberOfSlotsRecordGroup numberOfSlotsRecordGroup = new Hint_NumberOfSlotsRecordGroup();
		//CommonPrefixSuffixLengthGroup commonPrefixSuffixLengthGroup = new CommonPrefixSuffixLengthGroup();
		//Hint_DensityOfSlotGroup densityOfSlotsGroup;
		//NodeDepthGroup nodeDepthGroup = new NodeDepthGroup();
		//CosineDistanceGroup cosineDistanceGroup = new CosineDistanceGroup();
		//NgramSimilarityGroup ngramSimilarityGroup = new NgramSimilarityGroup();
		//TyperScoreGroup typerScoreGroup = new TyperScoreGroup();
		//EditDistanceGroup editDistanceGroup = new EditDistanceGroup();
		//Hint_MinimumTreeDistanceGroup minimumTreeDistanceGroup;
		//NumberOfBrothersGroup numberOfBrothersGroup;
		//Hint_DensityOfBrothersGroup densityofBrothersGroup;
		NumericValueGroup numericValueGroup = new NumericValueGroup();
		TextualValueGroup textualValueGroup = new TextualValueGroup();
		SourceGroup sourceGroup = new SourceGroup();
		GloveEmbeddingsCrawl300Group gloveEmbeddingsCrawl300Group = new GloveEmbeddingsCrawl300Group();

		//NameDistanceGroup nameDistanceGroup = new NameDistanceGroup();

		/*TyperScoreGroup typerScoreGroup;
		Pham_DistributionSimilarityGroup distributionSimilarityGroup;
		Pham_JaccardGroup jaccardGroup;*/
		//NumberOfChildrenGroup numberOfChildrenGroup;

		DatasetFeaturesCalculator datasetFeaturesCalculator;

		//MODEL APPLICATION
		List<Dataset> trainingDatasets;
		List<Dataset> classesDatasets;
		DatasetReader datasetReader;
		String datasetsRoot;
		String datasetsPath;
		DatasetFeaturesCalculator featuresCalculator;

		clock = new ClockMonitor();

		//MODEL CREATION
		//MODEL APPLICATION

		List<String> classesDomains = Lists.newArrayList(new String[]{
				//"buy.net"
		});

		/*List<String> domains = Lists.newArrayList(new String[]{
				"buy.net",
				"cammarkt.com",
				"www.alibaba.com",
				"www.buzzillions.com",
				"www.cambuy.com.au",
				"www.camerafarm.com.au",
				"www.canon-europe.com",
				"www.ebay.com",
				"www.eglobalcentral.co.uk",
				"www.flipkart.com",
				"www.garricks.com.au",
				"www.gosale.com",
				"www.henrys.com",
				"www.ilgs.net",
				"www.mypriceindia.com",
				"www.pcconnection.com",
				"www.pricedekho.com",
				"www.price-hunt.com",
				"www.priceme.co.nz",
				"www.shopbot.com.au",
				"www.shopmania.in",
				"www.ukdigitalcameras.co.uk",
				"www.walmart.com",
				"www.wexphotographic.com"
		});*/

		/*List<String> domains = Lists.newArrayList(new String[]{
				"aliexpress.com",
				"bestbuy.com",
				"ebay.com",
				"newegg.com",
				"shop.com",
				"walmart.com"
		});*/

		/*List<String> domains = Lists.newArrayList(new String[]{
				"aliexpress.com",
				"bestbuy.com",
				"ebay.com",
				"newegg.com",
				"shop.com",
				"walmart.com",
				"overstock.com",
				"samsclub.com",
				"searsoutlet.com",
				"shop.com",
				"target.com",
				"techspot.com",
				"tesco.com",
				"walmart.com"
		});*/

		List<String> domains = Lists.newArrayList(new String[]{
				"bestbuy.com",
				"conns.com",
				"ebay.com",
				"flipkart.com",
				"macmall.com",
				"microcenter.com",
				"shop.com",
				"walmart.com"
		});

		datasetReader = new DatasetReader();

		Integer numberOfDomains = domains.size();
		Integer numberOfFolds = 10;
		datasetsRoot = "E:/Documents/US/Tesis/datasets/matching-products-tvs";
		Set<Integer> trainingFolds;

		for (int i = 0; i < 1; i++) {
			trainingFolds = new HashSet<>();
			for (int j = i; j < i+numberOfFolds; j++) {
				trainingFolds.add(j%10+1);
			}
			System.out.println(trainingFolds);

			//modelHandler = new ModelHandlerLinearSVC();
			//modelHandler.setClassifiersRootFolder(String.format("%s/%s-folds/%s/classifiersAndTables/modelClassifiers", classifiersTablesRoot, numberOfFolds, i));
			//modelHandler.setTablesRootFolder(String.format("%s/%s-folds/%s/classifiersAndTables/modelTables", classifiersTablesRoot, numberOfFolds, i));

			slotFeaturesGroups = new HashSet<>();
			hintSlotFeaturesGroups = new HashSet<>();
			featurableFeaturesGroups = new HashSet<>();
			hintFeaturableFeaturesGroups = new HashSet<>();
			classesPairFeaturesGroups = new HashSet<>();

			slotFeaturesGroups.add(characterDensityGroup);
			slotFeaturesGroups.add(tokenDensityGroup);
			slotFeaturesGroups.add(numberOfOccurrencesGroup);
			//slotFeaturesGroups.add(commonPrefixSuffixLengthGroup);
			//slotFeaturesGroups.add(nodeDepthGroup);
			//slotFeaturesGroups.add(editDistanceGroup);
			//slotFeaturesGroups.add(numberOfBrothersGroup);
			slotFeaturesGroups.add(numericValueGroup);
			//slotFeaturesGroups.add(cosineDistanceGroup);
			//slotFeaturesGroups.add(ngramSimilarityGroup);
			//slotFeaturesGroups.add(typerScoreGroup);
			slotFeaturesGroups.add(textualValueGroup);
			slotFeaturesGroups.add(sourceGroup);
			slotFeaturesGroups.add(gloveEmbeddingsCrawl300Group);

			//classesPairFeaturesGroups.add(nameDistanceGroup);

			//slotFeaturesGroups.add(numberOfChildrenGroup);
			/*slotFeaturesGroups.add(typerScoreGroup);
			slotFeaturesGroups.add(distributionSimilarityGroup);
			slotFeaturesGroups.add(jaccardGroup);
			*/
			//hintSlotFeaturesGroups.add(numberOfSlotsRecordGroup);
			//hintSlotFeaturesGroups.add(densityOfSlotsGroup);
			//hintSlotFeaturesGroups.add(densityofBrothersGroup);
			//hintFeaturableFeaturesGroups.add(minimumTreeDistanceGroup);


			trainingDatasets = new ArrayList<Dataset>();
			classesDatasets = new ArrayList<Dataset>();
			List<Dataset> newDatasets;
			for (String domain : domains) {
				for (Integer trainingFold : trainingFolds) {
					datasetsPath = String.format("%s/%s/%s",datasetsRoot, domain, trainingFold);
					newDatasets = new ArrayList<>();
					try {
						datasetReader.addDataset(datasetsPath, 1.0, newDatasets);
						trainingDatasets.addAll(newDatasets);
						if (classesDomains.contains(domain)) {
							classesDatasets.addAll(newDatasets);
						}
					} catch(NullPointerException e){
						System.out.println("Fold not present: "+trainingFold);
					}
				}
			}

			featuresCalculator = new DatasetFeaturesCalculator();
			featuresCalculator.setSlotFeaturesGroups(slotFeaturesGroups);
			//featuresCalculator.setSlotFeaturesGroups(new HashSet<>());
			featuresCalculator.setFeaturableFeaturesGroups(featurableFeaturesGroups);
			//featuresCalculator.setFeaturableFeaturesGroups(new HashSet<>());
			featuresCalculator.setClassesPairsFeaturesGroups(classesPairFeaturesGroups);
			featuresCalculator.setIndexPath("E:/matching-features/tvs/index");
			String tablesRootFolder = "E:/matching-features/tvs/tables";
			File tablesRootFolderFile = new File(tablesRootFolder);

			datasetReader = new DatasetReader();
			if (tablesRootFolderFile.exists()) {
				FileUtils.cleanDirectory(tablesRootFolderFile);
			}

			tablesRootFolderFile.mkdirs();
			featuresCalculator.initializeClasses(classesDatasets);
			featuresCalculator.initializeClasses(trainingDatasets);
			featuresCalculator.initialize(false);
			//featuresCalculator.computeFeaturesClassesPairs(tablesRootFolder, true);

			for (Dataset dataset : trainingDatasets) {
				featuresCalculator.setDataset(dataset);
				featuresCalculator.run(tablesRootFolder, true);
				System.out.println(String.format("Processed dataset %s", dataset));
			}
		}

	}
	
}
