package featuresCalculation.features.featurable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dataset.Dataset;
import dataset.Record;
import dataset.Slot;
import featuresCalculation.Featurable;
import featuresCalculation.Feature;
import featuresCalculation.FeatureValue;
import featuresCalculation.FeaturesVector;

public class Hint_MinimumTreeDistance extends Feature<Featurable>{
	
	//Properties-----------------------------------------------------
	
	private String slotClass;
	
	public String getSlotClass() {
		return slotClass;
	}

	public void setSlotClass(String slotClass) {
		assert slotClass != null;
	
		this.slotClass = slotClass;
	}

	//Interface methods----------------------------------------------

	@Override
	public FeatureValue apply(Featurable featurable) {
		assert featurable != null;
		
		FeatureValue result;
		double value;
		double minFamiliarValue;
		Slot slot;
		
		value = 999;
		
		if (featurable instanceof Slot) {
			slot = (Slot)featurable;
			if (slot.getHint().equals(slotClass)) {
				value = 0.0;
			}
		}
		if(value != 0.0) {
			minFamiliarValue = minFamiliar(featurable);
			value = Math.min(value, minFamiliarValue+1);
			if(value == 999) {
				value = -1.0;
			}
		}
		
		result = new FeatureValue();
		result.setFeaturable(featurable);
		result.setFeature(this);
		result.setValue(value);
		//Observers are only updated when the final value is obtained
		if(featurable.getFeaturesVector().getFeatureValues().containsKey(this)){
			updateObservers(result);
		}
		
		return result;
	}
	
	//Ancillary methods----------------------------------------------
	
	protected double minFamiliar(Featurable featurable) {
		assert featurable != null;
		assert slotClass != null;
		
		double result;
		Set<Featurable> family;
		List<Slot> children;
		Featurable parent;
		FeaturesVector featuresVector;
		Map<Feature<?>, FeatureValue> featureValuesMap;
		FeatureValue featureValue;
		double familiarValue;
		
		result = 999; 
		family = new HashSet<Featurable>();
		if(featurable instanceof Dataset) {
			children = ((Dataset)featurable).getSlots();
			family.addAll(children);
		} else {
			parent = ((Slot)featurable).getParent();
			children = new ArrayList<Slot>();
			if(featurable instanceof Record) {
				children = ((Record)featurable).getSlots();
			}
			family.add(parent);
			family.addAll(children);
		}
		
		for (Featurable familyFeaturable : family) {
			featuresVector = familyFeaturable.getFeaturesVector();
			assert featuresVector != null;
			featureValuesMap = featuresVector.getFeatureValues();
			featureValue = featureValuesMap.get(this);
			if (featureValue != null) {
				familiarValue = featureValue.getValue();
				if(familiarValue >= 0) {
					result = Math.min(result, familiarValue);
				}
			}
		}
		
		return result;
	}
	
	public String toString() {
		String result;
		
		result = String.format("Minimum tree distance to hint %s", slotClass);
		
		return result;
	}

}
