package featuresCalculation.featureGroups.attribute;

import dataset.Attribute;
import featuresCalculation.Feature;
import featuresCalculation.FeaturesGroup;
import featuresCalculation.IterationType;
import featuresCalculation.features.attribute.GloveEmbeddingsCrawl300;
import featuresCalculation.features.attribute.NumericValue;

import java.io.IOException;

public class GloveEmbeddingsCrawl300Group extends FeaturesGroup<Attribute>{
	//Constructors---------------------------------------------------

	public GloveEmbeddingsCrawl300Group() {
		super();
		setIterationType(IterationType.DOWN);
	}

	//Interface methods----------------------------------------------
	
	public void initialize() throws IOException {
		Feature<Attribute> feature;
		
		feature = new GloveEmbeddingsCrawl300();
		addFeature(feature);
	}

}
