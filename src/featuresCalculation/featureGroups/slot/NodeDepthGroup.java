package featuresCalculation.featureGroups.slot;

import dataset.Slot;
import featuresCalculation.Feature;
import featuresCalculation.FeaturesGroup;
import featuresCalculation.IterationType;
import featuresCalculation.features.slot.NodeDepth;

public class NodeDepthGroup extends FeaturesGroup<Slot>{
	//Constructors---------------------------------------------------
	
	public NodeDepthGroup() {
		super();
		setIterationType(IterationType.DOWN);
	}

	//Interface methods----------------------------------------------
	
	public void initialize(){
		Feature<Slot> feature;
		
		feature = new NodeDepth();
		addFeature(feature);
	}

}
