package featuresCalculation;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class FeaturesGroup<T extends Featurable> implements Serializable{
	
	private static final long serialVersionUID = -1454945192736906727L;
	
	//Constructors---------------------------------------------------

	public FeaturesGroup(){
		this.features = new HashSet<Feature<T>>();
	}
	
	//Properties-----------------------------------------------------
	
	private IterationType iterationType;
	private Set<String> recordClasses;
	private Set<String> attributeClasses;
	private Set<Feature<T>> features;
	private String indexPath;

	public IterationType getIterationType() {
		return iterationType;
	}

	public void setIterationType(IterationType iterationType) {
		assert iterationType != null;
		
		this.iterationType = iterationType;
	}

	public Set<String> getRecordClasses() {
		return recordClasses;
	}

	public void setRecordClasses(Set<String> recordClasses) {
		assert recordClasses != null;
		assert !recordClasses.isEmpty();
		
		this.recordClasses = recordClasses;
	}

	public Set<String> getAttributeClasses() {
		return attributeClasses;
	}

	public void setAttributeClasses(Set<String> attributeClasses) {
		assert attributeClasses != null;
		assert !attributeClasses.isEmpty();
		
		this.attributeClasses = attributeClasses;
	}
	
	public Set<Feature<T>> getFeatures() {
		Set<Feature<T>> result;
		
		result = Collections.unmodifiableSet(features);
		
		return result;
	}
	
	public String getIndexPath() {
		return indexPath;
	}
	
	public void setIndexPath(String indexPath) {
		assert indexPath != null;
		
		this.indexPath = indexPath;
	}
	
	protected void addFeature(Feature<T> feature){
		assert feature != null;
		
		features.add(feature);
	}	
	
	//Interface methods----------------------------------------------
	
	public FeaturesVector apply(T featurable) {
		assert featurable != null;
		assert iterationType != null;
		assert recordClasses != null;
		assert attributeClasses != null;
		
		FeaturesVector result;
		FeatureValue featureValue;
		
		result = new FeaturesVector();
		
		for (Feature<T> feature : features) {
			featureValue = feature.apply(featurable);
			result.addFeatureValue(featureValue);
		}
		
		return result;
	}
	
	public Integer getNumberOfFeatures() {
		return features.size();
	}
	
	public abstract void initialize() throws IOException;
	
}
