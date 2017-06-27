package featuresCalculation.featureGroups.slot;

import dataset.Slot;
import featuresCalculation.Feature;
import featuresCalculation.FeaturesGroup;
import featuresCalculation.IterationType;
import featuresCalculation.features.slot.AbsolutePreviousLocation;
import featuresCalculation.features.slot.AbsoluteRootLocation;
import featuresCalculation.features.slot.LengthLocation;
import featuresCalculation.features.slot.PreviousLocation;
import featuresCalculation.features.slot.RelativeRootLocation;
import featuresCalculation.features.slot.RootLocation;

public class OliveiraDaSilvaGroup extends FeaturesGroup<Slot>{
	//Constructors---------------------------------------------------
	
	public OliveiraDaSilvaGroup() {
		super();
		setIterationType(IterationType.DOWN);
	}

	//Interface methods----------------------------------------------
	
	public void initialize(){
		Feature<Slot> feature;
		
		feature = new RootLocation();
		addFeature(feature);
		feature = new AbsoluteRootLocation();
		addFeature(feature);
		feature = new RelativeRootLocation();
		addFeature(feature);
		feature = new PreviousLocation();
		addFeature(feature);
		feature = new AbsolutePreviousLocation();
		addFeature(feature);
		feature = new LengthLocation();
		addFeature(feature);
	}

}
