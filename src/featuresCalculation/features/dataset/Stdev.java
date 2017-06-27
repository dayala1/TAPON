package featuresCalculation.features.dataset;

import dataset.Dataset;
import featuresCalculation.Feature;
import featuresCalculation.FeatureValue;

public class Stdev extends Feature<Dataset>{

	//Properties-----------------------------------------------------
	
	private Feature<?> feature;
	private String className;
	private StatisticsState state;

	//Internal state-------------------------------------------------
	
	public Feature<?> getFeature() {
		return feature;
	}

	public void setFeature(Feature<?> feature) {
		assert feature != null;
	
		this.feature = feature;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		assert className != null;
	
		this.className = className;
	}

	public StatisticsState getState() {
		return state;
	}
	
	public void setState(StatisticsState state) {
		assert state != null;
	
		this.state = state;
	}
	
	//Interface methods----------------------------------------------
	
	@Override
	public FeatureValue apply(Dataset featurable) {
		assert featurable != null;
		assert feature != null;
		assert className != null;
		
		FeatureValue result;
		double value;
		
		value = state.getStdev();
		result = new FeatureValue();
		result.setFeature(this);
		result.setValue(value);
		updateObservers(result);

		return result;
	}

	public String toString(){
		String result;
		
		result = String.format("Standard deviation of feature %s for slot class %s", feature, className);
		
		return result;
	}
}
