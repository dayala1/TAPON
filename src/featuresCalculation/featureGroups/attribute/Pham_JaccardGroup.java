package featuresCalculation.featureGroups.attribute;

import dataset.Attribute;
import featuresCalculation.Feature;
import featuresCalculation.FeaturesGroup;
import featuresCalculation.IterationType;
import featuresCalculation.features.attribute.CommonPrefixLength;
import featuresCalculation.features.attribute.CommonSuffixLength;
import featuresCalculation.features.attribute.Pham_JaccardNumeric;
import featuresCalculation.features.attribute.Pham_JaccardTextual;

import java.io.IOException;

public class Pham_JaccardGroup extends FeaturesGroup<Attribute>{

	//Constructors---------------------------------------------------

	public Pham_JaccardGroup() {
		super();
		setIterationType(IterationType.DOWN);
	}

	//Interface methods----------------------------------------------

	@Override
	public void initialize() throws IOException {

		Pham_JaccardNumeric jaccardNumeric;
		Pham_JaccardTextual jaccardTextual;
		
		for (String slotClass : getAttributeClasses()) {
			if(this.classesConfiguration.getIsNumeric(slotClass)){
				jaccardNumeric = new Pham_JaccardNumeric();
				jaccardNumeric.setIndexPath(getIndexPath());
				jaccardNumeric.setClassName(slotClass);
				addFeature(jaccardNumeric);
			} else {
				jaccardTextual = new Pham_JaccardTextual();
				jaccardTextual.setIndexPath(getIndexPath());
				jaccardTextual.setClassName(slotClass);
				addFeature(jaccardTextual);
			}
		}
	}

}
