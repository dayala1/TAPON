package dataset;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.clearspring.analytics.util.Lists;

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
	
	@Override
	public JSONObject getJSONObject() {
		JSONObject res;
		JSONArray children;
		LinkedHashMap<String, Double> ranking;
		List<Entry<String, Double>> entries;
		
		res = new JSONObject();
		if (this.getSlotClass() != null) {
			res.put("class", this.getSlotClass());
		}
		if (this.getName() != null) {
			res.put("name", this.getName());
		}
		if (this.getHint() != null) {
			res.put("hint", this.getHint());
			ranking = this.getHintsRanking();
			entries = Lists.newArrayList(ranking.entrySet());
			res.put("HB1", entries.get(0).getKey());
			res.put("HB1P", entries.get(0).getValue());
			res.put("HB2", entries.get(1).getKey());
			res.put("HB2P", entries.get(1).getValue());
			res.put("HB3", entries.get(2).getKey());
			res.put("HB3P", entries.get(2).getValue());
			res.put("HB4", entries.get(3).getKey());
			res.put("HB4P", entries.get(3).getValue());
		}
		
		res.put("textualValue", this.getValue());
		
		return res;
	}
	
}
