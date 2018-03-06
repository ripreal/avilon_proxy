package ru.avilon.proxy.entities;

import java.security.Principal;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.apache.commons.codec.binary.Base64;

import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import ru.avilon.proxy.repo.CassandraDataStore;

@JsonIgnoreProperties(ignoreUnknown = true)
@Table(keyspace = CassandraDataStore.KEYSPACE, name = CassandraDataStore.TABLE_USERS)
public class User implements Principal {

	@PartitionKey
	private String name;
	
	private String password;
	
	private Set<String> roles = new HashSet<>();
	
	private UUID json_object_uuid;
	
	public User() {
	}
	
	public User(String name) {
		this.name = name;
	}
	
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Set<String> getRoles() {
		return roles;
	}

	public void setRoles(Set<String> roles) {
		this.roles = roles;
	}
	
	@Override
	public String getName() {
		return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public UUID getJson_object_uuid() {
		return json_object_uuid;
	}
	
	public void setJson_object_uuid(UUID json_object_uuid) {
		this.json_object_uuid = json_object_uuid;
	}
	
	@Override
	public String toString() {
		return name + " roles: " + roles.toString();
	}


	public static class Password {
	    // The higher the number of iterations the more 
	    // expensive computing the hash is for us and
	    // also for an attacker.
	    private static final int iterations = 20*1000;
	    private static final int saltLen = 32;
	    private static final int desiredKeyLen = 256;

	    /** Computes a salted PBKDF2 hash of given plaintext password
	        suitable for storing in a database. 
	        Empty passwords are not supported. */
	    public static String getSaltedHash(String password) throws Exception {
	        byte[] salt = SecureRandom.getInstance("SHA1PRNG").generateSeed(saltLen);
	        // store the salt with the password
	        return Base64.encodeBase64String(salt) + "$" + hash(password, salt);
	    }

	    /** Checks whether given plaintext password corresponds 
	        to a stored salted hash of the password. */
	    public static boolean check(String password, String stored) throws Exception{
	        String[] saltAndPass = stored.split("\\$");
	        if (saltAndPass.length != 2) {
	            throw new IllegalStateException(
	                "The stored password have the form 'salt$hash'");
	        }
	        String hashOfInput = hash(password, Base64.decodeBase64(saltAndPass[0]));
	        return hashOfInput.equals(saltAndPass[1]);
	    }

	    // using PBKDF2 from Sun, an alternative is https://github.com/wg/scrypt
	    // cf. http://www.unlimitednovelty.com/2012/03/dont-use-bcrypt.html
	    private static String hash(String password, byte[] salt) throws Exception {
	        if (password == null || password.length() == 0)
	            throw new IllegalArgumentException("Empty passwords are not supported.");
	        SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
	        SecretKey key = f.generateSecret(new PBEKeySpec(
	            password.toCharArray(), salt, iterations, desiredKeyLen)
	        );
	        return Base64.encodeBase64String(key.getEncoded());
	    }
	}
}
