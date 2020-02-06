package featuresCalculation.featureGroups.slot;

import dataset.Slot;
import featuresCalculation.Feature;
import featuresCalculation.FeaturesGroup;
import featuresCalculation.IterationType;
import featuresCalculation.features.slot.NodeDepth;
import featuresCalculation.features.slot.Source;

public class SourceGroup extends FeaturesGroup<Slot>{
	//Constructors---------------------------------------------------

	public SourceGroup() {
		super();
		setIterationType(IterationType.DOWN);
	}

	//Interface methods----------------------------------------------
	
	public void initialize(){
		Feature<Slot> feature;
		
		feature = new Source();
		addFeature(feature);
	}

}
