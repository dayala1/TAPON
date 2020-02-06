package featuresCalculation.featureGroups.attribute;

import java.io.IOException;

import dataset.Attribute;
import featuresCalculation.FeaturesGroup;
import featuresCalculation.IterationType;
import featuresCalculation.features.attribute.CosineDistance;
import featuresCalculation.features.attribute.NgramSimilarity;

public class NgramSimilarityGroup extends FeaturesGroup<Attribute>{

	//Constructors---------------------------------------------------
	
	public NgramSimilarityGroup() {
		super();
		setIterationType(IterationType.DOWN);
	}

	//Interface methods----------------------------------------------

	@Override
	public void initialize() throws IOException {

		NgramSimilarity ngramDistance;
		
		for (String slotClass : getAttributeClasses()) {
			ngramDistance = new NgramSimilarity();
			ngramDistance.setIndexPath(getIndexPath());
			//Used by Ngonga
			ngramDistance.setN(3);
			ngramDistance.setClassName(slotClass);
			addFeature(ngramDistance);
		}
	}

}
