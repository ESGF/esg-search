package esg.search.publish.plugins;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ucar.nc2.NetcdfFile;
import esg.search.core.Record;
import esg.search.core.RecordImpl;
import esg.search.publish.thredds.ThreddsPars;
import esg.search.query.api.QueryParameters;

/**
 * HDF parser that download the file to a local cache 
 * before parsing its content for metadata.
 * 
 * @author cinquini
 *
 */
public class FileDownloadHdfMetadataEnhancer extends HdfMetadataEnhancer {
	
	// output directory
	String outputDir = "/tmp";
	
	private final Log LOG = LogFactory.getLog(this.getClass());
	
	public FileDownloadHdfMetadataEnhancer(String logFilePath) throws Exception {
		super(logFilePath);
	}
	
	/**
	 * Selects URL of type 'HTTPServer' for downloading the file.
	 */
	@Override
	protected String selectUrl(Record record) {
		return this.selectUrl(ThreddsPars.SERVICE_TYPE_HTTP, record);
	}
	
	/**
	 * Opens local NetcdfFile, if not already existing in cache.
	 */
	@Override
	protected NetcdfFile open(String url) throws IOException {
		
		String filename = url.substring(url.lastIndexOf("/")+1);
		File outputFile = new File(this.outputDir, filename);
		
		// download file unless it exists
		if (!outputFile.exists()) {
			try {
				this.downloadFromUrl(new URL(url), outputFile.getAbsolutePath());
			} catch(MalformedURLException ue) {
				LOG.warn(ue.getMessage());
			} catch(IOException ioe) {
				LOG.warn(ioe.getMessage());
			}
		} else {
			if (LOG.isInfoEnabled()) LOG.info("File="+outputFile.getAbsolutePath()+" already exists, size="+outputFile.length());
		}
		
		// open file
		NetcdfFile ncfile = null;
		if (outputFile.exists()) {
			ncfile = NetcdfFile.open(outputFile.getAbsolutePath());
		}
		
		return ncfile;
		
	}
	
	protected void close(NetcdfFile ncfile) {
		
	    if (null != ncfile) try {
	    	ncfile.close();
	    } catch (IOException ioe) {
	    	LOG.warn("Error closing NetCDF file: " + ncfile, ioe);
	    }
	}
	
	private boolean downloadFromUrl(URL url, String localFilename) throws IOException {
		
		// success return flag
		boolean success = false;
		
		if (LOG.isInfoEnabled()) LOG.info("Downloading URL="+url.toString()+" to local path="+localFilename);
		
	    InputStream is = null;
	    FileOutputStream fos = null;

	    try {
	        URLConnection urlConn = url.openConnection();  // connect

	        is = urlConn.getInputStream();                 // get connection input stream
	        fos = new FileOutputStream(localFilename);     // open output stream to local file

	        byte[] buffer = new byte[4096];                // declare 4KB buffer
	        int len;

	        // while we have available data, continue downloading and storing to local file
	        while ((len = is.read(buffer)) > 0) {  
	            fos.write(buffer, 0, len);
	        }
	        
	        if (LOG.isInfoEnabled()) LOG.info("File="+localFilename+" downloaded, size="+ (new File(localFilename)).length());
	        success = true;
	        
	    } finally {
	        try {
	            if (is != null) {
	                is.close();
	            }
	        } finally {
	            if (fos != null) {
	                fos.close();
	            }
	        }
	    }
	    
	    return success;
	    
	}
	
	public void setOutputDir(String outputDir) {
		this.outputDir = outputDir;
	}
	
	// debug Main program
	public static void main(String[] args) throws Exception {
		
		FileDownloadHdfMetadataEnhancer self = new FileDownloadHdfMetadataEnhancer("/tmp/hdflog.txt");
		self.setLatitude("/SoundingGeometry/sounding_latitude");
		self.setLongitude("/SoundingGeometry/sounding_longitude");
		self.setTime("/RetrievalHeader/sounding_time_tai93");
		self.settimeOffset("1993-01-01T00:00:00Z");
		self.setOutputDir("/usr/local/co2/data/ACOS/3.3");
		
		Record record = new RecordImpl("id");
		List<String> urls = new ArrayList<String>();
		urls.add("http://aurapar1.ecs.nasa.gov/opendap/hyrax/GOSAT_TANSO_Level2/ACOS_L2S.3.3/2013/001/acos_L2s_130101_04_Production_v150151_L2s30300_r01_PolB_130225024220.h5|application/x-hdf|HTTPServer");
		urls.add("http://aurapar1.ecs.nasa.gov/opendap/hyrax/GOSAT_TANSO_Level2/ACOS_L2S.3.3/2013/001/acos_L2s_130101_04_Production_v150151_L2s30300_r01_PolB_130225024220.h5.html|application/opendap-html|OPENDAP");
		record.setField(QueryParameters.FIELD_URL, urls);
		
		self.enhance(null, null, record);

		System.out.println(record.getFields());

	}
	

}
