package featuresCalculation.featureGroups.attribute;

import com.google.re2j.Pattern;

import dataset.Attribute;
import featuresCalculation.FeaturesGroup;
import featuresCalculation.IterationType;
import featuresCalculation.features.attribute.TokenDensity;

public class TokenDensityGroup extends FeaturesGroup<Attribute>{

	//Constructors---------------------------------------------------
	
	public TokenDensityGroup() {
		super();
		setIterationType(IterationType.DOWN);
	}
	
	//Internal state-------------------------------------------------
	
	private static final String[] PATTERNS;
	
	static{
		PATTERNS = new String[] {
				"<[^<>]+>",											//Number of HTML tags
				"\\d+[\\.\\,]?\\d*",								//Number of numeric strings
				"\\b\\p{Lu}+\\b",									//Number of uppercase words
				"\\b\\p{Ll}+[^\\p{Z}<>]*?\\b",						//Number of words starting with a lowercase letter
				"\\b\\p{Lu}+[^\\p{Z}<>]*?\\p{Ll}+[^\\p{Z}<>]*?\\b"	//Number of words starting with an uppercase letter followed by something else
		};
	}
	
	//Interface methods----------------------------------------------
	
	public void initialize(){
		TokenDensity feature;
		
		setIterationType(IterationType.DOWN);
		for (final String patternString : PATTERNS) {
			feature = new TokenDensity();
			feature.setPatternString(patternString);
			addFeature(feature);
		}
	}

}