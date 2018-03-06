package ru.avilon.proxy.authentication.oauth;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import ru.avilon.proxy.repo.MetadataRepository;

public class ScopeChecker {

	private Set<String> avialableScopes = new HashSet<>();
	
	@Inject
	public ScopeChecker(MetadataRepository metadataRepo) {
		Set<String> objectTypes = metadataRepo.getObjectTypes();
		avialableScopes.addAll(objectTypes);
		objectTypes.forEach(objType -> avialableScopes.add(objType.concat("_write")));
	}
	
	public boolean isScopeValid(String scope) {
		if(StringUtils.isBlank(scope))
			return false;
		
		 StringTokenizer st = new StringTokenizer(scope, " ");
		 while (st.hasMoreTokens()) {
	         String requestedScope = st.nextToken();
	         if(!avialableScopes.contains(requestedScope)) {
	        	 return false;
	         }
	     }
		 return true;
	}
	
	
}
