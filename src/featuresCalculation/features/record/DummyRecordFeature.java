package featuresCalculation.features.record;

import dataset.Record;
import dataset.Slot;
import featuresCalculation.Feature;
import featuresCalculation.FeatureValue;

import java.util.List;

public class DummyRecordFeature extends Feature<Record>{

	//Interface methods----------------------------------------------
	
	@Override
	public FeatureValue apply(Record featurable) {
		assert featurable != null;
		
		FeatureValue result;
		double value;
		
		value = 0.0;
		result = new FeatureValue();
		result.setFeature(this);
		result.setValue(value);
		result.setFeaturable(featurable);
		updateObservers(result);
		
		return result;
	}

	public String toString(){
		String result;

		result = "Dummy record feature";

		return result;
	}
}
