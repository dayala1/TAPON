package featuresCalculation.featureGroups.attribute;

import dataset.Attribute;
import featuresCalculation.FeaturesGroup;
import featuresCalculation.IterationType;
import featuresCalculation.features.attribute.EditDistance;
import featuresCalculation.features.attribute.Pham_DistributionSimilarity;

import java.io.IOException;

public class Pham_DistributionSimilarityGroup extends FeaturesGroup<Attribute>{

	//Constructors---------------------------------------------------

	public Pham_DistributionSimilarityGroup() {
		super();
		setIterationType(IterationType.DOWN);
	}

	//Interface methods----------------------------------------------

	@Override
	public void initialize() throws IOException {

		Pham_DistributionSimilarity distributionSimilarity;
		
		for (String slotClass : getAttributeClasses()) {
			if(this.classesConfiguration.getIsNumeric(slotClass)) {
				distributionSimilarity = new Pham_DistributionSimilarity();
				distributionSimilarity.setIndexPath(getIndexPath());
				distributionSimilarity.setClassName(slotClass);
				addFeature(distributionSimilarity);
			}
		}
	}

}
