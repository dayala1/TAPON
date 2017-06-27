package featuresCalculation.featureGroups.attribute;

import java.io.IOException;

import dataset.Attribute;
import featuresCalculation.FeaturesGroup;
import featuresCalculation.IterationType;
import featuresCalculation.features.attribute.CommonPrefixLength;
import featuresCalculation.features.attribute.CommonSuffixLength;

public class CommonPrefixSuffixLengthGroup extends FeaturesGroup<Attribute>{

	//Constructors---------------------------------------------------
	
	public CommonPrefixSuffixLengthGroup() {
		super();
		setIterationType(IterationType.DOWN);
	}

	//Interface methods----------------------------------------------

	@Override
	public void initialize() throws IOException {

		CommonPrefixLength commonPrefixLength;
		CommonSuffixLength commonSuffixLength;
		
		for (String slotClass : getAttributeClasses()) {
			commonPrefixLength = new CommonPrefixLength();
			commonPrefixLength.setIndexPath(getIndexPath());
			commonPrefixLength.setClassName(slotClass);
			addFeature(commonPrefixLength);
			commonSuffixLength = new CommonSuffixLength();
			commonSuffixLength.setIndexPath(getIndexPath());
			commonSuffixLength.setClassName(slotClass);
			addFeature(commonSuffixLength);
		}
	}

}
