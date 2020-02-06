package featuresCalculation.featureGroups.attribute;

import com.google.re2j.Pattern;

import dataset.Attribute;
import featuresCalculation.FeaturesGroup;
import featuresCalculation.IterationType;
import featuresCalculation.features.attribute.NumberOfOccurrences;

public class Kushmerick_NumberOfOccurrencesGroup extends FeaturesGroup<Attribute>{
	
	//Constructors---------------------------------------------------

	public Kushmerick_NumberOfOccurrencesGroup() {
		super();
		setIterationType(IterationType.DOWN);
	}
	
	
	//Internal state-------------------------------------------------
	
	private static final String[] PATTERNS;
	
	static{
		PATTERNS = new String[] {
				"\\b\\.+\\b",									//Number of words
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
