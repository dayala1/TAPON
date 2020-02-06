package featuresCalculation.featureGroups.record;

import dataset.Record;
import featuresCalculation.Feature;
import featuresCalculation.FeaturesGroup;
import featuresCalculation.IterationType;
import featuresCalculation.features.record.DummyRecordFeature;
import featuresCalculation.features.record.NumberOfAttributeChildren;
import featuresCalculation.features.record.NumberOfRecordChildren;

public class DummyRecordGroup extends FeaturesGroup<Record>{
	//Constructors---------------------------------------------------

	public DummyRecordGroup() {
		super();
		setIterationType(IterationType.DOWN);
	}

	//Interface methods----------------------------------------------
	
	public void initialize(){
		Feature<Record> feature;
		
		feature = new DummyRecordFeature();
		addFeature(feature);
	}

}
