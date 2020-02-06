package featuresCalculation.features.classesPair;

import dataset.ClassesPair;
import featuresCalculation.Feature;
import featuresCalculation.FeatureValue;
import org.simmetrics.metrics.Jaro;

public class JaroNameDistance extends Feature<ClassesPair> {
    @Override
    public FeatureValue apply(ClassesPair featurable) {
        FeatureValue result;

        Jaro distanceMetric = new Jaro();
        Double value = new Double(distanceMetric.distance(featurable.getClass1(), featurable.getClass2()));
        result = new FeatureValue();
        result.setFeature(this);
        result.setValue(value);
        result.setFeaturable(featurable);
        updateObservers(result);

        return result;

    }

    @Override
    public String toString() {
        String result;

        result = "Name distance Jaro";

        return result;
    }
}
