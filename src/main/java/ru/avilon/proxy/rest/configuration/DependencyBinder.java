package ru.avilon.proxy.rest.configuration;

import javax.inject.Singleton;

import org.apache.oltu.oauth2.as.issuer.OAuthIssuer;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuerImpl;
import org.apache.oltu.oauth2.as.issuer.UUIDValueGenerator;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import com.fasterxml.jackson.databind.ObjectMapper;

import ru.avilon.proxy.authentication.oauth.ClientCredentialsInMemoryRepository;
import ru.avilon.proxy.authentication.oauth.ClientCredentialsRepository;
import ru.avilon.proxy.authentication.oauth.OAuthPersistentRepository;
import ru.avilon.proxy.authentication.oauth.OAuthRepository;
import ru.avilon.proxy.authentication.oauth.ScopeChecker;
import ru.avilon.proxy.conversion.Converter;
import ru.avilon.proxy.repo.CassandraDataStore;
import ru.avilon.proxy.repo.MetadataRepository;

public class DependencyBinder extends AbstractBinder {

	@Override
	protected void configure() {
		bind(OAuthPersistentRepository.class).to(OAuthRepository.class).in(Singleton.class);
		bind(ClientCredentialsInMemoryRepository.class).to(ClientCredentialsRepository.class).in(Singleton.class);
		bind(new OAuthIssuerImpl(new UUIDValueGenerator())).to(OAuthIssuer.class);
		
		bind(MetadataRepository.class).to(MetadataRepository.class).in(Singleton.class);
		bind(ScopeChecker.class).to(ScopeChecker.class).in(Singleton.class);
		bind(Converter.class).to(Converter.class);
		
		bind(CassandraDataStore.class).to(CassandraDataStore.class).in(Singleton.class);
		bind(ObjectMapper.class).to(ObjectMapper.class).in(Singleton.class);
	}

}
