package esg.search.utils;

import java.util.ArrayList;
import java.util.List;

public class GeoUtils {
	
    public static List<float[]> convertLongitudeRangeto180(float lonMin, float lonMax) {
        
    	List<float[]> lonRanges = new ArrayList<float[]>();
    	
    	// return left interval
    	if (lonMax <= 180) {
    		float[] lonRange = new float[]{ lonMin, lonMax };
    		lonRanges.add(lonRange);
    		
    	// return right interval, shifted
    	} else if (lonMin >= 180) {
    		float[] lonRange = new float[]{ lonMin-360, lonMax-360 };
    		lonRanges.add(lonRange);
    		
    	// must split into two intervals
    	} else {
    		
    		float[] lonRange1 = new float[]{ -180, lonMax-360 };
    		float[] lonRange2 = new float[]{ lonMin, 180 };
    		lonRanges.add(lonRange1);
    		lonRanges.add(lonRange2);
    		
    	}
    	
    	return lonRanges;
    	
    }
	
	private GeoUtils() {}

}
