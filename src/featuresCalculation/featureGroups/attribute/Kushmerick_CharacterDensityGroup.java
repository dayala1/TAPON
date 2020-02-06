package featuresCalculation.featureGroups.attribute;

import com.google.re2j.Pattern;

import dataset.Attribute;
import featuresCalculation.FeaturesGroup;
import featuresCalculation.IterationType;
import featuresCalculation.features.attribute.CharacterDensity;

public class Kushmerick_CharacterDensityGroup extends FeaturesGroup<Attribute>{
	
	//Constructors---------------------------------------------------

	public Kushmerick_CharacterDensityGroup() {
		super();
		setIterationType(IterationType.DOWN);
	}
	
	//Internal state-------------------------------------------------
	
	private static final String[] PATTERNS;
	
	static{
		PATTERNS = new String[] {
				"\\p{L}",
				"\\p{N}", 
				"\\p{Lu}",
				"\\p{Ll}",
				"\\p{P}"
		};
	}
	
	//Interface methods----------------------------------------------
	
	public void initialize(){
		CharacterDensity feature;
		
		for (String patternString : PATTERNS) {
			feature = new CharacterDensity();
			feature.setPatternString(patternString);
			addFeature(feature);
		}
	}

}
