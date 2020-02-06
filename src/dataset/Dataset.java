package dataset;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import featuresCalculation.Featurable;

public class Dataset extends Featurable {
	
	//Constructors----------------------------------------------------
	
	public Dataset() {
		super();
		
		this.slots = new ArrayList<Slot>();
	}
	
	//Properties------------------------------------------------------
	
	private List<Slot> slots;
	private String source;

	public List<Slot> getSlots() {
		List<Slot> result;
		
		result = Collections.unmodifiableList(slots);
		
		return result;
	}
	
	public void addSlot(Slot slot) {
		assert slot != null;
		
		slots.add(slot);
		slot.setDataset(this);
		slot.setRecord(null);
		slot.setSource(this.source);
	}
	
	public void removeSlot(Slot slot) {
		assert slot != null;
		assert contains(slot);
		
		slots.remove(slot);
		slot.setDataset(null);
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	//Interface methods------------------------------------------------
	
	public boolean contains(Slot slot) {
		assert slot != null;
		
		boolean result;
		
		result = slots.contains(slot);
		
		return result;
	}
	
	public JSONObject toJSONObject(){
		JSONObject res;
		JSONArray children;
		
		res = new JSONObject();
		
		children = new JSONArray();
		for (Slot child : this.getSlots()) {
			children.add(child.getJSONObject());
		}
		res.put("children", children);
		
		return res;
	}

	public List<Slot> getAllSlots(){
		List<Slot> result = new ArrayList<>();
		for (Slot slot:this.getSlots()) {
			result.add(slot);
			if(slot instanceof Record){
				result.addAll(((Record)slot).getAllSlots());
			}
		}
		return result;
	}

	public void removeAllSlots(){
		this.slots = new ArrayList<>();
	}
	
}
