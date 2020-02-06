package featuresCalculation.features.attribute;

import com.google.re2j.Matcher;
import com.google.re2j.Pattern;

import dataset.Attribute;
import featuresCalculation.Feature;
import featuresCalculation.FeatureValue;
import utils.StringUtils;

public class NumberOfOccurrences extends Feature<Attribute> {
	
	//Properties-----------------------------------------------------
	
	private String patternString;
	
	public String getPatternString() {
		return patternString;
	}

	public void setPatternString(String patternString) {
		assert patternString != null;
	
		this.patternString = patternString;
	}

	//Interface methods----------------------------------------------

	@Override
	public FeatureValue apply(Attribute attribute) {
		assert attribute != null;
		assert patternString != null;
		
		FeatureValue result;
		String attributeValue;
		Pattern pattern;
		Matcher matcher;
		double value;
		
		attributeValue = attribute.getValue();
		attributeValue = StringUtils.removeAccents(attributeValue);
		pattern = Pattern.compile(patternString);
		matcher = pattern.matcher(attributeValue);
		value = 0.0;
		while(matcher.find()){
			value++;
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
		
		result = String.format("Number of occurrences of pattern %s", patternString.replace(',', ';'));
		
		return result;
	}

}
