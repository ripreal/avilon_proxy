package ru.avilon.proxy.rest.configuration.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.ws.rs.NameBinding;

@Retention(RetentionPolicy.RUNTIME)
@NameBinding
public @interface CheckBasicAuthentication {}