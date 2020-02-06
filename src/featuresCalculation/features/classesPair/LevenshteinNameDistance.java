package featuresCalculation.features.classesPair;

import dataset.ClassesPair;
import featuresCalculation.Feature;
import featuresCalculation.FeatureValue;
import org.simmetrics.metrics.Jaro;
import org.simmetrics.metrics.Levenshtein;

public class LevenshteinNameDistance extends Feature<ClassesPair> {
    @Override
    public FeatureValue apply(ClassesPair featurable) {
        FeatureValue result;

        Levenshtein distanceMetric = new Levenshtein();
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

        result = "Name distance Levenshtein";

        return result;
    }
}
