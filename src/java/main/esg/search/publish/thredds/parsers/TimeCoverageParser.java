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
    
        final DateRange daterange = dataset.getTimeCoverage();
        
        if (daterange!=null) {
            record.addField(SolrXmlPars.FIELD_DATETIME_START, daterange.getStart().toDateTimeStringISO());  
            record.addField(SolrXmlPars.FIELD_DATETIME_STOP, daterange.getEnd().toDateTimeStringISO());
        }

    }

}
