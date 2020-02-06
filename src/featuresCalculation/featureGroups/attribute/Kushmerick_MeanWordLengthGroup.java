package featuresCalculation.featureGroups.attribute;

import java.io.IOException;

import dataset.Attribute;
import featuresCalculation.FeaturesGroup;
import featuresCalculation.IterationType;
import featuresCalculation.features.attribute.CommonPrefixLength;
import featuresCalculation.features.attribute.CommonSuffixLength;
import featuresCalculation.features.attribute.Kushmerick_MeanWordLength;

public class Kushmerick_MeanWordLengthGroup extends FeaturesGroup<Attribute>{

	//Constructors---------------------------------------------------
	
	public Kushmerick_MeanWordLengthGroup() {
		super();
		setIterationType(IterationType.DOWN);
	}

	//Interface methods----------------------------------------------

	@Override
	public void initialize() throws IOException {

		Kushmerick_MeanWordLength kushmerick_meanWordLength;
		
		kushmerick_meanWordLength = new Kushmerick_MeanWordLength();
		addFeature(kushmerick_meanWordLength);
	}

}
