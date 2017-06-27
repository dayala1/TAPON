package featuresCalculation.featureGroups.slot;

import dataset.Slot;
import featuresCalculation.Feature;
import featuresCalculation.FeaturesGroup;
import featuresCalculation.IterationType;
import featuresCalculation.features.slot.NumberOfAttributeBrothers;
import featuresCalculation.features.slot.NumberOfRecordBrothers;
import featuresCalculation.features.slot.NumberOfBrothers;

public class NumberOfBrothersGroup extends FeaturesGroup<Slot>{
	//Constructors---------------------------------------------------
	
	public NumberOfBrothersGroup() {
		super();
		setIterationType(IterationType.DOWN);
	}

	//Interface methods----------------------------------------------
	
	public void initialize(){
		Feature<Slot> feature;
		
		feature = new NumberOfAttributeBrothers();
		addFeature(feature);
		feature = new NumberOfRecordBrothers();
		addFeature(feature);
		feature = new NumberOfBrothers();
		addFeature(feature);
	}

}
