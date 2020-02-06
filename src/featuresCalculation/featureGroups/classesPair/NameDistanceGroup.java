package featuresCalculation.featureGroups.classesPair;

import dataset.Attribute;
import dataset.ClassesPair;
import featuresCalculation.Feature;
import featuresCalculation.FeaturesGroup;
import featuresCalculation.IterationType;
import featuresCalculation.features.attribute.NumericValue;
import featuresCalculation.features.classesPair.JaroNameDistance;
import featuresCalculation.features.classesPair.LevenshteinNameDistance;
import featuresCalculation.features.classesPair.NeedlemanWunchSimilarity;
import featuresCalculation.features.classesPair.SmithWatermanGotohSimilarity;

public class NameDistanceGroup extends FeaturesGroup<ClassesPair>{
	//Constructors---------------------------------------------------
	
	public NameDistanceGroup() {
		super();
		setIterationType(IterationType.DOWN);
	}

	//Interface methods----------------------------------------------
	
	public void initialize(){
		Feature<ClassesPair> feature;
		
		feature = new JaroNameDistance();
		addFeature(feature);
		feature = new LevenshteinNameDistance();
		addFeature(feature);
		feature = new NeedlemanWunchSimilarity();
		addFeature(feature);
		feature = new SmithWatermanGotohSimilarity();
		addFeature(feature);
	}

}
