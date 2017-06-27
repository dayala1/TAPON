package featuresCalculation.featureGroups.slot;

import java.util.HashSet;
import java.util.Set;

import dataset.Slot;
import featuresCalculation.Feature;
import featuresCalculation.FeaturesGroup;
import featuresCalculation.IterationType;
import featuresCalculation.features.record.Hint_NumberOfSlotsRecord;
import featuresCalculation.features.slot.Hint_DensityOfBrothers;
import featuresCalculation.features.slot.NumberOfAttributeBrothers;
import featuresCalculation.features.slot.NumberOfRecordBrothers;
import featuresCalculation.features.slot.NumberOfBrothers;

public class Hint_DensityOfBrothersGroup extends FeaturesGroup<Slot>{
	//Constructors---------------------------------------------------
	
	public Hint_DensityOfBrothersGroup() {
		super();
		setIterationType(IterationType.DOWN);
	}

	//Interface methods----------------------------------------------
	
	public void initialize(){
		Set<String> slotClasses;
		Hint_DensityOfBrothers densityOfBrothers;
		
		slotClasses = new HashSet<String>();
		slotClasses.addAll(getRecordClasses());
		slotClasses.addAll(getAttributeClasses());
		
		for (String slotClass : slotClasses) {
			densityOfBrothers = new Hint_DensityOfBrothers();
			densityOfBrothers.setClassName(slotClass);
			addFeature(densityOfBrothers);
		}
	}

}
