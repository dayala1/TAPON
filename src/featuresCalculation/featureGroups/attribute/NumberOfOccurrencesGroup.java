package featuresCalculation.featureGroups.attribute;

import com.google.re2j.Pattern;

import dataset.Attribute;
import featuresCalculation.FeaturesGroup;
import featuresCalculation.IterationType;
import featuresCalculation.features.attribute.NumberOfOccurrences;

public class NumberOfOccurrencesGroup extends FeaturesGroup<Attribute>{
	
	//Constructors---------------------------------------------------

	public NumberOfOccurrencesGroup() {
		super();
		setIterationType(IterationType.DOWN);
	}
	
	
	//Internal state-------------------------------------------------
	
	private static final String[] PATTERNS;
	
	static{
		PATTERNS = new String[] {
				"\\p{L}",
				"\\p{Lu}",
				"\\p{Ll}",
				"\\p{M}",
				"\\p{N}", 
				"\\p{C}",
				"\\p{P}",
				"\\p{S}",
				"\\p{Z}",							
				"<[^<>]+>",											//Number of HTML tags
				"\\d+[\\.\\,]?\\d*",								//Number of numeric strings
				"\\b\\p{Lu}+\\b",									//Number of uppercase words
				"\\b\\p{Ll}+[^\\p{Z}<>]*?\\b",						//Number of words starting with a lowercase letter
				"\\b\\p{Lu}+[^\\p{Z}<>]*?\\p{Ll}+[^\\p{Z}<>]*?\\b"	//Number of words starting with an uppercase letter followed by something else
		};
	}
	
	//Interface methods----------------------------------------------
	
	public void initialize(){
		NumberOfOccurrences feature;
		
		for (String patternString : PATTERNS) {
			feature = new NumberOfOccurrences();
			feature.setPatternString(patternString);
			addFeature(feature);
		}
	}

}
