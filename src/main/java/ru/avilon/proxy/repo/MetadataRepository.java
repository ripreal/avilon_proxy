package ru.avilon.proxy.repo;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ru.avilon.proxy.entities.ProxyObject;
import ru.avilon.proxy.entities.properties.Property;
import ru.avilon.proxy.utils.DefaultValues;
import ru.avilon.proxy.utils.TimeUUIDUtils;

public class MetadataRepository {
	
	static Map<String, Map<String,Property>> objectsMetadata =  new HashMap<>();
	
	static Logger logger = LoggerFactory.getLogger(MetadataRepository.class);
	
	@Inject ObjectMapper objectMapper;
	
	public MetadataRepository() {
		MetadataBuilder builder = new MetadataBuilderJSON();
		builder.constructTypes(objectsMetadata);
	}
	
	public ProxyObject constructNewProxyObject(String objectType) {
		
		Map<String, Object> objProperties = getObjectPropertiesMap(objectType);
		
		ProxyObject proxyObject = new ProxyObject();
		proxyObject.setObjecttype(objectType);
		proxyObject.setTimeUUID(TimeUUIDUtils.getCurrentTimeUUID());
		try {
			proxyObject.setData(objectMapper.writeValueAsString(objProperties));
		} catch (JsonProcessingException e) {
			logger.error(e.toString(), e);
		}
		
		return proxyObject;
	}


	public Map<String, Object> getObjectPropertiesMap(String objectType) {
		Map<String, Property> properties = objectsMetadata.get(objectType);
		if(properties == null) {
			throw new RuntimeException("object type not found: " + objectType );
		}
		Map<String, Object> objProperties = new HashMap<>();
		fillObject(objProperties, properties);
		return objProperties;
	}
	
	
	public Set<String> getObjectTypes() {
		return objectsMetadata.keySet();
	}
	
	private void fillObject(Map<String, Object> obj, Map<String, Property> properties) {
		for(Entry<String, Property> property : properties.entrySet()) {
			
			if(property.getValue().getValueClass().isAssignableFrom(HashMap.class)) {
				try {
					Map<String, Object> nestedProperties = new HashMap<>();
					obj.put(property.getKey(), nestedProperties);
					fillObject(nestedProperties, property.getValue().nestedProperties);
				} catch(Exception e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
			} else {
				Object newProp = null;
				if(property.getValue().required)
					newProp = DefaultValues.getDefault(property.getValue().getValueClass());
				obj.put(property.getKey(), newProp);
			}
		}
	}
}
	

