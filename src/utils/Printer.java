package utils;

import dataset.Attribute;
import dataset.Record;
import featuresCalculation.features.attribute.CharacterDensity;
import featuresCalculation.features.attribute.NumberOfOccurrences;
import featuresCalculation.features.attribute.TokenDensity;
import featuresCalculation.features.dataset.Mean;
import featuresCalculation.features.dataset.Variance;

public class Printer {
	
	public static String print(Attribute attribute){
		assert attribute != null;
		
		String result;
		
		result = String.format("Attribute %s of class %s with value '%s'", attribute.getName(), attribute.getSlotClass(), attribute.getValue());
		
		return result;
	}
	
	public static String print(Record record){
		assert record != null;
		
		String result;
		
		result = String.format("Record %s of class %s", record.getName(), record.getSlotClass());
		
		return result;
	}
	
	public static String print(Variance variance){
		assert variance != null;
		
		String result;
	    
		result = String.format("Variance of feature %s for slot class %s", variance.getFeature(), variance.getClassName());
		
		return result;
	}
	
	public static String print(Mean mean){
		assert mean != null;
		
		String result;
	    
		result = String.format("Mean of feature %s for slot class %s", mean.getFeature(), mean.getClassName());
		
		return result;
	}
	
	public static String print(TokenDensity tokenDensity){
		assert tokenDensity != null;
		
		String result;
		
		result = String.format("Token density for pattern %s", tokenDensity.getPatternString());
		
		return result;
	}
	
	public static String print(NumberOfOccurrences numberOfOccurrences){
		assert numberOfOccurrences != null;
		
		String result;
		
		result = String.format("Number of Occurrences for pattern %s", numberOfOccurrences.getPatternString());
		
		return result;
	}
	
	public static String print(CharacterDensity characterDensity){
		assert characterDensity != null;
		
		String result;
		
		result = String.format("Token density for pattern %s", characterDensity.getPatternString());
		
		return result;
	}
}
