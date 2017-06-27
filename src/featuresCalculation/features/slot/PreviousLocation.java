package featuresCalculation.features.slot;

import java.util.List;

import dataset.Dataset;
import dataset.Record;
import dataset.Slot;
import featuresCalculation.Featurable;
import featuresCalculation.Feature;
import featuresCalculation.FeatureValue;

public class PreviousLocation extends Feature<Slot>{

	//Interface methods----------------------------------------------
	
	@Override
	public FeatureValue apply(Slot featurable) {
		assert featurable != null;
		
		FeatureValue result;
		Featurable parent;
		List<Slot> children;
		int slotIndex;
		Slot previousSlot;
		Slot currentSlot;
		double value;
		
		if(featurable.getRecord() == null) {
			parent = featurable.getDataset();
			children = ((Dataset)parent).getSlots();
		} else {
			parent = featurable.getRecord();
			children = ((Record)parent).getSlots();
		}
		slotIndex = children.indexOf(featurable);
		if(slotIndex == 0) {
			value = 0.0;
		} else {
			previousSlot = children.get(slotIndex-1);
			value = featurable.getStartIndex() - previousSlot.getStartIndex();
			for(int i = 1; i<slotIndex; i++) {
				currentSlot = children.get(i);
				value = value - currentSlot.getEndIndex();
				value = value + currentSlot.getStartIndex();
			}
		}
		
		result = new FeatureValue();
		result.setFeature(this);
		result.setValue(value);
		result.setFeaturable(featurable);
		updateObservers(result);
		
		return result;
	}
}
