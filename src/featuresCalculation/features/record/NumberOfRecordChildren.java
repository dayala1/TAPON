package featuresCalculation.features.record;

import java.util.List;

import dataset.Attribute;
import dataset.Dataset;
import dataset.Record;
import dataset.Slot;
import featuresCalculation.Featurable;
import featuresCalculation.Feature;
import featuresCalculation.FeatureValue;

public class NumberOfRecordChildren extends Feature<Record>{

	//Interface methods----------------------------------------------
	
	@Override
	public FeatureValue apply(Record featurable) {
		assert featurable != null;
		
		FeatureValue result;
		List<Slot> children;
		double value;
		
		children = featurable.getSlots();
		value = 0.0;
		for (Slot slot : children) {
			if (slot instanceof Record) {
				value++;
			}
		}
		
		result = new FeatureValue();
		result.setFeature(this);
		result.setValue(value);
		result.setFeaturable(featurable);
		updateObservers(result);
		
		return result;
	}

	public String toString(){
		String result;

		result = "Number of records among the children";

		return result;
	}
}
