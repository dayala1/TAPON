package featuresCalculation.featureGroups.featurable;

import java.util.HashSet;
import java.util.Set;

import featuresCalculation.Featurable;
import featuresCalculation.FeaturesGroup;
import featuresCalculation.IterationType;
import featuresCalculation.features.featurable.Hint_MinimumTreeDistance;

public class Hint_MinimumTreeDistanceGroup extends FeaturesGroup<Featurable>{

	//Constructors---------------------------------------------------
	
	public Hint_MinimumTreeDistanceGroup() {
		super();
		setIterationType(IterationType.UP_DOWN);
	}

	//Interface methods----------------------------------------------

	@Override
	public void initialize() {

		Hint_MinimumTreeDistance minimumTreeDistance;
		Set<String> slotClasses;
		
		slotClasses = new HashSet<String>();
		slotClasses.addAll(getRecordClasses());
		slotClasses.addAll(getAttributeClasses());
		
		for (String slotClass : slotClasses) {
			minimumTreeDistance = new Hint_MinimumTreeDistance();
			minimumTreeDistance.setSlotClass(slotClass);
			addFeature(minimumTreeDistance);
		}
		
	}

}
