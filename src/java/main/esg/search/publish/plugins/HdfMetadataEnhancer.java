package esg.search.publish.plugins;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import ucar.ma2.Array;
import ucar.ma2.IndexIterator;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import esg.search.core.Record;
import esg.search.query.impl.solr.SolrXmlPars;

/**
 * Super-class that parses an HDF file to harvest searchable metadata.
 * Concrete sub-classes must define specific methods for opening the HDF data stream.
 * 
 * @author cinquini
 *
 */
public abstract class HdfMetadataEnhancer extends BaseMetadataEnhancerImpl {
	
	private final Log LOG = LogFactory.getLog(this.getClass());
	
	// coordinates
	private String latitude = null;
	private String longitude = null;
	private String time = null;
	
	// start of TAI date/times
	private final static String TAI_START = "1993-01-01T00:00:00Z";
	private DateTime taiDateTimeUtcGmt;
	DateTimeFormatter formatter;
		
	public HdfMetadataEnhancer() {
		
		// initialize TAI
	    DateTimeZone timeZone = DateTimeZone.forID( "Zulu" );
	    DateTime dateTimeStart = new DateTime( TAI_START, timeZone );
	    taiDateTimeUtcGmt = dateTimeStart.withZone( DateTimeZone.UTC );
	    formatter = DateTimeFormat.forPattern( SolrXmlPars.SOLR_DATE_FORMAT ).withZone( timeZone );

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

	@Override
	public void enhance(String name, List<String> values, Record record) {
		
		// FIXME: use record URL instead
		//String url = "http://aurapar1.ecs.nasa.gov/opendap/hyrax/GOSAT_TANSO_Level2/ACOS_L2S.3.3/2013/001/acos_L2s_130101_07_Production_v150151_L2s30300_r01_PolB_130225032330.h5.ddx";
		//String url = "/Users/cinquini/tmp/acos_L2s_130101_40_Production_v150151_L2s30300_r01_PolB_130225035247.h5";
		String url = "http://aurapar1.ecs.nasa.gov/opendap/GOSAT_TANSO_Level2/ACOS_L2S.3.3/2013/001/acos_L2s_130101_40_Production_v150151_L2s30300_r01_PolB_130225035247.h5";
		//String url = "/Users/cinquini/tmp/acos_L2s_130101_01_Production_v150151_L2s30300_r01_PolB_130225030150.h5";
		//String url = "http://acdisc.gsfc.nasa.gov/opendap/Aqua_AIRS_Level3/AIRX3STM.005/2004/AIRS.2004.03.01.L3.RetStd031.v5.0.14.0.G07270011247.hdf";
		
		NetcdfFile ncfile = null;
		try {
			ncfile = this.open(url);
			if (ncfile!=null) {
			    if (LOG.isTraceEnabled()) LOG.trace("Processing URL: "+url);
			    process(ncfile, record);
			}
		} catch (Exception e) {
			LOG.warn(e.getMessage());
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
		
		// complete geo location
		if (   record.getFieldValue(SolrXmlPars.FIELD_WEST)!=null  && record.getFieldValue(SolrXmlPars.FIELD_EAST)!=null
			&& record.getFieldValue(SolrXmlPars.FIELD_SOUTH)!=null && record.getFieldValue(SolrXmlPars.FIELD_NORTH)!=null) {
			
			record.setField(SolrXmlPars.FIELD_GEO, // "minLon minLat maxLon maxLat"
					    record.getFieldValue(SolrXmlPars.FIELD_WEST)
				   +" "+record.getFieldValue(SolrXmlPars.FIELD_SOUTH)
				   +" "+record.getFieldValue(SolrXmlPars.FIELD_EAST)
				   +" "+record.getFieldValue(SolrXmlPars.FIELD_NORTH) );
				  			
		}
		
		// time boundaries
		if (this.time != null) {
			Double[] timeBounds = this.parseCoordinate(ncfile, this.time);
			if (!timeBounds[0].isNaN()) {	
				  double secs = timeBounds[0];
				  record.setField(SolrXmlPars.FIELD_DATETIME_START, formatTai(secs));
			}
			if (!timeBounds[1].isNaN()) {	
				  double secs = timeBounds[1];
				  record.setField(SolrXmlPars.FIELD_DATETIME_STOP, formatTai(secs));
			}
		}
		
	}
	
	private String formatTai(double seconds) {
	      
		DateTime dt = taiDateTimeUtcGmt.plusSeconds( (int)seconds );
	    String iso8601String = dt.toString();
	    if (LOG.isDebugEnabled()) LOG.debug("TAI datetime="+iso8601String);
	    return this.formatter.print(dt);
	      
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

}
