package esg.search.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class GeoUtils {
	
	private static final Log LOG = LogFactory.getLog(GeoUtils.class);
	
    public static List<float[]> convertLongitudeRangeto180(float lonMin, float lonMax) {
        
    	List<float[]> lonRanges = new ArrayList<float[]>();
    	
    	// invalid longitude values
    	if ( (lonMax < lonMin) || (lonMax > lonMin + 360) ) {
    		LOG.warn("Invalid longitude values: lonMin="+lonMin+" lonMax="+lonMax);
    		lonRanges.add( new float[]{ 0, 0 } ); 
    		return lonRanges;
    	}
    	
    	while (lonMin < 180) {
    		lonMin += 360;
    		lonMax += 360;
    	}
    	
    	while (lonMin >= 180) {
    		lonMin -= 360;
    		lonMax -= 360;
    	}
    	
        if (lonMax >= 180) {
        	
        	lonRanges.add( new float[]{ -180, lonMax-360 } );
        	lonRanges.add( new float[]{ lonMin, 180 } );

        } else {
        	
        	lonRanges.add( new float[]{ lonMin, lonMax } );
        }
    		    	
    	return lonRanges;
    	
    }
	
	private GeoUtils() {}
	
	public static void main(String[] args) {
		
		 List<float[]> lonRanges = GeoUtils.convertLongitudeRangeto180( -279.5f,  79.5f);
		 for (float[] lonRange : lonRanges) {
			 System.out.println(lonRange[0]+", "+lonRange[1]);
		 }
		
	}

}
