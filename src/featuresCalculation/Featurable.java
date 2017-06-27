package featuresCalculation;

import java.util.List;

import dataset.Dataset;
import dataset.Record;
import dataset.Slot;

public abstract class Featurable {
	
	//Properties-----------------------------------------------------
	
	private String name;
	private FeaturesVector featuresVector;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		assert name != null;
		
		this.name = name;
	}
	
	public FeaturesVector getFeaturesVector() {
		return featuresVector;
	}

	public void setFeaturesVector(FeaturesVector featuresVector) {
		this.featuresVector = featuresVector;
	}
	
}
