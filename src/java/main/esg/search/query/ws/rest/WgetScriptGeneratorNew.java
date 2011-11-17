package esg.search.query.ws.rest;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Creates a Wget script for downloading the given files and handling
 * certificate renewal. The main conpet is to have some kind of template where
 * the information will get filled. In order to do so, some specific structures
 * must be filled.
 * 
 * @author egonzalez
 */
public class WgetScriptGeneratorNew {
	private static final Log LOG = LogFactory.getLog(WgetScriptGeneratorNew.class);

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
			String id;		//future use, to allow the user to define a directory structure
			String size;	//unused
			String chksumType;
			String chksum;
		}

		String userOpenId;	//
		String hostName;
		String searchUrl;
		List<File> files = new LinkedList<File>();

		public WgetDescriptor(String hostName, String userOpenId,
				String searchUrl) {
			this.hostName = hostName;
			this.userOpenId = userOpenId;
			this.searchUrl = searchUrl;
		}

		public void addFile(String url, String id, String size,
				String chksumType, String chksum) {
			File fd = new File();
			fd.url = url;
			fd.id = id;
			fd.size = size;
			fd.chksum = chksum;
			fd.chksumType = chksumType;
			files.add(fd);
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
		// incredibly slow but working
		// expected speed up Nx, where N > tags.size()
		for (Entry<String, String> e : tags.entrySet()) {
			temp = temp.replaceAll("\\{\\{" + e.getKey() + "\\}\\}",
					e.getValue());
		}
		return temp;
	}

	/**
	 * @param desc descriptor to fill into the script
	 * @return the string conatining the whole script
	 */
	static public String getWgetScript(WgetDescriptor desc) {
		String template = getTemplate();

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
			sb.append('\'').append(
					fd.url.substring(fd.url.lastIndexOf('/') + 1));
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
	static private final String TEMPLATE_LOC = "file://wget-template";
	static private String TEMPLATE;
	
	static private final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
			"yyyy/MM/dd HH:mm:ss");

	static private String getTemplate() {
		if (TEMPLATE == null) {
			
			try {
				URL url = new URL(TEMPLATE_LOC);
				URLConnection c = url.openConnection();
				InputStreamReader reader = new InputStreamReader(
						c.getInputStream(), "UTF-8");
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
	
	static public void init() {
		//for checking it is found, if not the just fail when starting
		getTemplate();
		LOG.debug(getTemplate());
	}

	/**
	 * @param args
	 *            nothing
	 */
	public static void main(String[] args) {
		System.out.println(WgetScriptGeneratorNew
				.getWgetScript(createTestDescriptor()));
	}

	public static WgetDescriptor createTestDescriptor() {
		WgetDescriptor desc = new WgetDescriptor("TESThostName",
				null, "TESTsearchUrl");
		desc.addFile(
				"http://bcccsm.cma.gov.cn/thredds/fileServer/cmip5_data/output/BCC/bcc-csm1-1/abrupt4xCO2/3hr/land/mrsos/r9i1p1/mrsos_3hr_bcc-csm1-1_abrupt4xCO2_r9i1p1_016009010000-016512312100.nc",
				"TESTid", "777", "TESTchksumType", "TESTchksum");
		desc.addFile(
				"http://bcccsm.cma.gov.cn/thredds/fileServer/cmip5_data/output/BCC/bcc-csm1-1/abrupt4xCO2/3hr/land/tslsi/r9i1p1/tslsi_3hr_bcc-csm1-1_abrupt4xCO2_r9i1p1_016009010000-016512312100.nc",
				"TESTid2", "7772", "TESTchksumType2", "TESTchksum2");
		return desc;
	}

}
