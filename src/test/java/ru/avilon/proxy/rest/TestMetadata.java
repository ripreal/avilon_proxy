package ru.avilon.proxy.rest;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import ru.avilon.proxy.entities.properties.Property;
import ru.avilon.proxy.repo.MetadataBuilder;
import ru.avilon.proxy.repo.MetadataBuilderCode;
import ru.avilon.proxy.repo.MetadataBuilderJSON;

public class TestMetadata {

	@Test
	public void test() throws Exception {
		
		Map<String, Map<String,Property>> objectsMetadata =  new HashMap<>();
		
		MetadataBuilder builder = new MetadataBuilderJSON();
		builder.constructTypes(objectsMetadata);
		assertTrue(objectsMetadata.size() > 0);
		
		objectsMetadata =  new HashMap<>();
		builder = new MetadataBuilderCode();
		builder.constructTypes(objectsMetadata);
		assertTrue(objectsMetadata.size() > 0);
		
	}
	

	
}
