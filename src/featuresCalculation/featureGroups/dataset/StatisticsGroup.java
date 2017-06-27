package featuresCalculation.featureGroups.dataset;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import dataset.Attribute;
import dataset.Dataset;
import dataset.Record;
import dataset.Slot;
import featuresCalculation.Featurable;
import featuresCalculation.Feature;
import featuresCalculation.FeatureValue;
import featuresCalculation.FeaturesGroup;
import featuresCalculation.IterationType;
import featuresCalculation.features.dataset.Maximum;
import featuresCalculation.features.dataset.Mean;
import featuresCalculation.features.dataset.Minimum;
import featuresCalculation.features.dataset.StatisticsState;
import featuresCalculation.features.dataset.Stdev;
import featuresCalculation.features.dataset.Variance;
import observer.Observer;
import utils.ReflectionUtils;

public class StatisticsGroup extends FeaturesGroup<Dataset> implements Observer<FeatureValue>{

	//Constructors---------------------------------------------------
	
	public StatisticsGroup() {
		super();
		setIterationType(IterationType.DOWN);
		this.measuredFeatures = new HashSet<Feature<? extends Slot>>();
		this.featureSlotClassMap = new HashMap<Feature<? extends Slot>, Map<String, StatisticsState>>();
	}

	//Internal state-------------------------------------------------
	
	private Map<Feature<? extends Slot>, Map<String, StatisticsState>> featureSlotClassMap;
	private Set<Feature<? extends Slot>> measuredFeatures;
	
	public void addMeasuredFeature(Feature<? extends Slot> feature) {
		assert feature != null;
		
		this.measuredFeatures.add(feature);
	}
	
	public void addMeasuredFeatures(FeaturesGroup<? extends Slot> featuresGroup) {
		assert featuresGroup != null;
		
		for (Feature<? extends Slot> feature : featuresGroup.getFeatures()) {
			addMeasuredFeature(feature);
		}
	}

	//Interface methods----------------------------------------------

	@Override
	public void initialize() {
		
		Set<String> slotClasses;
		Map<String, StatisticsState> slotClassMap;
		Mean mean;
		Variance variance;
		Stdev stdev;
		Maximum maximum;
		Minimum minimum;
		StatisticsState state;
		
		for (Feature<? extends Slot> measuredFeature : measuredFeatures) {
			slotClasses = new HashSet<String>();
			measuredFeature.addObserver(this);
			if(ReflectionUtils.getParameterClass(measuredFeature).equals(Record.class)) {
				slotClasses.addAll(getRecordClasses());
			} else if(ReflectionUtils.getParameterClass(measuredFeature).equals(Attribute.class)){
				slotClasses.addAll(getAttributeClasses());
			} else {
				slotClasses.addAll(getRecordClasses());
				slotClasses.addAll(getAttributeClasses());
			}
			slotClassMap = new HashMap<String, StatisticsState>();
			for (String slotClass : slotClasses) {
				state = new StatisticsState();
				state.setSlotClass(slotClass);
				slotClassMap.put(slotClass, state);
				
				mean = new Mean();
				variance = new Variance();
				stdev = new Stdev();
				maximum = new Maximum();
				minimum = new Minimum();
				
				mean.setClassName(slotClass);
				variance.setClassName(slotClass);
				stdev.setClassName(slotClass);
				maximum.setClassName(slotClass);
				minimum.setClassName(slotClass);
				
				mean.setFeature(measuredFeature);
				variance.setFeature(measuredFeature);
				stdev.setFeature(measuredFeature);
				maximum.setFeature(measuredFeature);
				minimum.setFeature(measuredFeature);
				
				mean.setState(state);
				variance.setState(state);
				stdev.setState(state);
				maximum.setState(state);
				minimum.setState(state);
				
				addFeature(mean);
				addFeature(variance);
				addFeature(stdev);
				addFeature(maximum);
				addFeature(minimum);
			}
			featureSlotClassMap.put(measuredFeature, slotClassMap);
		}
		
	}

	@Override
	public void update(FeatureValue info) {
		assert info != null;
	
		Feature<?> feature;
		Featurable featurable;
		double value;
		Slot slot;
		String slotClass;
		Map<String, StatisticsState> slotClassMap;
		StatisticsState state;
		
		featurable = info.getFeaturable();
		//Dataset feature values are ignored, since there is ony one value per dataset
		if (featurable instanceof Slot) {
			feature = info.getFeature();
			value = info.getValue();
			slot = (Slot)info.getFeaturable();
			slotClass = slot.getSlotClass();
			slotClassMap = featureSlotClassMap.get(feature);
			state = slotClassMap.get(slotClass);
			state.collect(value);
		}
	}

	//Ancillary methods----------------------------------------------
}
