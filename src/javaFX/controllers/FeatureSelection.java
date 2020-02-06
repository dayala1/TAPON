package javaFX.controllers;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class FeatureSelection {
	private String name;
	private SimpleBooleanProperty selected;
	private String type;
	private Integer id;

	public FeatureSelection() {
		this.selected = new SimpleBooleanProperty(true);
	}

	public FeatureSelection(String name, String type, Integer id) {
		this.name = name;
		this.type = type;
		this.id = id;
		this.selected = new SimpleBooleanProperty(true);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public BooleanProperty getSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected.set(selected);
		;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Boolean isSelected() {
		return selected.getValue();
	}

	public Integer getId() {
		return id;
	}

}
