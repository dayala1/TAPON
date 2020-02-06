package featuresCalculation.featureGroups.attribute;

import dataset.Attribute;
import featuresCalculation.Feature;
import featuresCalculation.FeaturesGroup;
import featuresCalculation.IterationType;
import featuresCalculation.features.attribute.NumericValue;
import featuresCalculation.features.attribute.TextualValue;

public class TextualValueGroup extends FeaturesGroup<Attribute>{
	//Constructors---------------------------------------------------

	public TextualValueGroup() {
		super();
		setIterationType(IterationType.DOWN);
	}

	//Interface methods----------------------------------------------
	
	public void initialize(){
		Feature<Attribute> feature;
		
		feature = new TextualValue();
		addFeature(feature);
	}

}
