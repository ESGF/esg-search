package esg.search.publish.plugins;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ucar.ma2.Array;
import ucar.ma2.IndexIterator;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import esg.search.core.Record;
import esg.search.publish.impl.FileLogger;
import esg.search.publish.thredds.ThreddsUtils;
import esg.search.query.api.QueryParameters;
import esg.search.query.impl.solr.SolrXmlPars;
import esg.search.utils.DateUtils;

/**
 * Super-class that parses an HDF file to harvest searchable metadata.
 * Concrete sub-classes must define specific methods for opening the HDF data stream.
 * 
 * @author Luca Cinquini
 *
 */
public abstract class HdfMetadataEnhancer extends BaseMetadataEnhancerImpl {
	
	private final Log LOG = LogFactory.getLog(this.getClass());
	
	// coordinates
	private String latitude = null;
	private String longitude = null;
	private String time = null;
	
	// example: TAI date-time start: "1993-01-01T00:00:00Z"
	private String timeOffset = "1970-01-01T00:00:00Z"; // defaults to Unix Epoch
	
	// keeps track of URL access success/error
	private FileLogger logger = null;
			
	public HdfMetadataEnhancer(String logFilePath) throws Exception {
		 logger = new FileLogger(logFilePath);
	}
	
	/**
	 * Method to open the data stream from the URL and return a NetcdfFile object.
	 * Returns null if the HDF stream could not be opened.
	 * 
	 * @param url
	 * @return
	 */
	protected abstract NetcdfFile open(String url) throws IOException;
	
	/**
	 * Method to close the data stream after processing.
	 * @param ncfile
	 */
	protected abstract void close(NetcdfFile ncfile);
	
	/**
	 * Selects the appropriate URL for access from the record list.
	 * 
	 * @param record
	 * @return
	 */
	protected abstract String selectUrl(Record record);
	
	/** 
	 * Superclass method that selects the URL of a given type from the record list.
	 * 
	 * @param record
	 * @return
	 */
	protected String selectUrl(String serviceType, Record record) {
		
        // loop over record access URLs
		// example: http://aurapar1.ecs.nasa.gov/opendap/hyrax/GOSAT_TANSO_Level2/ACOS_L2S.3.3/2013/001/acos_L2s_130101_04_Production_v150151_L2s30300_r01_PolB_130225024220.h5|application/x-hdf|HTTPServer
		// example: http://aurapar1.ecs.nasa.gov/opendap/hyrax/GOSAT_TANSO_Level2/ACOS_L2S.3.3/2013/001/acos_L2s_130101_04_Production_v150151_L2s30300_r01_PolB_130225024220.h5.html|application/opendap-html|OPENDAP
        for (String url : record.getFieldValues(QueryParameters.FIELD_URL)) {	
        	
        	String[] parts = url.split("\\|");	
        	if (parts[2].equals(serviceType)) return parts[0];

        }
        
        return null;
		
	}

	@Override
	public void enhance(String name, List<String> values, Record record) {
		
		
		NetcdfFile ncfile = null;
		String url = this.selectUrl(record);
		try {
			ncfile = this.open(url);
			if (ncfile!=null) {
			    if (LOG.isTraceEnabled()) LOG.trace("Processing URL: "+url);
			    process(ncfile, record);
			    logger.afterCrawlingSuccess(url);
			}
		} catch (Exception e) {
			LOG.warn(e.getMessage());
			logger.afterCrawlingError(url);
		} finally {
			this.close(ncfile);
		}
		
	}
	
	private void process(NetcdfFile ncfile, Record record) {
		
		// get variable names, types
		List<String> variables = new ArrayList<String>();
		Group group = ncfile.getRootGroup();
		for (Group g : group.getGroups()) {
			for (Variable v : g.getVariables()) {
				if (LOG.isTraceEnabled()) 
					LOG.trace("Group="+g.getName()+" Variable="+v.getName()+" "+v.isCoordinateVariable()+" "+v.getDataType());
				variables.add(v.getName());
			}
		}
		record.setField(SolrXmlPars.FIELD_VARIABLE, variables);
		
		// latitude boundaries
		if (this.latitude != null) {
			Double[] latBounds = this.parseCoordinate(ncfile, this.latitude);
			if (!latBounds[0].isNaN()) record.setField(SolrXmlPars.FIELD_SOUTH, Double.toString(latBounds[0]));
			if (!latBounds[1].isNaN()) record.setField(SolrXmlPars.FIELD_NORTH, Double.toString(latBounds[1]));
		}
		
		// longitude boundaries
		if (this.longitude != null) {
			Double[] lonBounds = this.parseCoordinate(ncfile, this.longitude);
			if (!lonBounds[0].isNaN()) record.setField(SolrXmlPars.FIELD_WEST, Double.toString(lonBounds[0]));
			if (!lonBounds[1].isNaN()) record.setField(SolrXmlPars.FIELD_EAST, Double.toString(lonBounds[1]));
		}
		
        // complete geospatial coverage
        ThreddsUtils.addGeoCoverage(record);
		
		// time boundaries
		if (this.time != null) {
			Double[] timeBounds = this.parseCoordinate(ncfile, this.time);
			if (!timeBounds[0].isNaN()) {	
				  double secs = timeBounds[0];
				  record.setField(SolrXmlPars.FIELD_DATETIME_START, DateUtils.toSolrDateTimeFormat(timeOffset, secs));
			}
			if (!timeBounds[1].isNaN()) {	
				  double secs = timeBounds[1];
				  record.setField(SolrXmlPars.FIELD_DATETIME_STOP, DateUtils.toSolrDateTimeFormat(timeOffset, secs));
			}
		}
		
		// file data format
		record.setField(SolrXmlPars.FIELD_DATA_FORMAT, "HDF");
		
	}
		
	private Double[] parseCoordinate(NetcdfFile ncfile, String varName) {
				
		Double[] minmax = new Double[]{ Double.NaN, Double.NaN };
		Variable v = ncfile.findVariable(varName);
		if (v!=null) {
			try {
			    Array data = v.read();
			    Double min = Double.MAX_VALUE;
			    Double max = -Double.MAX_VALUE;
			    //int[] shape = data.getShape();
			    //LOG.debug("Variable shape="+shape);
			    IndexIterator ii = data.getIndexIterator();
			    while (ii.hasNext()) {
			      double x = ii.getDoubleNext();
			      if (x<min) min=x;
			      if (x>max) max=x;
			    }
			    minmax[0] = min;
			    minmax[1] = max;
			} catch (IOException ioe) {
				LOG.warn("Error reading variable: " + varName, ioe);
			} 
		}
		return minmax;

	}
	
	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public void setTime(String time) {
		this.time = time;
	}
	
	public void settimeOffset(String timeOffset) {
		this.timeOffset = timeOffset;
	}

}
