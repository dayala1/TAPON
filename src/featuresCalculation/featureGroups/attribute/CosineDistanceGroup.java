package featuresCalculation.featureGroups.attribute;

import java.io.IOException;

import dataset.Attribute;
import featuresCalculation.FeaturesGroup;
import featuresCalculation.IterationType;
import featuresCalculation.features.attribute.CosineDistance;
import featuresCalculation.features.attribute.EditDistance;
import utils.LuceneUtils;

public class CosineDistanceGroup extends FeaturesGroup<Attribute>{

	//Constructors---------------------------------------------------
	
	public CosineDistanceGroup() {
		super();
		setIterationType(IterationType.DOWN);
	}

	//Interface methods----------------------------------------------

	@Override
	public void initialize() throws IOException {

		CosineDistance cosineDistance;
		
		for (String slotClass : getAttributeClasses()) {
			cosineDistance = new CosineDistance();
			cosineDistance.setIndexPath(getIndexPath());
			cosineDistance.setClassName(slotClass);
			addFeature(cosineDistance);
		}
		
		LuceneUtils.prepareVocabulary("value", getIndexPath(), getIndexPath());
	}

}
