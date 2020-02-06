package featuresCalculation;

public class FeatureValue {
	
	//Properties------------------------------------------------------
	
	private Feature<?> feature;
	private Object value;
	private Featurable featurable;

	public Feature<?> getFeature() {
		return feature;
	}

	public void setFeature(Feature<?> feature) {
		assert feature != null;
		
		this.feature = feature;
		feature.addFeatureValue(this);
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}
	
	public Featurable getFeaturable() {
		return featurable;
	}

	public void setFeaturable(Featurable featurable) {
		assert featurable != null;
		
		this.featurable = featurable;
	}
	
}
