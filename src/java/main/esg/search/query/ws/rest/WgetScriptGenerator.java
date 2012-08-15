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
	 * Description required for generating the script. It must contain all fields
	 * That are requested for in the template. These fields must be simple
	 * Strings and not private nor inherited.
	 * @author egonzalez
	 *
	 */
	static public class WgetDescriptor {
		/**
		 * File representation used for generating the file listing within the
		 * wget script.
		 * @author egonzalez
		 *
		 */
		private class File {
		    String name = "";
			String url = "";
			String dir = "";	//to allow the user to define a directory structure
			String size = "";	//presently unused
			String chksumType = "";
			String chksum = "";
	        public String toString() {
	            return String.format("name:%s, Url:%s, Dir:%s, size:%s, chksum:%s (%s)\n", 
	                                 name, url, dir, size, chksum, chksumType);
	        }
		}

		//All public strings are going to get extracted automatically for 
		//filling out the template.
		String userOpenId;	
		String hostName;
		String searchUrl;
		String files;
		String message;
		
		private StringBuffer messg_sb = new StringBuffer();
		List<File> all_files = new LinkedList<File>();
		Map<String, String> checksums = new HashMap<String, String>();
		
		
        private final String MSG_FILE_COLLISION = "There were files with the "
            + "same name which were requested to be download to the same "
            + "directory. To avoid overwritting the previous downloaded "
            + "one they were skipped.\nPlease use the parameter "
            + "'download_structure' to set up unique directories for them.";
		private boolean collision_msg_sent = false;
		private int no_url_count = 0;
		
		/**
		 * @param hostName hostname where this wget script got generated
		 * @param userOpenId OpenId of the user (if known, it can be null)
		 * @param searchUrl searchUrl that produced this results.
		 */
		public WgetDescriptor(String hostName, String userOpenId,
				String searchUrl) {
			this.hostName = hostName;
			this.userOpenId = userOpenId;
			this.searchUrl = searchUrl;
		}

        /**
         * Adds a file description to the files used in the wget script. There
         * are some checks taking place, so it's not guaranteed that the file is
         * really stored (e.g. if the url is empty it will be skipped, if
         * there's already a file with the same checksum and the same
         * destination it will also be silently skipped; if two files with the
         * same name but different checksums are found, then only the first will
         * be written and a warning will be given to the user about this issue.
         * 
         * @param url
         *            Url where the file is to be found (required or the file
         *            will just be skipped)
         * @param dir_structure
         *            directory structure generated for the file (can be null)
         * @param size
         *            file size
         * @param chksumType
         *            checksum type
         * @param chksum
         *            checksum value
         */
		public void addFile(String url, String dir_structure, String size,
				String chksumType, String chksum) {
			File fd = new File();
			if (url == null || url.length() == 0) {
			    //we can't do anything with this! just skip it...
			    no_url_count++;
			    return;
			}
			fd.url = url;
			fd.name = url.substring(fd.url.lastIndexOf('/') + 1);
			fd.dir = dir_structure;
			//assure it ends in a slash if defined
			if (fd.dir != null && fd.dir.length() > 0 && fd.dir.charAt(fd.dir.length()-1) != '/') 
			    fd.dir = dir_structure + '/';
			fd.size = size;
			
			//Don't store null values
			if (chksum != null) fd.chksum = chksum;
			if (chksumType != null) fd.chksumType = chksumType;
			
			if (this.checksums.containsKey(fd.dir + fd.name)) {
			    //this file would overwrite a file already downloaded.
			    //we won't be adding it.
			    if (!collision_msg_sent && fd.chksum != null &&
			            fd.chksum.equals(this.checksums.get(fd.dir + fd.name))){
			        //ouch! we have a file with a different checksum going
			        //to the same location. We need to inform the user about this.
                    this.addMessage(MSG_FILE_COLLISION);
			        collision_msg_sent = true;
			    }
			} else {
			    //everything is fine proceed as usual
			    all_files.add(fd);
			    this.checksums.put(fd.dir + fd.name, fd.chksum);
			}
		}
		
		/**
		 * Adds a string that will be shown to the user
		 * @param message message to be displayed to the user
		 */
		public void addMessage(String message) {
		    this.messg_sb.append(message).append('\n');
		}
		
		/**
		 * @return the number of files that will be displayed in the wget script.
		 */
		public int getFileCount() {
		    return all_files.size();
		}
		
		/**
		 * @return the number of files that where skipped because they had no url.
		 */
		public int getNoUrlCount() {
		    return no_url_count;
		}
		
		public String toString() {
		    StringBuilder sb = new StringBuilder();
		    sb.append(String.format("OpenID:%s\nhostanme:%s\nsearchUrl:%s\nmessage:%s\n",
		                            userOpenId,hostName,searchUrl,message));
		    sb.append("Files:\n");
		    for (File f : all_files) {
		        sb.append('\t').append(f);
            }
		    
		    return sb.toString();
		}

        /**
         * Assure everything is ready for script generation.
         * After this call all non private Strings with names matched in the
         * template must be contain the required information.
         */
        public void flush() {
            //set the message
            this.message = this.messg_sb.toString();
            
            // add files
            StringBuilder sb = new StringBuilder();
            final String sep = "' '";
            for (WgetDescriptor.File fd : this.all_files) {

                sb.append('\'');
                if (fd.dir != null) sb.append(fd.dir);
                
                //get the name                                          
                sb.append(fd.name);
                
                sb.append(sep).append(fd.url);
                sb.append(sep).append(fd.chksumType);
                sb.append(sep).append(fd.chksum).append("'\n");
            }
            // correct last line break
            if (sb.length() >0) sb.setLength(sb.length() - 1);
            this.files = sb.toString();
            
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
	 * Returns the Wget bash script as a string.
	 * @param desc descriptor to fill into the script
	 * @return the string containing the whole script
	 */
	static public String getWgetScript(WgetDescriptor desc) {
		String template = getTemplate(null);
		desc.flush();
		
		Map<String, String> tags = new HashMap<String, String>();
		// extract using reflections all string from the description
		try {
			for (Field f : desc.getClass().getDeclaredFields()) {
			    //don't even care to check what's not public
			    if(!java.lang.reflect.Modifier.isPrivate(f.getModifiers())) {
    				Object val = f.get(desc);
    				//null is "" for bash
    				if (val instanceof String)
    					tags.put(f.getName(), (String) val);
    				else if (val==null)
    					tags.put(f.getName(), "");
    			    }
			}
		} catch (Exception e) {
			// not expected unless bug in source code
			e.printStackTrace();
		}
		tags.put("date", DATE_FORMAT.format(new Date()));

		template = replace(template, tags);
		return template;
	}

	//point to the resource holding the template (where?)
	static private final String TEMPLATE_LOC = "WEB-INF/wget-template";
	static private String TEMPLATE;
	
	static private final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
			"yyyy/MM/dd HH:mm:ss");

	/**
	 * Finds and caches the template into memory.
	 * @param servletContext
	 * @return the template
	 */
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
			    //would we actually want to let the servlet load?
			    LOG.error("Can't get template at " + TEMPLATE_LOC, e);
				throw new Error("Can't get the wget Template. Aborting servlet load.");
			}
		}
		return TEMPLATE;
	}
	
	/**
	 * Initializes this servlet which checks the template is loadable.
	 * @param servletContext
	 */
	static public void init(ServletContext servletContext) {
		
		getTemplate(servletContext);
		LOG.debug(getTemplate(null));
	}

}
