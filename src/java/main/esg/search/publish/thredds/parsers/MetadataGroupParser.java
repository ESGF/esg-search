package esg.search.publish.thredds.parsers;

import thredds.catalog.InvDataset;
import esg.search.core.Record;

/**
 * Class that parses a THREDDS <metadata> element.
 * Currently it delegates parsing to the {@link GeospatialCoverageParser} and {@link TimeCoverageParser}.
 * 
 * @author Luca Cinquini
 *
 */
public class MetadataGroupParser implements ThreddsElementParser {
    
    private final GeospatialCoverageParser geoParser;
    private final TimeCoverageParser timeParser;
    
    public MetadataGroupParser() {
        geoParser = new GeospatialCoverageParser();
        timeParser = new TimeCoverageParser();
    }

    @Override
    public void parse(InvDataset dataset, Record record) {
        
        geoParser.parse(dataset, record);
        timeParser.parse(dataset, record);

    }

}
