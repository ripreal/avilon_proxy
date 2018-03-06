package ru.avilon.proxy.repo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.avilon.proxy.entities.properties.AdvancedProperty;
import ru.avilon.proxy.entities.properties.Property;

public class MetadataBuilderCode implements MetadataBuilder {
	
	/* (non-Javadoc)
	 * @see ru.avilon.proxy.repo.MetadataBuilder#constructTypes(java.util.Map)
	 */
	@Override
	public void constructTypes(Map<String, Map<String,Property>> objectsMetadata) {

		Map<String, Property> userProperties = new HashMap<>();
		putProperty(userProperties, "name", true, String.class);
		putProperty(userProperties, "department", true, String.class);
		putProperty(userProperties, "position", false, String.class);

		objectsMetadata.put("user", userProperties);
		
		Map<String, Property> taskProperties = new HashMap<>();
		putProperty(taskProperties, "name", true, String.class);
		putProperty(taskProperties, "order", true, Integer.class);
		putProperty(taskProperties, "iconEncoded", false, String.class);
		putProperty(taskProperties, "color", false, String.class);
		putProperty(taskProperties, "parentTaskGroupUUID", false, String.class);
		putProperty(taskProperties, "requireFinish", false, Boolean.class);
		putProperty(taskProperties, "requireOnlineStart", false, Boolean.class);
		putProperty(taskProperties, "description", false, String.class);
		
		List<AdvancedProperty> advancedProperties = new ArrayList<>();
		
		Map<String, Property> startFinishProperties = new HashMap<>();
		putProperty(startFinishProperties, "date", false, Long.class);
		putProperty(startFinishProperties, "userUUID", false, String.class);
		putProperty(startFinishProperties, "properties", false, advancedProperties.getClass());
		
		Property startProperty = new Property(taskProperties.getClass());
		startProperty.nestedProperties = new HashMap<>(startFinishProperties);
		taskProperties.put("start", startProperty);
		
		Property fisnishProperty = new Property(taskProperties.getClass());
		fisnishProperty.nestedProperties = new HashMap<>(startFinishProperties);
		taskProperties.put("fisnish", fisnishProperty);
		
		objectsMetadata.put("task", taskProperties);
		
		
		Map<String, Property> messageProperties = new HashMap<>();
		putProperty(messageProperties, "recipientUUIDS", true, ArrayList.class);
		putProperty(messageProperties, "message", true, String.class);
		objectsMetadata.put("message", messageProperties);
		
		Map<String, Property> geoProperties = new HashMap<>();
		putProperty(geoProperties, "longitude", true, Double.class);
		putProperty(geoProperties, "latitude", true, Double.class);
		putProperty(geoProperties, "userUUID", true, String.class);
		objectsMetadata.put("geo", messageProperties);

		
		Map<String, Property> task_group = new HashMap<>();
		putProperty(task_group, "name", true, String.class);
		putProperty(task_group, "parentTaskGroupUUID", true, String.class);
		objectsMetadata.put("task_group", task_group);
		
		Map<String, Property> logProperties = new HashMap<>();
		putProperty(messageProperties, "message", true, String.class);
		objectsMetadata.put("log", logProperties);
		

	}
	
	private void putProperty(Map<String, Property> objectMetadata,  String name, boolean requred, Class<?> clazz) {

		Property prop = new Property(clazz);
		prop.required = requred;
		objectMetadata.put(name, prop);
		
	}
}
