package featuresCalculation.featureGroups.attribute;

import java.io.IOException;

import dataset.Attribute;
import featuresCalculation.FeaturesGroup;
import featuresCalculation.IterationType;
import featuresCalculation.features.attribute.EditDistance;

public class EditDistanceGroup extends FeaturesGroup<Attribute>{

	//Constructors---------------------------------------------------
	
	public EditDistanceGroup() {
		super();
		setIterationType(IterationType.DOWN);
	}

	//Interface methods----------------------------------------------

	@Override
	public void initialize() throws IOException {

		EditDistance editDistance;
		
		for (String slotClass : getAttributeClasses()) {
			editDistance = new EditDistance();
			editDistance.setIndexPath(getIndexPath());
			editDistance.setClassName(slotClass);
			addFeature(editDistance);
		}
	}

}
