package featuresCalculation.features.classesPair;

import dataset.ClassesPair;
import featuresCalculation.Feature;
import featuresCalculation.FeatureValue;
import org.simmetrics.metrics.MongeElkan;
import org.simmetrics.metrics.NeedlemanWunch;
import org.simmetrics.metrics.SmithWatermanGotoh;

public class SmithWatermanGotohSimilarity extends Feature<ClassesPair> {
    @Override
    public FeatureValue apply(ClassesPair featurable) {
        FeatureValue result;

        SmithWatermanGotoh metric = new SmithWatermanGotoh();

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

        result = " Name similarity Smith Waterman Gotoh";

        return result;
    }
}
