package esg.search.publish.thredds.parsers;

import thredds.catalog.InvDataset;
import thredds.catalog.ThreddsMetadata.GeospatialCoverage;
import esg.search.core.Record;
import esg.search.query.impl.solr.SolrXmlPars;

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
    public void parse(InvDataset dataset, Record record) {
       
        final GeospatialCoverage gsc = dataset.getGeospatialCoverage();
        
        if (gsc!=null) {
            
            record.addField(SolrXmlPars.FIELD_SOUTH, Double.toString(gsc.getNorthSouthRange().getStart()));
        
            record.addField(SolrXmlPars.FIELD_NORTH, Double.toString(gsc.getNorthSouthRange().getStart()+gsc.getNorthSouthRange().getSize()));
            
            record.addField(SolrXmlPars.FIELD_WEST, Double.toString(gsc.getEastWestRange().getStart()));
            
            record.addField(SolrXmlPars.FIELD_EAST, Double.toString(gsc.getEastWestRange().getStart()+gsc.getEastWestRange().getSize()));
            
        }

    }
}
