package ru.avilon.proxy.entities.properties;

import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class Property {
	
	public boolean required = false;

	private Class<?> clazz;
	
	public Class<?> getValueClass() {
		return clazz;
	}
	
	public Property(Class<?> clazz) {
		this.clazz = clazz;
	}
	
	public Property(TypeReference<?> typeRef) {
		JavaType jt = TypeFactory.defaultInstance().constructType(typeRef);
		this.clazz = jt.getRawClass();
	}
	
	public Map<String, Property> nestedProperties;
}
