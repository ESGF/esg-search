package esg.search.publish.thredds.parsers;

import java.util.HashSet;
import java.util.Set;

import ucar.nc2.units.DateRange;

/**
 * Data structure containing summary information for discoverable THREDDS datasets.
 * 
 * @author Luca Cinquini
 *
 */
public class DatasetSummary {
    
    public long size = 0L;
    
    public int numberOfFiles = 0;
    
    public int numberOfAggregations = 0;
    
    public DateRange dateRange = null;
    
    public double lonEast = Double.MIN_VALUE;
    public double lonWest = Double.MAX_VALUE;
    public double latNorth = Double.MIN_VALUE;
    public double latSouth = Double.MAX_VALUE;
    public String geoUnits = "";
    
    public double heightBottom = Double.MAX_VALUE;
    public double heightTop = Double.MIN_VALUE;
    public String heightUnits = "";
    
    
    // HTTPServer, OPENDAP, GridFTP
    public Set<String> access = new HashSet<String>();


}
