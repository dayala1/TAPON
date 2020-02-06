package featuresCalculation.features.record;

import java.util.List;

import dataset.Record;
import dataset.Slot;
import featuresCalculation.Feature;
import featuresCalculation.FeatureValue;

public class Hint_NumberOfSlotsRecord extends Feature<Record>{
	
	//Properties-----------------------------------------------------
	
	private String className;

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		assert className != null;
		
		this.className = className;
	}

	//Interface methods----------------------------------------------
	
	@Override
	public FeatureValue apply(Record element) {
		assert element != null;
		assert className != null;
		
		FeatureValue result;
		List<Slot> children;
		double value;
		
		children = element.getSlots();
		value = 0.0;
		for (Slot slot : children) {
			if(slot.getHint().equals(className)){
				value++;
			}
		}
		
		result = new FeatureValue();
		result.setFeature(this);
		result.setFeaturable(element);
		result.setValue(value);
		updateObservers(result);
		
		return result;
	}
	
	public String toString() {
		String result;
		
		result = String.format("Number of children slots with label %s", className);
		
		return result;
	}
}
