package featuresCalculation.featureGroups.attribute;

import com.google.re2j.Pattern;

import dataset.Attribute;
import featuresCalculation.FeaturesGroup;
import featuresCalculation.IterationType;
import featuresCalculation.features.attribute.CharacterDensity;

public class CharacterDensityGroup extends FeaturesGroup<Attribute>{
	
	//Constructors---------------------------------------------------

	public CharacterDensityGroup() {
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
				"\\p{Z}"
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
