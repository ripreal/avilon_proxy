package ru.avilon.proxy.entities.properties;

public class AdvancedProperty {
	
	public String name = "";
	public String description = "";
	public PropertyValue<?> value;
	public boolean required = false;
	public String regexp;
}
