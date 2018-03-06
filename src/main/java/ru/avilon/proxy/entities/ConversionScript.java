package ru.avilon.proxy.entities;

import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import ru.avilon.proxy.repo.CassandraDataStore;

@JsonIgnoreProperties(ignoreUnknown = true)
@Table(keyspace = CassandraDataStore.KEYSPACE, name = CassandraDataStore.TABLE_CONVERSION_SCRIPTS)
public class ConversionScript {
    
	@PartitionKey(value = 0)
	private String client_app;
	
	@PartitionKey(value = 1)
    private String from_to;
	
    private String script;
	
	public String getClient_app() {
		return client_app;
	}

	public void setClient_app(String client_app) {
		this.client_app = client_app;
	}

	public String getFrom_to() {
		return from_to;
	}

	public void setFrom_to(String from_to) {
		this.from_to = from_to;
	}

	public String getScript() {
		return script;
	}

	public void setScript(String script) {
		this.script = script;
	}

}
