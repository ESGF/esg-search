package esg.search.utils;

import org.springframework.core.io.ClassPathResource;


/**
 * Utility class to set the keystore and trustore environment to be used in SSL communication.
 */
public class CertUtils {
	
	
	final static ClassLoader classloader = CertUtils.class.getClassLoader();
	
	/**
	 * Method to set a keystore to the desired file in the classpath.
	 * A keystore is needed by the client to send its own certificate for authentication.
     * Note that the keystore must be trusted by the server.
     * 
	 * @param keystoreClassPathLocation
	 * @throws Exception
	 */
	public static void setKeystore(final String keystoreClassPathLocation) throws Exception {
		
		ClassPathResource keystore = new ClassPathResource(keystoreClassPathLocation, classloader);
		System.setProperty("javax.net.ssl.keyStore", keystore.getFile().getAbsolutePath()); 
		System.setProperty("javax.net.ssl.keyStorePassword","changeit");
		
	}

	/**
	 * Method to set the trustore to the desired file in the classpath.
	 * A trustore is needed for the client to trust the server certificate.
	 * The trustore must match the certificate used by the server
	 * 
	 * @param trustoreClassPathLocation
	 * @throws Exception
	 */
	public static void setTruststore(final String trustoreClassPathLocation) throws Exception {
		
		ClassPathResource trustore = new ClassPathResource(trustoreClassPathLocation, classloader);
		System.setProperty("javax.net.ssl.trustStore", trustore.getFile().getAbsolutePath()); 
		System.setProperty("javax.net.ssl.trustStorePassword","changeit");
		
	}
	

}
