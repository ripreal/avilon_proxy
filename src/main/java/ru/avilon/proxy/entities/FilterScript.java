package ru.avilon.proxy.entities;

import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import ru.avilon.proxy.repo.CassandraDataStore;

@JsonIgnoreProperties(ignoreUnknown = true)
@Table(keyspace = CassandraDataStore.KEYSPACE, name = CassandraDataStore.TABLE_FILTER_SCRIPTS)
public class FilterScript {
    
	@PartitionKey
	private String client_app;
	
    private String script;
	
	public String getClient_app() {
		return client_app;
	}

	public void setClient_app(String client_app) {
		this.client_app = client_app;
	}

		public String getScript() {
		return script;
	}

	public void setScript(String script) {
		this.script = script;
	}

}
