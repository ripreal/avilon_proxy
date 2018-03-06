package ru.avilon.proxy.rest.configuration;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

public class DependencyBinderFeature implements Feature {

	@Override
	public boolean configure(FeatureContext context) {
		context.register(new DependencyBinder());
		return false;
	}

}
