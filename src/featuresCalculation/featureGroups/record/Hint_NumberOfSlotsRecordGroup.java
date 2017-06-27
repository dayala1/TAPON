package featuresCalculation.featureGroups.record;

import java.util.HashSet;
import java.util.Set;

import dataset.Record;
import featuresCalculation.FeaturesGroup;
import featuresCalculation.IterationType;
import featuresCalculation.features.record.Hint_NumberOfSlotsRecord;

public class Hint_NumberOfSlotsRecordGroup extends FeaturesGroup<Record>{

	//Constructors---------------------------------------------------
	
	public Hint_NumberOfSlotsRecordGroup() {
		super();
		setIterationType(IterationType.DOWN);
	}

	//Interface methods----------------------------------------------

	@Override
	public void initialize() {

		Hint_NumberOfSlotsRecord numberOfSlotsRecord;
		Set<String> slotClasses;
		
		slotClasses = new HashSet<String>();
		slotClasses.addAll(getRecordClasses());
		slotClasses.addAll(getAttributeClasses());
		
		for (String slotClass : slotClasses) {
			numberOfSlotsRecord = new Hint_NumberOfSlotsRecord();
			numberOfSlotsRecord.setClassName(slotClass);
			addFeature(numberOfSlotsRecord);
		}
		
	}

}
