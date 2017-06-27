package featuresCalculation.features.dataset;

import java.io.Serializable;

public class StatisticsState implements Serializable{

	private static final long serialVersionUID = 5283837822091089986L;
	
	//Constructors---------------------------------------------------

	public StatisticsState() {
		mean = 0.0;
		stdev = 0.0;
		variance = 0.0;
		size = 0.0;
		sum = 0.0;
		min = 999999;
		max = -999999;
	}

	//Properties-----------------------------------------------------
	
	private double mean, stdev, variance, size, m2, min, max, sum;
	private String slotClass;
	
	public double getMean() {
		return mean;
	}

	public double getVariance() {
		return variance;
	}

	public double getStdev() {
		return stdev;
	}

	public double getSum() {
		return sum;
	}

	public double getMinimum() {
		return min;
	}
	
	public double getMaximum() {
		return max;
	}
	
	public String getSlotClass() {
		return slotClass;
	}

	public void setSlotClass(String slotClass) {
		assert slotClass != null;
	
		this.slotClass = slotClass;
	}

	//Interface methods----------------------------------------------

	public void collect(double observation) {

		double delta;

		size = size + 1;
		sum += observation;
		delta = observation - mean;
		mean = mean + delta / size;
		m2 = m2 + delta * (observation - mean);
		if (size > 1) {
			variance = m2 / (size - 1);
			stdev = Math.sqrt(variance);
		}
		if (observation < min)
			min = observation;
		if (observation > max)
			max = observation;
		
	}

}
