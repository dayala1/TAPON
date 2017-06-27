package featuresCalculation;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import observer.Observable;
import observer.Observer;

public abstract class Feature<T extends Featurable> extends Observable<FeatureValue> implements Serializable{
	
	private static final long serialVersionUID = 1902492196004211607L;

	//Constructors---------------------------------------------------

	public Feature(){
		super();
		this.featureValues = new HashSet<FeatureValue>();
	}
	
	//Properties-----------------------------------------------------
	
	private Set<FeatureValue> featureValues;
	
	public void addFeatureValue(FeatureValue featureValue){
		assert featureValue != null;
		
		featureValues.add(featureValue);
	}
	
	//Interface methods----------------------------------------------
	
	public abstract FeatureValue apply(T featurable);
	
	public void updateObservers(FeatureValue featureValue){
		assert featureValue!=null;
		
		for (Observer<FeatureValue> observer : getObservers()) {
			observer.update(featureValue);
		}
	}
}