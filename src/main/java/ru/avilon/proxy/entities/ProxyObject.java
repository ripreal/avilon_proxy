package ru.avilon.proxy.entities;

import java.text.MessageFormat;
import java.util.Map;
import java.util.UUID;

import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import ru.avilon.proxy.repo.CassandraDataStore;
import ru.avilon.proxy.utils.TimeUUIDUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
@Table(keyspace = CassandraDataStore.KEYSPACE, name = CassandraDataStore.TABLE_OBJECTS)
public class ProxyObject {
	
	@PartitionKey(value = 0)
	private UUID uuid;
	
	@PartitionKey(value = 1)
	private String clientapp;
	
	@PartitionKey(value = 2)
	private String objecttype;
		
	@PartitionKey(value = 3)
	private UUID timeUUID;
	
	@PartitionKey(value = 4)
	private Boolean changedByProxy = false;
	
	private String data;
	
	public UUID getUuid() {
		return uuid;
	}
	
	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public String getClientapp() {
		return clientapp;
	}

	public void setClientapp(String clientapp) {
		this.clientapp = clientapp;
	}

	public String getObjecttype() {
		return objecttype;
	}

	public void setObjecttype(String objecttype) {
		this.objecttype = objecttype;
	}

	public UUID getTimeUUID() {
		return timeUUID;
	}

	public void setTimeUUID(UUID timeUUID) {
		this.timeUUID = timeUUID;
	}

	public Boolean getChangedByProxy() {
		return changedByProxy;
	}

	public void setChangedByProxy(Boolean changedByProxy) {
		this.changedByProxy = Boolean.TRUE.equals(changedByProxy);
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}
	
	public long getTimestamp() {
		return TimeUUIDUtils.getTimeFromUUID(timeUUID);
	}
	
	
	@Override
	public String toString() {
		return MessageFormat.format("uuid: {0}, objecttype: {1} timeUUID: {2}, data: {3}", uuid, objecttype, timeUUID, data);
	}
	
	static ObjectMapper mapper = new ObjectMapper();
	
	public void setProperty(String property, Object value) throws Exception {
		Map<String, Object> mapData = mapper.readValue(data, new  TypeReference<Map<String, Object>>() {});
		mapData.put(property, value);
		data = mapper.writeValueAsString(mapData);
	}
	
	@JsonIgnore
	public Object getProperty(String property) throws Exception {
		return getProperties().get(property);
	}
	
	@JsonIgnore
	public Map<String, Object> getProperties() throws Exception {
		try {
			return mapper.readValue(data, new  TypeReference<Map<String, Object>>() {});
		} catch(Exception e) {
			return null;
		}
	}
    
}
