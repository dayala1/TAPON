package main;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import model.Classifier;
import model.randomForest.ModelHandlerRandomForest;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.ml.classification.RandomForestClassificationModel;
import org.apache.spark.ml.linalg.Vector;
import org.json.simple.parser.ParseException;

public class ClassifiersPrintingDriver {

	public static void main(String[] args) throws IOException, ParseException, ClassNotFoundException {
		JavaSparkContext context;
		SparkConf sparkConf;
		String classifierPath;
		
		sparkConf = new SparkConf();
		sparkConf.setAppName("SparkHandler");
		sparkConf.setMaster(sparkConf.get("master", "local[*]"));
		context = new JavaSparkContext(sparkConf);
		ModelHandlerRandomForest modelHandler = new ModelHandlerRandomForest();
		modelHandler.setClassifiersRootFolder("E:/model/testHBFeaturesUse/8-folds/0/classifiersAndTables/modelClassifiers");
		modelHandler.setTablesRootFolder("E:/model/testHBFeaturesUse/8-folds/0/classifiersAndTables/modelTables");
		modelHandler.loadClassifiers();
		modelHandler.loadFeaturesCalculators();
		modelHandler.loadFeatureNames();
		Map<Integer, String> featureNames = modelHandler.getFeatureNamesAttributesHB();
		Integer countHB = 0;
		for (Classifier model :
				modelHandler.getSparkHandler().getAttributeClassifiersHintBased()) {
			System.out.println(model.getName());
			Vector importances = ((RandomForestClassificationModel)model.getModel()).featureImportances();
			List<Integer> bestFeatures = IntStream.range(0, importances.size()).mapToObj(i->new Integer(i)).sorted(Comparator.comparing(n->importances.apply((int)n)).reversed()).limit(5).collect(Collectors.toList());
			for (Integer feature:
				 bestFeatures) {
				String featureName =  featureNames.get(feature);
				System.out.println(String.format("\t%s : %s", featureName, importances.apply(feature)));
				if (	featureName != null && (
						featureName.startsWith("Density of siblings") ||
						featureName.startsWith("Minimum tree distance") ||
						featureName.startsWith("Number of children slots with label") ||
						featureName.startsWith("Density of children slots with label") )){
					countHB++;
				}
			}
		}
		System.out.println(countHB);
		System.out.println(modelHandler.getSparkHandler().getAttributeClassifiersHintBased().size()*5);
		//RandomForestClassificationModel model = RandomForestClassificationModel.load("E:/model/resultsCompareOneVsAll/3-folds/0/classifiersAndTables/modelClassifiers/classifiersNoHint/attributes/http__www.aktors.org_ontology_portal#has-grant-reference");
		//System.out.println(model.toDebugString());
		//System.out.println(model.featureImportances());
		context.close();
		
	}

}
