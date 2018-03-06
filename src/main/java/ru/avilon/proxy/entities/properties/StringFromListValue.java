package ru.avilon.proxy.entities.properties;

import java.util.LinkedHashMap;
import java.util.Map;

public class StringFromListValue extends PropertyValue<String> {
	public Map<String, String> avialableValues = new LinkedHashMap<>();
	
	public StringFromListValue() {
		super(String.class);
	}
}
