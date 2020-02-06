package featuresCalculation.featureGroups.attribute;

import com.google.re2j.Pattern;

import dataset.Attribute;
import featuresCalculation.FeaturesGroup;
import featuresCalculation.IterationType;
import featuresCalculation.features.attribute.TokenDensity;

public class Kushmerick_TokenDensityGroup extends FeaturesGroup<Attribute>{

	//Constructors---------------------------------------------------
	
	public Kushmerick_TokenDensityGroup() {
		super();
		setIterationType(IterationType.DOWN);
	}
	
	//Internal state-------------------------------------------------
	
	private static final String[] PATTERNS;
	
	static{
		PATTERNS = new String[] {
				"<[^<>]+>"											//Number of HTML tags
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