package featuresCalculation.features.slot;

import dataset.Attribute;
import dataset.Slot;
import featuresCalculation.Featurable;
import featuresCalculation.Feature;
import featuresCalculation.FeatureValue;

public class Source extends Feature<Slot> {

	//Interface methods----------------------------------------------

	@Override
	public FeatureValue apply(Slot slot) {
		assert slot != null;
		assert slot.getSource() != null;
		FeatureValue result;

		result = new FeatureValue();
		result.setFeature(this);
		result.setValue(slot.getSource());
		result.setFeaturable(slot);
		updateObservers(result);
		
		return result;
	}

	@Override
	public String toString() {
		String result;

		result = "Source";

		return result;
	}
}
