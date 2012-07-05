package esg.search.query.ws.rest;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.support.ServletContextResource;

/**
 * Creates a Wget script for downloading the given files and handling
 * certificate renewal. The main conpet is to have some kind of template where
 * the information will get filled. In order to do so, some specific structures
 * must be filled.
 * 
 * @author egonzalez
 */
public class WgetScriptGenerator {
	private static final Log LOG = LogFactory.getLog(WgetScriptGenerator.class);

	/**
	 * Description required for generating the script
	 * @author egonzalez
	 *
	 */
	static public class WgetDescriptor {
		/**
		 * File representation
		 * @author egonzalez
		 *
		 */
		private class File {
			String url;
			String dir;		//future use, to allow the user to define a directory structure
			String size;	//unused
			String chksumType;
			String chksum;
	        public String toString() {
	            return String.format("Url:%s, Dir:%s, size:%s, chksum:%s (%s)\n", 
	                                 url, dir, size, chksum, chksumType);
	        }
		}

		String userOpenId;	//
		String hostName;
		String searchUrl;
		String message;
		List<File> files = new LinkedList<File>();
		

		public WgetDescriptor(String hostName, String userOpenId,
				String searchUrl) {
			this.hostName = hostName;
			this.userOpenId = userOpenId;
			this.searchUrl = searchUrl;
		}

		public void addFile(String url, String dir_structure, String size,
				String chksumType, String chksum) {
			File fd = new File();
			fd.url = url;
			fd.dir = dir_structure;
			//assure it ends in a slash if defined
			if (fd.dir != null && fd.dir.length() > 0 && fd.dir.charAt(fd.dir.length()-1) != '/') 
			    fd.dir = dir_structure + '/';
			fd.size = size;
			fd.chksum = chksum;
			fd.chksumType = chksumType;
			files.add(fd);
		}
		
		public void addMessage(String message) {
		    this.message = message;
		}
		
		public String toString() {
		    StringBuilder sb = new StringBuilder();
		    sb.append(String.format("OpenID:%s\nhostanme:%s\nsearchUrl:%s\nmessage:%s\n",
		                            userOpenId,hostName,searchUrl,message));
		    sb.append("Files:\n");
		    for (File f : files) {
		        sb.append('\t').append(f);
            }
		    
		    return sb.toString();
		}
	}

	/**
	 * This is almost a dummy method, although it works as desired. The
	 * replacement should be in O(N), this method uses O(N*M) where M >=
	 * #replacing tags
	 * 
	 * @param temp
	 *            the template to use
	 * @param tags
	 * 		a map<tag, value> that will be used for replacing all "{{tag}}" with "value"
	 * @return the resulting script as a string
	 */
	static private String replace(String temp, Map<String, String> tags) {
		// incredibly slow but working O(tags.size()*temp.length())
		// potential speed up O(temp.length())
		for (Entry<String, String> e : tags.entrySet()) {
			temp = temp.replaceAll("\\{\\{" + e.getKey() + "\\}\\}",
					e.getValue());
		}
		return temp;
	}

	/**
	 * @param desc descriptor to fill into the script
	 * @return the string containing the whole script
	 */
	static public String getWgetScript(WgetDescriptor desc) {
		String template = getTemplate(null);

		Map<String, String> tags = new HashMap<String, String>();
		// extract using reflections all string from the description
		try {
			for (Field f : desc.getClass().getDeclaredFields()) {
				Object val = f.get(desc);
				//null is "" for bash
				if (val instanceof String)
					tags.put(f.getName(), (String) val);
				else if (val==null)
					tags.put(f.getName(), "");
			}
		} catch (Exception e) {
			// not expected unless bug in source code
			e.printStackTrace();
		}

		// add files
		StringBuilder sb = new StringBuilder();
		final String sep = "' '";
		for (WgetDescriptor.File fd : desc.files) {

			sb.append('\'');
			if (fd.dir != null) sb.append(fd.dir);
			
            //get the name                                			
            sb.append(fd.url.substring(fd.url.lastIndexOf('/') + 1));
		    
			sb.append(sep).append(fd.url);
			sb.append(sep).append(fd.chksumType);
			sb.append(sep).append(fd.chksum).append("'\n");
		}
		// correct last line break
		sb.setLength(sb.length() - 1);

		// add missing general fields
		tags.put("files", sb.toString());
		tags.put("date", DATE_FORMAT.format(new Date()));

		template = replace(template, tags);
		return template;
	}

	//point to the resource holding the template (where?)
	static private final String TEMPLATE_LOC = "WEB-INF/wget-template";
	static private String TEMPLATE;
	
	static private final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
			"yyyy/MM/dd HH:mm:ss");

	static private String getTemplate(ServletContext servletContext) {
		if (TEMPLATE == null && servletContext != null) {
			
			try {
				ServletContextResource resource = new ServletContextResource(servletContext, TEMPLATE_LOC);
				
				LOG.debug(resource.getFile().getAbsolutePath());
				InputStreamReader reader = new InputStreamReader(resource.getInputStream(), "UTF-8");
				StringBuilder sb = new StringBuilder();
				char[] buff = new char[1024];
				int read;
				while ((read = reader.read(buff)) == buff.length) {
					sb.append(buff);
				}
				sb.append(buff, 0, read);

				TEMPLATE = sb.toString();

			} catch (IOException e) {
				System.out.println("Can't get template at " + TEMPLATE_LOC);
				e.printStackTrace();
			}
		}
		return TEMPLATE;
	}
	
	static public void init(ServletContext servletContext) {
		
		getTemplate(servletContext);
		LOG.debug(getTemplate(null));
	}

}
