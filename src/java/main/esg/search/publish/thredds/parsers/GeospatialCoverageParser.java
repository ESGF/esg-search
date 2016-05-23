package esg.search.publish.thredds.parsers;

import java.util.List;

import org.springframework.util.StringUtils;

import esg.search.core.Record;
import esg.search.query.impl.solr.SolrXmlPars;
import esg.search.utils.GeoUtils;
import thredds.catalog.InvDataset;
import thredds.catalog.ThreddsMetadata.GeospatialCoverage;

/**
 * Class that parsers a THREDDS <geospatialCoverage> element.
 * 
 * <geospatialCoverage zpositive="down">
 *      <northsouth>
 *          <start>36.6058</start>
 *          <size>0.0</size>
 *          <units>degrees_north</units>
 *      </northsouth>
 *      <eastwest>
 *          <start>-97.4888</start>
 *          <size>0.0</size>
 *          <units>degrees_west</units>
 *      </eastwest>
 *      <updown>
 *          <start>314.0</start>
 *          <size>0.0</size>
 *          <units>m</units>
 *      </updown>
 *  </geospatialCoverage>
 *
 * @author Luca Cinquini
 *
 */
public class GeospatialCoverageParser implements ThreddsElementParser {

    @Override
    public void parse(InvDataset dataset, Record record, final DatasetSummary ds) {
       
        final GeospatialCoverage gsc = dataset.getGeospatialCoverage();
        
        if (gsc!=null) {
            
            // record metadata
            if (gsc.getNorthSouthRange()!=null)
                record.addField(SolrXmlPars.FIELD_SOUTH, Double.toString(gsc.getNorthSouthRange().getStart()));
            if (gsc.getNorthSouthRange()!=null)
                record.addField(SolrXmlPars.FIELD_NORTH, Double.toString(gsc.getNorthSouthRange().getStart()+gsc.getNorthSouthRange().getSize()));
            if (gsc.getEastWestRange()!=null)
                record.addField(SolrXmlPars.FIELD_WEST, Double.toString(gsc.getEastWestRange().getStart()));
            if (gsc.getEastWestRange()!=null)
                record.addField(SolrXmlPars.FIELD_EAST, Double.toString(gsc.getEastWestRange().getStart()+gsc.getEastWestRange().getSize()));
    		// complete geo-spatial location
            // ENVELOPE(-10, 20, 15, 10) # ENVELOPE(minX, maxX, maxY, minY)
            // <field name="geo">ENVELOPE(-74.093, -69.347, 44.558, 41.042)</field>
    		if (   record.getFieldValue(SolrXmlPars.FIELD_WEST)!=null  && record.getFieldValue(SolrXmlPars.FIELD_EAST)!=null
    			&& record.getFieldValue(SolrXmlPars.FIELD_SOUTH)!=null && record.getFieldValue(SolrXmlPars.FIELD_NORTH)!=null) {
    			
    			List<float[]> latRanges = GeoUtils.convertLongitudeRangeto180( 
    					                     Float.parseFloat(record.getFieldValue(SolrXmlPars.FIELD_WEST)),
    					                     Float.parseFloat(record.getFieldValue(SolrXmlPars.FIELD_EAST)) );
    			for (float[] latRange : latRanges) {
	    			record.setField(SolrXmlPars.FIELD_GEO, // "minLon minLat maxLon maxLat"
	    					        "ENVELOPE("
	    					       + latRange[0]  + ", "
	    					       + latRange[1]  + ", "
	    					       + record.getFieldValue(SolrXmlPars.FIELD_NORTH) + ", "
	    					       + record.getFieldValue(SolrXmlPars.FIELD_SOUTH) 
	    					       + ")" );
    			}
    				  			
    		}
            
            if (gsc.getUpDownRange()!=null) {
                record.addField(SolrXmlPars.FIELD_HEIGHT_BOTTOM, Double.toString(gsc.getUpDownRange().getStart()));
                record.addField(SolrXmlPars.FIELD_HEIGHT_TOP, Double.toString(gsc.getUpDownRange().getStart()+gsc.getUpDownRange().getSize()));
                record.addField(SolrXmlPars.FIELD_HEIGHT_UNITS, gsc.getHeightUnits());
            }
            
            // summary metadata
            if (gsc.getNorthSouthRange()!=null) {        
                if (ds.latNorth<gsc.getLatNorth()) ds.latNorth = gsc.getLatNorth();
                if (ds.latSouth>gsc.getLatSouth()) ds.latSouth = gsc.getLatSouth();                
            }
            if (gsc.getEastWestRange()!=null) {                
                if (ds.lonEast<gsc.getLonEast()) ds.lonEast = gsc.getLonEast();
                if (ds.lonWest>gsc.getLonWest()) ds.lonWest = gsc.getLonWest();                
            }
            if (gsc.getUpDownRange()!=null) {
                if (ds.heightBottom>gsc.getUpDownRange().getStart()) ds.heightBottom=gsc.getUpDownRange().getStart();
                if (ds.heightTop<(gsc.getUpDownRange().getStart()+gsc.getUpDownRange().getSize())) 
                    ds.heightTop=(gsc.getUpDownRange().getStart()+gsc.getUpDownRange().getSize());
                if (StringUtils.hasText(gsc.getHeightUnits())) ds.heightUnits = gsc.getHeightUnits();
            }
            
        }

    }
}