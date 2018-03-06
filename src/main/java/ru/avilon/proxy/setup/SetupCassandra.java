package ru.avilon.proxy.setup;

import java.lang.annotation.Annotation;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Host;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;

import ru.avilon.proxy.entities.ProxyObject;
import ru.avilon.proxy.entities.User;
import ru.avilon.proxy.repo.CassandraDataStore;
import ru.avilon.proxy.repo.MetadataRepository;
import ru.avilon.proxy.rest.configuration.DependencyBinder;

public class SetupCassandra {
	
	static ServiceLocator serviceLocator = ServiceLocatorUtilities.bind(new DependencyBinder());

	static MetadataRepository metadataRepo = serviceLocator.getService(MetadataRepository.class, new Annotation[] {});
	
	static CassandraDataStore cassandraDataStore = serviceLocator.getService(CassandraDataStore.class, new Annotation[] {});
	
	static ObjectMapper mapper = new ObjectMapper();
	static Cluster cluster;
	
	static {
//		cluster = Cluster.builder().addContactPoint("192.168.254.1").build();
		cluster = Cluster.builder().addContactPoints(CassandraDataStore.conntactPoints).build();
		Metadata metadata = cluster.getMetadata();
		System.out.printf("Connected to cluster: %s\n", metadata.getClusterName());
		for (Host host : metadata.getAllHosts()) {
			System.out.printf("Datacenter: %s; Host: %s; Rack: %s\n", host.getDatacenter(), host.getAddress(),
					host.getRack());
		}
	}

	public static void doSetup() throws Exception {
		
		Session session = cluster.connect();
		session.execute("DROP KEYSPACE IF EXISTS ".concat(CassandraDataStore.KEYSPACE));

		URL url = MetadataRepository.class.getResource("/cassandra/setup.cql");
		Path dir = Paths.get(url.toURI());
		String cql = new String(Files.readAllBytes(dir));

		StringTokenizer st = new StringTokenizer(cql, ";");
		while (st.hasMoreTokens()) {
			String stmt = st.nextToken();
			if(StringUtils.isBlank(stmt))
				continue;
			System.err.println(stmt);
			session.execute(stmt);
		}
		
		doSetupSampleData();
		
		doSetupConversionScripts();
		
		doSetupFilterScripts();
		
		doSetupUsers();

	}
	
	public static void doSetupSampleData() throws Exception {
		
		SampleDataConstructor sampleDataConstructor = new SampleDataConstructor(metadataRepo);
		
		Session session = cassandraDataStore.getSession();

		Statement truncateStatement = QueryBuilder.truncate(CassandraDataStore.TABLE_OBJECTS);
		session.execute(truncateStatement);

		List<ProxyObject> objList = sampleDataConstructor.getList();
		
		for(ProxyObject proxyObject :objList) {
			proxyObject.setChangedByProxy(true);
			cassandraDataStore.saveObject(proxyObject);
		}
	}
	
	public static void doSetupConversionScripts() throws Exception {
		Session session = cluster.connect();
		
		Statement truncateStatement = QueryBuilder.truncate(CassandraDataStore.KEYSPACE, CassandraDataStore.TABLE_CONVERSION_SCRIPTS);
		session.execute(truncateStatement);

		
		URL url = SetupCassandra.class.getResource("/js/conversion_rules");
		Path dir = Paths.get(url.toURI());
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
			for (Path entry : stream) {
				if(entry.toFile().isDirectory()) {
					String source = entry.getFileName().toString();
					try (DirectoryStream<Path> sourceStream = Files.newDirectoryStream(entry, "*.js")) {
						for(Path script : sourceStream) {
							System.err.println("processing " + source + "/" + script.getFileName());
							String fileName = StringUtils.removeEnd(script.getFileName().toString(), ".js");
							Statement statement = QueryBuilder.insertInto(CassandraDataStore.KEYSPACE, CassandraDataStore.TABLE_CONVERSION_SCRIPTS)
									.value("client_app", source)
									.value("from_to", fileName)
									.value("script", new String(Files.readAllBytes(script)));
							session.execute(statement);
						}
					}
				}
			}
		}
	}
	
	public static void doSetupFilterScripts() throws Exception {
		Session session = cluster.connect();
		
		Statement truncateStatement = QueryBuilder.truncate(CassandraDataStore.KEYSPACE, CassandraDataStore.TABLE_FILTER_SCRIPTS);
		session.execute(truncateStatement);

		
		URL url = SetupCassandra.class.getResource("/js/filters");
		Path dir = Paths.get(url.toURI());
		try (DirectoryStream<Path> sourceStream = Files.newDirectoryStream(dir, "*.js")) {
			for(Path script : sourceStream) {
				System.err.println("processing " + script.getFileName());
				String fileName = StringUtils.removeEnd(script.getFileName().toString(), ".js");
				Statement statement = QueryBuilder.insertInto(CassandraDataStore.KEYSPACE, CassandraDataStore.TABLE_FILTER_SCRIPTS)
						.value("client_app", fileName)
						.value("script", new String(Files.readAllBytes(script)));
				session.execute(statement);
			}
		}
	}


	
	public static void doSetupUsers() {
		Session session = cluster.connect();
		Statement truncateStatement = QueryBuilder.truncate(CassandraDataStore.KEYSPACE, CassandraDataStore.TABLE_USERS);
		session.execute(truncateStatement);
		
		User user = new User();
		user.setName("user1c");
		user.setPassword("user1c_password");
		user.getRoles().add("user");
		cassandraDataStore.saveUser(user);
		
		
		user = new User();
		user.setName("admin");
		user.setPassword("admin");
		user.getRoles().add("admin");
		cassandraDataStore.saveUser(user);
		
		ProxyObject o =  cassandraDataStore.getProxyObjectsByCriteria("mobile_app", "user", null, null, 1).get(0);
		
		user = new User();
		user.setName("11111");
		user.getRoles().add("user");
//		user.setPassword("test");
		user.setJson_object_uuid(o.getUuid());
		cassandraDataStore.saveUser(user);
	}

	public static void doClear() {
		try {
			Session session = cluster.connect();
			session.execute("DROP KEYSPACE IF EXISTS ".concat(CassandraDataStore.KEYSPACE));
			session.close();
		} catch(Exception e) {
			// do nothing consiosly
		}
	}
}
