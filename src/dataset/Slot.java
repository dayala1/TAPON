package dataset;

import java.util.LinkedHashMap;

import featuresCalculation.Featurable;

public class Slot extends Featurable{
	
	//Properties------------------------------------------------------
	
	private Dataset dataset;
	private Record record;
	private String slotClass;
	private String hint;
	LinkedHashMap<String, Double> hintsRanking;
	private Integer startIndex;
	private Integer endIndex;
	
	public Dataset getDataset() {
		return dataset;
	}
	
	public void setDataset(Dataset dataset) {
		this.dataset = dataset;
	}
	
	public String getSlotClass() {
		return slotClass;
		
	}
	
	public void setSlotClass(String slotClass) {
		
		this.slotClass = slotClass;
	}

	public Record getRecord() {
		return record;
	}

	public void setRecord(Record record) {
		assert record != null;
		
		this.record = record;
	}
	
	public Integer getStartIndex() {
		return startIndex;
	}

	public void setStartIndex(Integer startIndex) {
		assert startIndex != null;
	
		this.startIndex = startIndex;
	}

	public Integer getEndIndex() {
		return endIndex;
	}

	public void setEndIndex(Integer endIndex) {
		assert endIndex != null;
	
		this.endIndex = endIndex;
	}
	
	public String getHint() {
		String res;
		if (hint==null) {
			res = "None";
		} else {
			res = hint;
		}
		return res;
	}

	public void setHint(String hint) {
		this.hint = hint;
	}
	
	public LinkedHashMap<String, Double> getHintsRanking() {
		return hintsRanking;
	}

	public void setHintsRanking(LinkedHashMap<String, Double> hintsRanking) {
		assert hintsRanking != null;
	
		this.hintsRanking = hintsRanking;
	}

	//Interface methods------------------------------------------------

	public Featurable getParent() {
		Featurable result;
		
		if(getDataset() == null) {
			result = getRecord();
		} else {
			result = getDataset();
		}
		
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Slot other = (Slot) obj;
		if (getName() == null) {
			if (other.getName() != null)
				return false;
		} else if (!getName().equals(other.getName()))
			return false;
		if (slotClass == null) {
			if (other.slotClass != null)
				return false;
		} else if (!slotClass.equals(other.slotClass))
			return false;
		return true;
	}
	
}
