package featuresCalculation.features.slot;

import java.util.Map;

import dataset.Slot;
import featuresCalculation.Feature;
import featuresCalculation.FeatureValue;
import featuresCalculation.FeaturesVector;

public class NodeDepth extends Feature<Slot>{

	//Interface methods----------------------------------------------
	
	@Override
	public FeatureValue apply(Slot featurable) {
		assert featurable != null;
		
		FeatureValue result;
		Slot parent;
		FeaturesVector featuresVector;
		Map<Feature<?>, FeatureValue> featureValuesMap;
		FeatureValue featureValue;
		double parentValue;
		double value;
		
		if(featurable.getRecord() == null) {
			value = 1.0;
		} else {
			parent = featurable.getRecord();
			featuresVector = parent.getFeaturesVector();
			assert featuresVector != null;
			featureValuesMap = featuresVector.getFeatureValues();
			featureValue = featureValuesMap.get(this);
			parentValue = (Double)featureValue.getValue();
			value = parentValue + 1;
			
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

		result = "Node depth";

		return result;
	}

}
