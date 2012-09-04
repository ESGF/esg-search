package esg.search.publish.thredds.parsers;

import thredds.catalog.InvDataset;
import ucar.nc2.units.DateRange;
import esg.search.core.Record;
import esg.search.query.impl.solr.SolrXmlPars;

/**
 * Class that parses a THREDDS <timeCoverage> element.
 * 
 * Note: not all representations are covered, just the following
 * <timeCoverage zpositive="down">
 *      <start>1999-11-16T12:00</start>
 *      <end>2009-11-16T12:00</end>
 *  </timeCoverage>
 * 
 */

public class TimeCoverageParser implements ThreddsElementParser {

    @Override
    public void parse(InvDataset dataset, Record record, final DatasetSummary ds) {
    
        final DateRange dateRange = dataset.getTimeCoverage();
        
        if (dateRange!=null) {
            
            // record metadata
            record.addField(SolrXmlPars.FIELD_DATETIME_START, dateRange.getStart().toDateTimeStringISO());  
            record.addField(SolrXmlPars.FIELD_DATETIME_STOP, dateRange.getEnd().toDateTimeStringISO());
            
            // summary metadata
            if (ds.dateRange==null) {
                ds.dateRange = dateRange;
                
            } else {
                // define time limits to be most inclusive
                if (dateRange.getStart()!=null) {
                    if (ds.dateRange.getStart()==null || dateRange.getStart().before(ds.dateRange.getStart().getDate())) {
                        ds.dateRange.setStart( dateRange.getStart() );
                    } 
                }
                if (dateRange.getEnd()!=null) {
                    if (ds.dateRange.getEnd()==null || dateRange.getEnd().after(ds.dateRange.getEnd().getDate())) {
                        ds.dateRange.setEnd( dateRange.getEnd() );
                    } 
                }

            }
            
        }

    }

}
