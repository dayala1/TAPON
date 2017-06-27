package featuresCalculation.featureGroups.record;

import dataset.Record;
import featuresCalculation.Feature;
import featuresCalculation.FeaturesGroup;
import featuresCalculation.IterationType;
import featuresCalculation.features.record.NumberOfAttributeChildren;
import featuresCalculation.features.record.NumberOfRecordChildren;

public class NumberOfChildrenGroup extends FeaturesGroup<Record>{
	//Constructors---------------------------------------------------
	
	public NumberOfChildrenGroup() {
		super();
		setIterationType(IterationType.DOWN);
	}

	//Interface methods----------------------------------------------
	
	public void initialize(){
		Feature<Record> feature;
		
		feature = new NumberOfRecordChildren();
		addFeature(feature);
		feature = new NumberOfAttributeChildren();
		addFeature(feature);
	}

}
