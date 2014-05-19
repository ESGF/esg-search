import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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


public class Testme {
	
	private final static Log LOG = LogFactory.getLog(Testme.class);
	private static String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		
		//String url = "http://aurapar1.ecs.nasa.gov/opendap/hyrax/GOSAT_TANSO_Level2/ACOS_L2S.3.3/2013/001/acos_L2s_130101_07_Production_v150151_L2s30300_r01_PolB_130225032330.h5.ddx";
		String url = "/Users/cinquini/tmp/acos_L2s_130101_40_Production_v150151_L2s30300_r01_PolB_130225035247.h5";
		//String url = "/Users/cinquini/tmp/acos_L2s_130101_01_Production_v150151_L2s30300_r01_PolB_130225030150.h5";
		//String url = "http://acdisc.gsfc.nasa.gov/opendap/Aqua_AIRS_Level3/AIRX3STM.005/2004/AIRS.2004.03.01.L3.RetStd031.v5.0.14.0.G07270011247.hdf";
		//NetcdfDataset ncd = NetcdfDataset.openDataset(url);
		
		NetcdfFile ncfile = null;
		  try {
		    ncfile = NetcdfFile.open(url);
		    System.out.println(ncfile);
		    process( ncfile);
		  } catch (IOException ioe) {
		    LOG.warn("Error opening URL: " + url, ioe);
		  } finally { 
		    if (null != ncfile) try {
		      ncfile.close();
		    } catch (IOException ioe) {
		    	LOG.warn("Error closing URL: " + url, ioe);
		    }
		  }

	}
	
	private static void process(NetcdfFile ncfile) throws ParseException {
		
	    // 1993-01-01T00:00:00Z
	    DateFormat df = new SimpleDateFormat(DATE_FORMAT);
	    df.setTimeZone(java.util.TimeZone.getTimeZone("Zulu"));
	    Date date = df.parse("1993-01-01T00:00:00Z");
	    //System.out.println("Starting Date="+date);
	    
	    String input = "1993-01-01T00:00:00Z";
	    DateTimeZone timeZone = DateTimeZone.forID( "Zulu" );
	    DateTime dateTimeStart = new DateTime( input, timeZone );
	    DateTime dateTimeUtcGmt = dateTimeStart.withZone( DateTimeZone.UTC );
	    System.out.println(dateTimeUtcGmt);
		
		// get variable names, types
		Group group = ncfile.getRootGroup();
		for (Group g : group.getGroups()) {
			//System.out.println(g);
			for (Variable v : g.getVariables()) {
				//System.out.println("Group="+g.getName()+" Variable="+v.getName()+" "+v.isCoordinateVariable()+" "+v.getDataType());
			}
		}
		List<Variable> variables = ncfile.getVariables();
		for (Variable v : variables) {
			//LOG.info("VARIABLE="+v.getName()+" - "+v.isCoordinateVariable());
		}
		
		// FIXME: should we use a quality flag ?
		//String varName = "/SoundingGeometry/sounding_latitude"; 
		//String varName = "/SoundingGeometry/sounding_longitude"; 
		String varName = "/RetrievalHeader/sounding_time_tai93"; 
		Variable v = ncfile.findVariable(varName);
		if (null == v) return;
		try {
		    Array data = v.read();
		    int[] shape = data.getShape();
		    LOG.debug("Variable shape="+shape);
		    IndexIterator ii = data.getIndexIterator();
		    while (ii.hasNext()) {
		      double x = ii.getDoubleNext();
		      //System.out.println("lat="+x);
		      
		      //float x = ii.getFloatNext();
		      //System.out.println(x);
		      DateTime y = dateTimeUtcGmt.plusSeconds((int)x);
		      String iso8601String = y.toString();
		      System.out.println("date="+iso8601String);
		      //System.out.println("iso string="+iso8601String);
		      DateTimeZone tz = DateTimeZone.forID( "Zulu" );
		      DateTimeFormatter formatter = DateTimeFormat.forPattern( "yyyy-MM-dd'T'HH:mm:ss'Z'" ).withZone( tz );
		      //System.out.println( "solr format="+formatter.print( y ) );
	
		    }
		} catch (IOException ioe) {
		    LOG.warn("Error reading variable: " + varName, ioe);
		} 
		
	}

}
