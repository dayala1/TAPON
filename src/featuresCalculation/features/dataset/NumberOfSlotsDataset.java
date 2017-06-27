package featuresCalculation.features.dataset;

import java.util.HashSet;
import java.util.Set;

import dataset.Dataset;
import dataset.Slot;
import featuresCalculation.Feature;
import featuresCalculation.FeatureValue;

public class NumberOfSlotsDataset extends Feature<Dataset>{
	
	//Constructors---------------------------------------------------
	
	public NumberOfSlotsDataset() {
		super();
		this.slots = new HashSet<Slot>();
	}
	
	//Properties-----------------------------------------------------
	
	private String className;

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		assert className != null;
		
		this.className = className;
	}
	
	//Internal state-------------------------------------------------
	
	private Set<Slot> slots;
	
	//Interface methods----------------------------------------------

	@Override
	public FeatureValue apply(Dataset element) {
		assert element != null;

		FeatureValue result;
		double value;
		
		value = (double)slots.size();
		result = new FeatureValue();
		result.setFeaturable(element);
		result.setFeature(this);
		result.setValue(value);
		
		return result;
	}
	
	public void addSlot(Slot slot) {
		assert slot != null;
		
		String slotClass;
		
		slotClass = slot.getSlotClass();
		assert slotClass == this.className;
		slots.add(slot);
	}
	
	public String toString() {
		String result;
		
		result = String.format("Number of slots of class %s", className);
		
		return result;
	}
	
}
