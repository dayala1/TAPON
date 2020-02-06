package javaFX.controllers;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class DomainSelection {
	private String name;
	private SimpleBooleanProperty selected;
	private String folderPath;
	
	public DomainSelection() {
		this.selected = new SimpleBooleanProperty(true);
	}

	public String getFolderPath() {
		return folderPath;
	}

	public void setFolderPath(String folderPath) {
		this.folderPath = folderPath;
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
		this.selected.set(selected);;
	}
	
	public Boolean isSelected() {
		return selected.getValue();
	}
	
}
