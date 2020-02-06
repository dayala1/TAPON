package featuresCalculation.features.attribute;

import com.google.re2j.Matcher;
import com.google.re2j.Pattern;

import dataset.Attribute;
import featuresCalculation.Feature;
import featuresCalculation.FeatureValue;
import utils.StringUtils;

public class NumericValue extends Feature<Attribute> {

	//Interface methods----------------------------------------------

	@Override
	public FeatureValue apply(Attribute attribute) {
		assert attribute != null;
		
		FeatureValue result;
		Double value;
		
		value = attribute.getNumericValue();
		if (value == null) {
			value = -1.0;
		}
		result = new FeatureValue();
		result.setFeature(this);
		result.setValue(value);
		result.setFeaturable(attribute);
		updateObservers(result);
		
		return result;
	}

	@Override
	public String toString() {
		String result;

		result = "Numeric value";

		return result;
	}
}
