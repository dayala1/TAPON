package dataset;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.clearspring.analytics.util.Lists;

public class Record extends Slot{
	
	//Constructors----------------------------------------------------
	
	public Record() {
		this.slots = new ArrayList<Slot>();
	}
	
	//Properties------------------------------------------------------
	
	private List<Slot> slots;

	public List<Slot> getSlots() {
		return slots;
	}
	
	public void addSlot(Slot slot) {
		assert slot != null;
		
		slots.add(slot);
		slot.setRecord(this);
		slot.setDataset(null);
		slot.setSource(this.getSource());
	}
	
	public void removeSlot(Slot slot) {
		assert slot != null;
		assert contains(slot);
		
		slots.remove(slot);
		slot.setRecord(null);
	}
	
	//Interface methods------------------------------------------------
	
	public boolean contains(Slot slot) {
		assert slot != null;
		
		boolean result;
		
		result = slots.contains(slot);
		
		return result;
	}
	
	public String toString() {
		return String.format("Record %s of class %s", getName(), getSlotClass());
	}

	public List<Slot> getSlotsWithClass(String slotClass) {
		assert slotClass != null;
		
		List<Slot> result;
		
		result = new ArrayList<Slot>();
		for (Slot slot : slots) {
			if(slot.getSlotClass().equals(slotClass)) {
				result.add(slot);
			}
		}
		
		return result;
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
		
		children = new JSONArray();
		for (Slot child : this.getSlots()) {
			children.add(child.getJSONObject());
		}
		res.put("children", children);
		
		return res;
	}

	public List<Slot> getAllSlots(){
		List<Slot> result = new ArrayList<>();
		for (Slot slot :
				this.getSlots()) {
			result.add(slot);
			if(slot instanceof Record){
				result.addAll(((Record)slot).getAllSlots());
			}
		}
		return result;
	}

	public JSONObject getJSONObjectWithoutChildren() {
		JSONObject res;
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

		return res;
	}

	public void setSource(String source) {

		super.setSource(source);
		for (Slot child: this.getSlots()) {
			child.setSource(this.getSource());
		}
	}
}
