package featuresCalculation.featureGroups.dataset;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import dataset.Dataset;
import dataset.Slot;
import featuresCalculation.Featurable;
import featuresCalculation.FeaturesGroup;
import featuresCalculation.IterationType;
import featuresCalculation.features.dataset.NumberOfSlotsDataset;
import observer.Observer;

public class NumberOfSlotsDatasetGroup extends FeaturesGroup<Dataset> implements Observer<Featurable>{

	//Constructors---------------------------------------------------
	
	public NumberOfSlotsDatasetGroup() {
		super();
		setIterationType(IterationType.DOWN);
		this.slotClassMap = new HashMap<String, NumberOfSlotsDataset>();
	}

	//Internal state-------------------------------------------------
	
	private Map<String, NumberOfSlotsDataset> slotClassMap;

	//Interface methods----------------------------------------------

	@Override
	public void initialize() {

		NumberOfSlotsDataset numberOfSlotsDataset;
		Set<String> slotClasses;
		
		slotClasses = new HashSet<String>();
		slotClasses.addAll(getRecordClasses());
		slotClasses.addAll(getAttributeClasses());
		
		for (String slotClass : slotClasses) {
			numberOfSlotsDataset = new NumberOfSlotsDataset();
			numberOfSlotsDataset.setClassName(slotClass);
			slotClassMap.put(slotClass, numberOfSlotsDataset);
			addFeature(numberOfSlotsDataset);
		}
		
	}

	@Override
	public void update(Featurable info) {
		assert info != null;
		assert info instanceof Slot;
		
		Slot slot;
		String slotClass;
		NumberOfSlotsDataset numberOfSlotsDataset;
		
		slot = (Slot)info;
		slotClass = slot.getSlotClass();
		numberOfSlotsDataset = slotClassMap.get(slotClass);
		numberOfSlotsDataset.addSlot(slot);
		
	}
}
