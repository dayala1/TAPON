package dataset;

import java.util.ArrayList;
import java.util.List;

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
}
