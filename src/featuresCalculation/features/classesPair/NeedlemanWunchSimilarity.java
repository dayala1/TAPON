package featuresCalculation.features.classesPair;

import dataset.ClassesPair;
import featuresCalculation.Feature;
import featuresCalculation.FeatureValue;
import org.simmetrics.metrics.Jaro;
import org.simmetrics.metrics.NeedlemanWunch;

public class NeedlemanWunchSimilarity extends Feature<ClassesPair> {
    @Override
    public FeatureValue apply(ClassesPair featurable) {
        FeatureValue result;

        NeedlemanWunch metric = new NeedlemanWunch();

        Double value = new Double(metric.compare(featurable.getClass1(), featurable.getClass2()));
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

        result = "Name similarity Needleman Wunch";

        return result;
    }
}
