package featuresCalculation.features.attribute;

import java.util.List;

import org.spark_project.guava.collect.Lists;

import com.google.re2j.Matcher;
import com.google.re2j.Pattern;

import dataset.Attribute;
import featuresCalculation.Feature;
import featuresCalculation.FeatureValue;
import utils.StringUtils;

public class Kushmerick_MeanWordLength extends Feature<Attribute> {
	
	//Interface methods----------------------------------------------

	@Override
	public FeatureValue apply(Attribute attribute) {
		assert attribute != null;
		
		FeatureValue result;
		String attributeValue;
		Pattern pattern;
		Matcher matcher;
		Double numWords;
		Double totalWordLength;
		double value;
		
		numWords = 0.0;
		totalWordLength = 0.0;
		attributeValue = attribute.getValue();
		attributeValue = StringUtils.removeAccents(attributeValue);
		pattern = Pattern.compile("\\b\\w+\\b");
		matcher = pattern.matcher(attributeValue);
		value = 0.0;
		while(matcher.find()){
			totalWordLength += matcher.group().length();
			numWords += 1.0;
		}
		if(numWords>0){
			value = totalWordLength/numWords;
		} else{
			value = 0.0;
		}
		result = new FeatureValue();
		result.setFeature(this);
		result.setValue(value);
		result.setFeaturable(attribute);
		updateObservers(result);
		
		return result;
	}

	public String toString(){
		String result;

		result = "Mean word length";

		return result;
	}
	
}
