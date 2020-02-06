package featuresCalculation.features.attribute;

import dataset.Attribute;
import featuresCalculation.Feature;
import featuresCalculation.FeatureValue;

public class TextualValue extends Feature<Attribute> {

	//Interface methods----------------------------------------------

	@Override
	public FeatureValue apply(Attribute attribute) {
		assert attribute != null;
		FeatureValue result;

		result = new FeatureValue();
		result.setFeature(this);
		result.setValue(attribute.getValue());
		result.setFeaturable(attribute);
		updateObservers(result);
		
		return result;
	}

	@Override
	public String toString() {
		String result;

		result = "Textual value";

		return result;
	}
}
