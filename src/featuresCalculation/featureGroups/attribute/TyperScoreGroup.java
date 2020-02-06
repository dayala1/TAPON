package featuresCalculation.featureGroups.attribute;

import java.io.IOException;

import dataset.Attribute;
import featuresCalculation.FeaturesGroup;
import featuresCalculation.IterationType;
import featuresCalculation.features.attribute.TyperScore;

public class TyperScoreGroup extends FeaturesGroup<Attribute>{

	//Constructors---------------------------------------------------
	
	public TyperScoreGroup() {
		super();
		setIterationType(IterationType.DOWN);
	}

	//Interface methods----------------------------------------------

	@Override
	public void initialize() throws IOException {

		TyperScore typerScore;

		typerScore = new TyperScore();
		typerScore.setClassesConfiguration(classesConfiguration);
		typerScore.setIndexPath(getIndexPath());
		addFeature(typerScore);
	}

}
