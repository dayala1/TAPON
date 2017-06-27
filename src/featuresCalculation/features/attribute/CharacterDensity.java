package featuresCalculation.features.attribute;

import com.google.re2j.Matcher;
import com.google.re2j.Pattern;

import dataset.Attribute;
import featuresCalculation.Feature;
import featuresCalculation.FeatureValue;
import utils.StringUtils;

public class CharacterDensity extends Feature<Attribute> {

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
		
		FeatureValue result;
		String attributeValue;
		Pattern pattern;
		Matcher occurrencesMatcher;
		double numberOfOccurrences;
		double numberOfCharacters;
		double value;
		
		pattern = Pattern.compile(patternString);
		attributeValue = attribute.getValue();
		attributeValue = StringUtils.removeAccents(attributeValue);
		occurrencesMatcher = pattern.matcher(attributeValue);
		numberOfOccurrences = 0.0;
		numberOfCharacters = (double)attributeValue.length();
		while(occurrencesMatcher.find()){
			numberOfOccurrences++;
		}
		if (numberOfOccurrences>0) {
			value = numberOfOccurrences/numberOfCharacters;
		} else {
			value = 0.0;
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
		
		result = String.format("Character density for pattern %s", patternString.replace(',', ';'));
		
		return result;
	}

}
