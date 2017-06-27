package featuresCalculation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class FeaturesVector {
	
	//Constructors----------------------------------------------------
	
	public FeaturesVector(){

		Comparator<Feature<?>> comparator;
		
		comparator = new NameFeaturesComparator();
		
		this.featureValues = new TreeMap<Feature<?>, FeatureValue>(comparator);
	}
	
	//Properties------------------------------------------------------
	
	private SortedMap<Feature<?>, FeatureValue> featureValues;
	private Featurable featurable;
	private String vectorClass;

	public SortedMap<Feature<?>, FeatureValue> getFeatureValues() {
		SortedMap<Feature<?>, FeatureValue> result;
		
		result = Collections.unmodifiableSortedMap(featureValues);
		
		return result;
	}

	public Featurable getFeaturable() {
		return featurable;
	}

	public void setFeaturable(Featurable featurable) {
		assert featurable != null;
		
		this.featurable = featurable;
		featurable.setFeaturesVector(this);
	}
	
	public String getVectorClass() {
		return vectorClass;
	}

	public void setVectorClass(String vectorClass) {
		assert vectorClass != null;
		
		this.vectorClass = vectorClass;
	}

	public void addFeatureValue(FeatureValue featureValue) {
		assert featureValue != null;
		
		Feature<?> feature;
		
		feature = featureValue.getFeature();
		featureValues.put(feature, featureValue);
	}
	
	public void removeFeatureValues(Feature<?> feature) {
		assert feature != null;
		assert contains(feature);
		
		featureValues.remove(feature);
	}
	
	//Interface methods------------------------------------------------
	
	public boolean contains(Feature<?> feature){
		assert feature != null;
		
		boolean result;
		
		result = featureValues.containsKey(feature);
		
		return result;
	}
	
	public List<String> getRawValues() {
		List<String> result;
		String value;
		
		result = new ArrayList<String>();
		for (FeatureValue featureValue : getFeatureValues().values()) {
			value = Double.toString(featureValue.getValue());
			result.add(value);
		}
		
		if(vectorClass != null) {
			result.add(vectorClass);
		}
		
		return result;
	}
	
	public FeaturesVector clone(){
		FeaturesVector result;
		
		result = new FeaturesVector();
		result.setVectorClass(vectorClass);
		for (FeatureValue featureValue : featureValues.values()) {
			result.addFeatureValue(featureValue);
		}
		result.setFeaturable(featurable);
		
		return result;
	}
	
}
