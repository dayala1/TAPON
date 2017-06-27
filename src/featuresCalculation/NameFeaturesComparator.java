package featuresCalculation;

import java.util.Comparator;

public class NameFeaturesComparator implements Comparator<Feature<?>>{

	//Interface methods----------------------------------------------
	
	@Override
	public int compare(Feature<?> f1, Feature<?> f2) {
		assert f1 != null;
		
		int result;
		String featureName1;
		String featureName2;
		
		featureName1 = f1.toString();
		featureName2 = f2.toString();
		result = featureName1.compareTo(featureName2);
		
		return result;
	}

}
