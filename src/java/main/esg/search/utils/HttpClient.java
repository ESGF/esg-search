package esg.search.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;

/**
 * Simple class to execute an HTTP GET/POST request,
 * and return the HTTP response as a single string.
 */
public class HttpClient {
	
	/**
	 * Method to execute an HTTP GET request.
	 * @param url
	 * @return
	 * @throws IOException
	 */
	public String doGet(final URL url) throws IOException {
						
		// prepare HTTP request
		final URLConnection connection = url.openConnection();		
		connection.setUseCaches(false);
				
	    // execute HTTP request
	    return getResponse(connection);
		
	}
	
	/**
	 * Method to send an XML document as a POST request.
	 * @param url
	 * @param xml
	 * @return
	 * @throws IOException
	 */
	public String doPostXml(final URL url, final String xml) throws IOException {
		
		// prepare HTTP request
	    final URLConnection connection = url.openConnection();
	    connection.setUseCaches(false);
	    connection.setDoOutput(true); // POST method
	    connection.setRequestProperty("Content-Type", "text/xml");
	    connection.setRequestProperty("Charset", "utf-8");
	    	    
	    final OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
	    wr.write(xml);
	    wr.flush();
	    wr.close();

	    // execute HTTP request
	    return getResponse(connection);
		
	}
	
	/**
	 * Method to execute an HTTP request (GET/POST) and return the HTTP response.
	 * @param connection
	 * @return
	 * @throws IOException
	 */
	private String getResponse(final URLConnection connection) throws IOException {
		
	    final BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	    final StringBuilder sb = new StringBuilder();
	    String line = null;
	    while ((line = rd.readLine()) != null) {
	    	sb.append(line + "\n");
	    }
	    rd.close();
	    return sb.toString();
	    
	}

}
