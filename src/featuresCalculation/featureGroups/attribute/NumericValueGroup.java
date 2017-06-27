package featuresCalculation.featureGroups.attribute;

import dataset.Attribute;
import featuresCalculation.Feature;
import featuresCalculation.FeaturesGroup;
import featuresCalculation.IterationType;
import featuresCalculation.features.attribute.NumericValue;
import featuresCalculation.features.slot.NumberOfBrothers;
import featuresCalculation.features.slot.NumberOfRecordBrothers;

public class NumericValueGroup extends FeaturesGroup<Attribute>{
	//Constructors---------------------------------------------------
	
	public NumericValueGroup() {
		super();
		setIterationType(IterationType.DOWN);
	}

	//Interface methods----------------------------------------------
	
	public void initialize(){
		Feature<Attribute> feature;
		
		feature = new NumericValue();
		addFeature(feature);
	}

}
