package ru.avilon.proxy.entities;

import java.util.UUID;

import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

import ru.avilon.proxy.repo.CassandraDataStore;
import ru.avilon.proxy.utils.TimeUUIDUtils;

@Table(keyspace = CassandraDataStore.KEYSPACE, name = CassandraDataStore.TABLE_PACKAGES)
public class Package {
	
	@PartitionKey(value = 0)
	private String clientapp;

	@PartitionKey(value = 1)
	private Integer packageNumber = 0;
	
	private UUID timeUUID;
	
	public String getClientapp() {
		return clientapp;
	}

	public void setClientapp(String clientapp) {
		this.clientapp = clientapp;
	}

	public UUID getTimeUUID() {
		return timeUUID;
	}

	public void setTimeUUID(UUID timeUUID) {
		this.timeUUID = timeUUID;
	}

	
	public Integer getPackageNumber() {
		return packageNumber;
	}

	public void setPackageNumber(Integer packageNumber) {
		this.packageNumber = packageNumber;
	}

	public long getTimestamp() {
		return TimeUUIDUtils.getTimeFromUUID(timeUUID);
	}
	
	public void incrementPackage() {
		packageNumber++;
		timeUUID = TimeUUIDUtils.getCurrentTimeUUID();
	}
    
}
