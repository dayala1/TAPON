package featuresCalculation.features.attribute;

import com.google.re2j.Matcher;
import com.google.re2j.Pattern;

import dataset.Attribute;
import featuresCalculation.Feature;
import featuresCalculation.FeatureValue;
import utils.StringUtils;

public class TokenDensity extends Feature<Attribute> {
	
	//Properties-----------------------------------------------------
	
	private String patternString;

	public String getPatternString() {
		return patternString;
	}

	public void setPatternString(String patternString) {
		assert patternString != null;
	
		this.patternString = patternString;
	}
	
	//Internal state-------------------------------------------------
	
	private static String tokenPatternString;
	
	static {
		tokenPatternString = "(^|$|\\p{Z})";
	}
	
	//Interface methods----------------------------------------------

	@Override
	public FeatureValue apply(Attribute attribute) {
		assert attribute != null;
		assert patternString != null;
		
		FeatureValue result;
		String attributeValue;
		Pattern pattern;
		Pattern tokenPattern;
		Matcher occurrencesMatcher;
		Matcher tokensMatcher;
		double numberOfOccurrences;
		double numberOfTokens;
		double value;
		
		attributeValue = attribute.getValue();
		attributeValue = StringUtils.removeAccents(attributeValue);
		pattern = Pattern.compile(patternString);
		tokenPattern = Pattern.compile(tokenPatternString);
		occurrencesMatcher = pattern.matcher(attributeValue);
		tokensMatcher = tokenPattern.matcher(attributeValue);
		numberOfOccurrences = 0.0;
		numberOfTokens = 0.0;
		while(occurrencesMatcher.find()){
			numberOfOccurrences++;
		}
		while(tokensMatcher.find()){
			numberOfTokens++;
		}
		numberOfTokens--;
		if (numberOfOccurrences>0) {
			value = numberOfOccurrences/numberOfTokens;
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
		
		result = String.format("Token density of pattern %s", patternString.replace(',', ';'));
		
		return result;
	}

}
