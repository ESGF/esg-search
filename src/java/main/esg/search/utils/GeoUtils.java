package esg.search.utils;

import java.util.ArrayList;
import java.util.List;

public class GeoUtils {
	
    public static List<float[]> convertLongitudeRangeto180(float latMin, float latMax) {
        
    	List<float[]> latRanges = new ArrayList<float[]>();
    	
    	// return left interval
    	if (latMax <= 180) {
    		float[] latRange = new float[]{ latMin, latMax };
    		latRanges.add(latRange);
    		
    	// return right interval, shifted
    	} else if (latMin >= 180) {
    		float[] latRange = new float[]{ latMin-360, latMax-360 };
    		latRanges.add(latRange);
    		
    	// must split into two intervals
    	} else {
    		
    		float[] latRange1 = new float[]{ -180, latMax-360 };
    		float[] latRange2 = new float[]{ latMin, 180 };
    		latRanges.add(latRange1);
    		latRanges.add(latRange2);
    		
    	}
    	
    	
    	return latRanges;
    	
    }
	
	private GeoUtils() {}

}
