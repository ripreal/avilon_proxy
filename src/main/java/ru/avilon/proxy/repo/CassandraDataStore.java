package ru.avilon.proxy.repo;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Host;
import com.datastax.driver.core.HostDistance;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.PoolingOptions;
import com.datastax.driver.core.ProtocolOptions.Compression;
import com.datastax.driver.core.QueryOptions;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy;
import com.datastax.driver.core.policies.DowngradingConsistencyRetryPolicy;
import com.datastax.driver.core.policies.LatencyAwarePolicy;
import com.datastax.driver.core.policies.LoggingRetryPolicy;
import com.datastax.driver.core.policies.TokenAwarePolicy;
import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.core.querybuilder.Select.Where;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import com.datastax.driver.mapping.Result;

import ru.avilon.proxy.entities.ConversionScript;
import ru.avilon.proxy.entities.FilterScript;
import ru.avilon.proxy.entities.Package;
import ru.avilon.proxy.entities.ProxyObject;
import ru.avilon.proxy.entities.User;
import ru.avilon.proxy.utils.TimeUUIDUtils;

@Singleton
public class CassandraDataStore {
	
	static Logger logger = LoggerFactory.getLogger(CassandraDataStore.class);
	
	public final static String KEYSPACE = "proxy";
	public final static String TABLE_OBJECTS = "json_objects";
	public final static String TABLE_CHANGED = "json_objects_changed";
	public final static String TABLE_PACKAGES = "packages";
	
	public final static String TABLE_OAUTH_OBJECTS = "oauth";
	
	public final static String TABLE_CONVERSION_SCRIPTS = "conversion_scripts";
	public final static String TABLE_FILTER_SCRIPTS = "filter_scipts";
	
	public final static String TABLE_USERS = "users";
	
	public static enum ConversionDirection {
		FROM("from"),
		TO("to");
		private String val;
		ConversionDirection(String val) {
	        this.val = val;
	    }
		@Override
		public String toString() {
	        return val;
	    }
	}

	private Cluster cluster;
	private Session session;
	Mapper<ProxyObject> objMapper;
	Mapper<ConversionScript> scriptMapper;
	Mapper<User> userMapper;
	Mapper<Package> packageMapper;
	Mapper<FilterScript> filterScriptMapper;
//	
	public final static String[] conntactPoints = System.getProperty(CassandraDataStore.class.getName().concat(".contanctPoints"), "localhost").split("\\|");
//	public final static String[] conntactPoints = System.getProperty(CassandraDataStore.class.getName().concat(".contanctPoints"), "192.168.254.1").split("\\|");
	
	
public CassandraDataStore() {
		
	final PoolingOptions poolingOptions = new PoolingOptions();
	poolingOptions.setConnectionsPerHost(HostDistance.LOCAL,  4, 16);
	poolingOptions.setConnectionsPerHost(HostDistance.REMOTE, 2, 8);
	poolingOptions.setMaxRequestsPerConnection(HostDistance.LOCAL, 32768);
	poolingOptions.setMaxRequestsPerConnection(HostDistance.REMOTE, 8192);
	poolingOptions.setHeartbeatIntervalSeconds(900);
	poolingOptions.setIdleTimeoutSeconds(1200);
	QueryOptions qo=new QueryOptions();
	qo.setConsistencyLevel(ConsistencyLevel.QUORUM);
	Cluster.Builder builder = Cluster.builder()
				.withPoolingOptions(poolingOptions)
				.withLoadBalancingPolicy(LatencyAwarePolicy
						  .builder(
								  new TokenAwarePolicy(DCAwareRoundRobinPolicy.builder().build()))
						  .build()
				 )
				.withRetryPolicy(new LoggingRetryPolicy(DowngradingConsistencyRetryPolicy.INSTANCE))
				.withQueryOptions(qo)
				.withCompression(Compression.LZ4);
				;
//		for (String address : servers) 
//			builder=builder.addContactPoint(address);
//		final Cluster cluster=builder.build();

	cluster = builder.addContactPoints(conntactPoints).build();
	Metadata metadata = cluster.getMetadata();
	logger.info("Connected to cluster: {}", metadata.getClusterName());
	for (Host host : metadata.getAllHosts()) {
		logger.info("Datacenter: {} Host: {} Rack: {}", host.getDatacenter(), host.getAddress(),
				host.getRack());
		}
	session = cluster.connect();
	session.execute(
	"CREATE KEYSPACE IF NOT EXISTS proxy " +
		"WITH replication = { " +
			"'class' : 'SimpleStrategy'," +
			"'replication_factor' : 1 " +
		"}"
	);

	session = cluster.connect(KEYSPACE);
	MappingManager mappingManager = new MappingManager(session);
	objMapper = mappingManager.mapper(ProxyObject.class);
	scriptMapper = mappingManager.mapper(ConversionScript.class);
	userMapper = mappingManager.mapper(User.class);
	packageMapper = mappingManager.mapper(Package.class);
	filterScriptMapper = mappingManager.mapper(FilterScript.class);
	}
	
	public Cluster getCluster() {
		return cluster;
	}
	
	public Session getSession() {
		return session;
	}
	
	public Session getNewSession() {
		return cluster.connect(KEYSPACE);
	}
	

	public String getConversionScript(String sourceDest, ConversionDirection direction) {
		Statement statement = QueryBuilder.select().from(TABLE_CONVERSION_SCRIPTS).where()
				.and(QueryBuilder.eq("client_app", sourceDest))
				.and(QueryBuilder.eq("from_to", direction.toString())).limit(1);
		
		List<Row> rows = session.execute(statement).all();
		for(Row row : rows) {
			return row.getString("script");
		}
		
		return null;
	}

	public void saveOAuthObject(UUID uuid, String objectType, String data, int ttl) {
		Statement statement = QueryBuilder.insertInto(TABLE_OAUTH_OBJECTS)
				.value("uuid", uuid)
				.value("objecttype", objectType)
				.value("data",data);
		if(ttl > 0) {
			statement = ((Insert)statement).using(QueryBuilder.ttl(ttl));
		}
		session.execute(statement);
	}
	
	public String getOAuthObjectData(UUID uuid) {
		Statement statement = QueryBuilder.select().all().from(TABLE_OAUTH_OBJECTS).where().and(QueryBuilder.eq("uuid", uuid)).limit(1);
		ResultSet rs = session.execute(statement);
		for(Row row : rs.all()) {
			return row.getString("data");
		}
		return null;
	}

	public void removeOAuthObect(String strUuid) {
		removeOAuthObect(UUID.fromString(strUuid));
	}
	
	public void removeOAuthObect(UUID uuid) {
		Statement statement = QueryBuilder.delete().from(TABLE_OAUTH_OBJECTS).where().and(QueryBuilder.eq("uuid", uuid));
		session.execute(statement);
	}
	
	public Set<String> getClientApps() {
		Statement statement =  QueryBuilder.select().distinct().column("clientapp").from(TABLE_CHANGED);
		ResultSet rs = session.execute(statement);
		Set<String> result = new HashSet<>();
		for(Row row : rs.all()) {
			result.add(row.getString("clientapp"));
		}
		return result;
	}
	
	public Set<String> getObjectTypes(Set<String> clientApp) {
		Statement statement =  QueryBuilder.select().column("objecttype").from(TABLE_CHANGED).where(QueryBuilder.in("clientapp", clientApp.toArray()));
		ResultSet rs = session.execute(statement);
		Set<String> result = new HashSet<>();
		for(Row row : rs.all()) {
			result.add(row.getString("objecttype"));
		}
		return result;
	}
	
	public Set<String> getObjectTypes(String clientApp) {
		return getObjectTypes(Collections.singleton(clientApp));
	}

	public ProxyObject getProxyObject(UUID uuid, String clientapp, String type) {
		Statement statement = QueryBuilder.select().all().from(TABLE_OBJECTS)
				.where().and(QueryBuilder.eq("uuid", uuid)).and(QueryBuilder.eq("clientapp", clientapp)).and(QueryBuilder.eq("objecttype", type)).limit(1);
		Result<ProxyObject> result = objMapper.map(session.execute(statement));
		
		for(ProxyObject obj : result) {
			return obj;
		}
		return null;
	}
	
	public List<ProxyObject> getProxyObjects(Integer size) {
		Statement select = QueryBuilder.select().all()
				.from(TABLE_CHANGED)
				;
		if(size != null) {
			select = ((Where)select).limit(size);
		}
		Result<ProxyObject> result = objMapper.map(session.execute(select));
		return result.all();
	}
	
	public List<ProxyObject> getProxyObjectsByCriteria(String clientApp, String objectType, String timeUUID, Boolean changedByProxy, Integer size) {
		return getProxyObjectsByCriteria(clientApp, objectType != null ? Collections.singleton(objectType) : null, timeUUID != null ? UUID.fromString(timeUUID) : null, changedByProxy, size);
	}
	
	public List<ProxyObject> getProxyObjectsByCriteria(String clientApp, Set<String> objectTypes, UUID timeUUID, Boolean changedByProxy, Integer size) {
		return getProxyObjectsByCriteria(Collections.singleton(clientApp), objectTypes, timeUUID, null, changedByProxy, size);
	}
	
	public List<ProxyObject> getProxyObjectsByCriteria(Set<String> clientApps, Set<String> objectTypes, UUID timeUUIDFrom, UUID timeUUIDTo, Boolean changedByProxy, Integer size) {
		
		if(timeUUIDFrom == null)
			timeUUIDFrom = TimeUUIDUtils.ZERO_TIME_UUID;
		
		if(timeUUIDTo == null)
			timeUUIDTo = TimeUUIDUtils.MAX_TIME_UUID;
		
		if(objectTypes == null)
			objectTypes = getObjectTypes(clientApps);
		
		Set<Boolean> changedByProxySet = Collections.singleton(changedByProxy);
		if(changedByProxy == null) {
			changedByProxySet = new HashSet<>();
			changedByProxySet.add(true);
			changedByProxySet.add(false);
		}
			
		
		Statement select = QueryBuilder.select().all()
				.from(TABLE_CHANGED)
				.where()
				.and(QueryBuilder.in("clientapp", clientApps.toArray()))
				.and(QueryBuilder.in("objecttype", objectTypes.toArray()))
				.and(QueryBuilder.in("changedbyproxy", changedByProxySet.toArray()))
				.and(QueryBuilder.gte("timeuuid", timeUUIDFrom))
				.and(QueryBuilder.lte("timeuuid", timeUUIDTo))
				;
		if(size != null) {
			select = ((Where)select).limit(size);
		}
		Result<ProxyObject> result = objMapper.map(session.execute(select));
		return result.all();
	}
	
	public void saveObject(ProxyObject proxyObject) {
		if(proxyObject.getUuid() == null) {
			proxyObject.setUuid(UUID.randomUUID());
		} else {
			deleteObject(proxyObject);
		}
		
		if(proxyObject.getTimeUUID() == null)
			proxyObject.setTimeUUID(TimeUUIDUtils.getCurrentTimeUUID());
		
		objMapper.save(proxyObject);
	}
	
	public void deleteObject(ProxyObject proxyObject) {
		deleteObject(proxyObject.getUuid(), proxyObject.getClientapp(), proxyObject.getObjecttype());
	}
	
	public void deleteObject(UUID uuid, String clientapp, String type) {
		Statement statement = QueryBuilder.delete().all().from(TABLE_OBJECTS)
				.where(QueryBuilder.eq("uuid", uuid));
		if(clientapp != null)
			statement = ((Delete.Where)statement).and(QueryBuilder.eq("clientapp", clientapp));
		if(type != null)
			statement = ((Delete.Where)statement).and(QueryBuilder.eq("objecttype", type));
		session.execute(statement);	
	}
	
	public List<ConversionScript> getConversionScriptsByCriteria(String client_app, String direction) {

		Statement statement = QueryBuilder.select().from(TABLE_CONVERSION_SCRIPTS);
		if(client_app != null && direction != null) {
			statement = ((Select)statement).where();
			if(client_app != null) {
				statement = ((Where)statement).and(QueryBuilder.eq("client_app", client_app));
			}
			if(direction != null) {
				statement = ((Where)statement).and(QueryBuilder.eq("from_to", direction.toString()));
			}
		}
		Result<ConversionScript> result = scriptMapper.map(session.execute(statement));
		return result.all();
	}

	public void saveScript(ConversionScript conversionScript) {
		scriptMapper.save(conversionScript);
	}

	public void deleteScript(String client_app, String from_to) {
		scriptMapper.delete(client_app, from_to);
	}

	public void saveUser(User user) {
		userMapper.save(user);
	}

	public User getUser(String name) {
		return userMapper.get(name);
	}
	
	public User getUserByUUID(UUID json_object_uuid) {
		Statement statement = QueryBuilder.select().from(TABLE_USERS).where(QueryBuilder.eq("json_object_uuid", json_object_uuid)).limit(1);
		List<User> users = userMapper.map(session.execute(statement)).all();
		if(users.size() > 0)
			return users.get(0);
		return null;
	}
	
	public List<User> getUsers() {
		Statement statement = QueryBuilder.select().from(TABLE_USERS);
		return userMapper.map(session.execute(statement)).all();
	}

	public void deleteUser(String name) {
		userMapper.delete(name);
	}
	
	
	public Package getLatestPackage(String appname) {
		Statement statement = QueryBuilder.select().from(TABLE_PACKAGES).where(QueryBuilder.eq("clientapp", appname)).setFetchSize(1);
		
		List<Package> result = packageMapper.map(session.execute(statement)).all();
		if(result.isEmpty())
			return null;
		return result.get(0);
	}

	public void savePackage(Package pack) {
		packageMapper.save(pack);
	}

	public FilterScript getFilterScript(String clientApp) {
		return filterScriptMapper.get(clientApp);
	}
	
	public List<FilterScript> getFilterScriptsByCriteria(String client_app) {

		Statement statement = QueryBuilder.select().from(TABLE_FILTER_SCRIPTS);
		if(client_app != null) {
			statement = ((Select)statement).where();
			if(client_app != null) {
				statement = ((Where)statement).and(QueryBuilder.eq("client_app", client_app));
			}
		}
		Result<FilterScript> result = filterScriptMapper.map(session.execute(statement));
		return result.all();
	}

	public void saveFilterScript(FilterScript filterScript) {
		filterScriptMapper.save(filterScript);
	}

	public void deleteFilterScript(String client_app) {
		filterScriptMapper.delete(client_app);
	}

}
