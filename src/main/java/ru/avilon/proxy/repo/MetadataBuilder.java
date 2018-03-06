package ru.avilon.proxy.repo;

import java.util.Map;

import ru.avilon.proxy.entities.properties.Property;

public interface MetadataBuilder {

	void constructTypes(Map<String, Map<String, Property>> objectsMetadata);

}