package featuresCalculation.featureGroups.record;

import java.util.HashSet;
import java.util.Set;

import dataset.Record;
import featuresCalculation.FeaturesGroup;
import featuresCalculation.IterationType;
import featuresCalculation.features.record.Hint_DensityOfSlots;

public class Hint_DensityOfSlotGroup extends FeaturesGroup<Record>{

	//Constructors---------------------------------------------------
	
	public Hint_DensityOfSlotGroup() {
		super();
		setIterationType(IterationType.DOWN);
	}

	//Interface methods----------------------------------------------

	@Override
	public void initialize() {

		Hint_DensityOfSlots densityOfSlots;
		Set<String> slotClasses;
		
		slotClasses = new HashSet<String>();
		slotClasses.addAll(getRecordClasses());
		slotClasses.addAll(getAttributeClasses());
		
		for (String slotClass : slotClasses) {
			densityOfSlots = new Hint_DensityOfSlots();
			densityOfSlots.setClassName(slotClass);
			addFeature(densityOfSlots);
		}
		
	}
	
	

}
