package ru.avilon.proxy.repo;

import java.net.URL;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.fasterxml.jackson.core.type.TypeReference;

import ru.avilon.proxy.entities.properties.AdvancedProperty;
import ru.avilon.proxy.entities.properties.Property;

public class MetadataBuilderJSON implements MetadataBuilder {
	
	/* (non-Javadoc)
	 * @see ru.avilon.proxy.repo.MetadataBuilder#constructTypes(java.util.Map)
	 */
	@Override
	public void constructTypes(Map<String, Map<String, Property>> objectsMetadata) {

		try {
			URL url = MetadataRepository.class.getResource("/metadata");
			Path dir = Paths.get(url.toURI());
			try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.json")) {
				for (Path entry : stream) {
					String content = new String(Files.readAllBytes(entry));
					JSONObject jsonMetadata = new JSONObject(content);
					// System.err.println(metadata.toString(4));
					for (Object obj : jsonMetadata.keySet()) {
						constructObjectMetadata(obj.toString(), jsonMetadata, objectsMetadata);
					}
				}
			} catch (DirectoryIteratorException ex) {
				// I/O error encounted during the iteration, the cause is an
				// IOException
				throw ex;
			}
		}
		catch(Exception e) {
			throw new RuntimeException(e);
		}

	}
	

	private void constructObjectMetadata(String objectType, JSONObject jsonMetadata,
			Map<String, Map<String, Property>> objectsMetadata) {
		
		Map<String, Property> properties = getProperties(jsonMetadata.getJSONObject(objectType));

		objectsMetadata.put(objectType, properties);
		
	}

	private Map<String, Property> getProperties(JSONObject fields) {
		Map<String, Property> properties = new HashMap<>();
	
		for(Object fieldName : fields.keySet()) {
			JSONObject field =  fields.getJSONObject(fieldName.toString());
			Property prop = initiateProperty(field);
			properties.put(fieldName.toString(), prop);
		}
		return properties;
	}
	
	private Property initiateProperty(JSONObject field) {
		//Property prop = new Property(getPropertyClass(field.get("type")));
		Property prop;
		if("list".equals(field.get("type"))) {
			prop = new Property(types.get("list.".concat(field.get("list_type").toString())));
		} else {
			prop = new Property(types.get(field.get("type").toString()));
		}
		prop.required = Boolean.TRUE.equals(field.has("required") ? field.getBoolean("required") : false);
		if(field.has("object")) {
			JSONObject fields = field.getJSONObject("object");
			prop.nestedProperties = getProperties(fields);
		}
		return prop;
		
	}
	
	static Map<String, TypeReference<?>> types = new HashMap<>();
	static {
		types.put("string", new TypeReference<String>() {});
		types.put("long", new TypeReference<Long>() {});
		types.put("double", new TypeReference<Double>() {});
		types.put("integer", new TypeReference<Integer>() {});
		types.put("boolean", new TypeReference<Boolean>() {});
		types.put("object", new TypeReference<Map<String, Property>>() {});
		types.put("advanced_property", new TypeReference<AdvancedProperty>() {});
		
		types.put("list", new TypeReference<List<?>>() {});
		types.put("list.string", new TypeReference<List<String>>() {});
		types.put("list.long", new TypeReference<List<Long>>() {});
		types.put("list.double", new TypeReference<List<Double>>() {});
		types.put("list.integer", new TypeReference<Integer>() {});
		types.put("list.boolean", new TypeReference<Boolean>() {});
		types.put("list.object", new TypeReference<Map<String, Property>>() {});
		types.put("list.advanced_property", new TypeReference<AdvancedProperty>() {});
	}

}
