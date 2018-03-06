package ru.avilon.proxy.data;

import static org.junit.Assert.assertEquals;

import java.lang.annotation.Annotation;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.json.JSONArray;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.fasterxml.jackson.databind.ObjectMapper;

import ru.avilon.proxy.entities.Package;
import ru.avilon.proxy.repo.CassandraDataStore;
import ru.avilon.proxy.repo.MetadataRepository;
import ru.avilon.proxy.rest.configuration.DependencyBinder;
import ru.avilon.proxy.setup.SetupCassandra;
import ru.avilon.proxy.utils.GZIPFiles;

public class Data_IT {
	
	static ServiceLocator serviceLocator = ServiceLocatorUtilities.bind(new DependencyBinder());

	static MetadataRepository metadataRepo = serviceLocator.getService(MetadataRepository.class, new Annotation[] {});
	
	static ObjectMapper mapper = serviceLocator.getService(ObjectMapper.class, new Annotation[] {});
	
	static CassandraDataStore cassandraDataStore = serviceLocator.getService(CassandraDataStore.class, new Annotation[] {});


	@BeforeClass
	public static void setUp() throws Exception {
		if (cassandraDataStore.getNewSession() == null)
			SetupCassandra.doSetup();
	}

	@AfterClass
	public static void destroy() throws Exception {
		//SetupCassandra.doClear();
	}

	@Test //@Ignore
	public void test_cassandra() throws Exception {
		SetupCassandra.doSetupSampleData();
		SetupCassandra.doSetupConversionScripts();
		SetupCassandra.doSetupFilterScripts();
		SetupCassandra.doSetupUsers();
	}
	
	@Test
	public void test1() {
		Session session = cassandraDataStore.getSession();
		
		Select statement = QueryBuilder.select().all().from(CassandraDataStore.KEYSPACE, CassandraDataStore.TABLE_OBJECTS);
		List<Row> rs = session.execute(statement.toString().replace("SELECT ", "SELECT JSON ")).all();
		JSONArray jsonArray = new JSONArray();
		for(Row row : rs) {
			jsonArray.put( new org.json.JSONObject( row.getString("[json]")));
			System.err.println(row.getString("[json]"));
		}
	}
	
	@Test
	public void testPackage() {
		Package pack = cassandraDataStore.getLatestPackage("mobile");
		if(pack == null) 
			pack = new Package();
		
		pack.setClientapp("mobile");
		pack.incrementPackage();
		
		cassandraDataStore.savePackage(pack);
		
		Package pack1 = cassandraDataStore.getLatestPackage("mobile");
		pack1.incrementPackage();
		cassandraDataStore.savePackage(pack1);
		
		Package pack2 = cassandraDataStore.getLatestPackage("mobile");
		
		assertEquals(pack1.getPackageNumber(), pack2.getPackageNumber());
	}
	@Test
	public void readLogFiles() throws Exception {
	
//		ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(ProxyLoggingFilter.class);
//		RollingFileAppender appender = (RollingFileAppender)logger.getAppender("REQ_RESP");
//		appender.getOutputStream();
//		Path path = Paths.get(appender.getFile());
//		System.err.println(path.getParent().toString());
		Path path = Paths.get("logs/tt", new String[] {});
		
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(path.getParent())) {
			for (Path entry : stream) {
				System.err.println(entry.getFileName().toString());
				
				List<String> lines = new ArrayList<>();
				StringBuilder strBuilder = new StringBuilder();
				try (Stream<String> fileStream = entry.getFileName().toString().endsWith(".gz") ? GZIPFiles.lines(entry) : Files.lines(entry, StandardCharsets.ISO_8859_1)) {
					fileStream.forEach(new Consumer<String>() {
						@Override
						public void accept(String t) {
							if(t.matches("^\\d{2}:\\d{2}:\\d{2}\\.\\d{3}.*") && strBuilder.length() > 0) {
								lines.add(strBuilder.toString());
								strBuilder.setLength(0);
							}
							strBuilder.append(t).append(System.lineSeparator());
						}
					});
					if(strBuilder.length() > 0)
						lines.add(strBuilder.toString());
//					fileStream.filter(line -> line.contains(".Cluster")).forEach(System.out::println);
				}
				
				System.out.println(lines);
			}
		}
	}
}
