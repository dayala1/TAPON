package dataset;

import utils.NumberUtils;

public class Attribute extends Slot{

	//Properties------------------------------------------------------
	
	private String value;
	private double[] tfidfVector;
	private Double numericValue;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		assert value != null;
		
		numericValue = NumberUtils.getNumericValue(value);
		
		this.value = value;
	}
	
	public double[] getTfidfVector() {
		return tfidfVector;
	}

	public void setTfidfVector(double[] tfidfVector) {
		assert tfidfVector != null;
	
		this.tfidfVector = tfidfVector;
	}

	public Double getNumericValue() {
		return numericValue;
	}
	
	//Interface methods------------------------------------------------

	public String toString() {
		return String.format("Attribute %s of class %s with value '%s'", getName(), getSlotClass(), getValue());
	}
	
}
