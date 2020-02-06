package featuresCalculation.features.slot;

import java.util.List;

import dataset.Attribute;
import dataset.Dataset;
import dataset.Record;
import dataset.Slot;
import featuresCalculation.Featurable;
import featuresCalculation.Feature;
import featuresCalculation.FeatureValue;

public class Hint_DensityOfBrothers extends Feature<Slot>{

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
	public FeatureValue apply(Slot featurable) {
		assert featurable != null;
		
		FeatureValue result;
		Featurable parent;
		List<Slot> children;
		double value;
		
		if(featurable.getRecord() == null) {
			parent = featurable.getDataset();
			children = ((Dataset)parent).getSlots();
		} else {
			parent = featurable.getRecord();
			children = ((Record)parent).getSlots();
		}
		value = 0.0;
		for (Slot slot : children) {
			if(slot.getHint().equals(className)){
				value++;
			}
		}
		
		value = value/children.size();
		
		result = new FeatureValue();
		result.setFeature(this);
		result.setValue(value);
		result.setFeaturable(featurable);
		updateObservers(result);
		
		return result;
	}

	public String toString(){
		String result;

		result =String.format("Density of siblings with label %s", className);

		return result;
	}
}
