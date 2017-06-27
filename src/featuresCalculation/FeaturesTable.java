package featuresCalculation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FeaturesTable {
	
	//Constructors----------------------------------------------------
	
	public FeaturesTable() {

		this.featuresVectors = new ArrayList<FeaturesVector>();
		
	}
	
	//Properties------------------------------------------------------
	
	private String name;
	private List<FeaturesVector> featuresVectors;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		assert name != null;
		
		this.name = name;
	}

	public List<FeaturesVector> getFeaturesVectors() {
		List<FeaturesVector> result;
		
		result = Collections.unmodifiableList(featuresVectors);
		
		return result;
	}
	
	public void addFeaturesVector(FeaturesVector featuresVector) {
		assert featuresVector != null;
		
		featuresVectors.add(featuresVector);
	}
	
	public void removeFeaturesVector(FeaturesVector featuresVector) {
		assert featuresVector != null;
		assert contains(featuresVector);
		
		featuresVectors.remove(featuresVector);
	}
	
	//Interface methods----------------------------------------------
	
	public void removeFeaturesVectorFromFeaturable(Featurable featurable) {
		assert featurable != null;

		for (FeaturesVector featuresVector : featuresVectors) {
			if(featuresVector.getFeaturable().equals(featurable)){
				removeFeaturesVector(featuresVector);
			}
		}
	}
	
	public boolean contains(FeaturesVector featuresVector){
		assert featuresVector != null;
		
		boolean result;
		
		result = featuresVectors.contains(featuresVector);
		
		return result;
	}
	
}
