package esg.search.publish.thredds.parsers;

import thredds.catalog.InvDataset;
import thredds.catalog.InvDatasetImpl;
import esg.search.core.Record;
import esg.search.query.impl.solr.SolrXmlPars;

/**
 * Class that parses a THREDDS <dataSize> element.
 * 
 * <thredds:dataSize units="bytes">3727917</thredds:dataSize>
 * 
 * @author Luca Cinquini
 *
 */
public class DataSizeParser implements ThreddsElementParser {
	
    @Override
    public void parse(final InvDataset dataset, final Record record, final DatasetSummary ds) {
        
    	if (dataset instanceof InvDatasetImpl) {
    		InvDatasetImpl _dataset = (InvDatasetImpl)dataset;
    		if (_dataset.getDataSize()!=Double.NaN ) {
    			// must convert from Double to Long
    			record.addField(SolrXmlPars.FIELD_SIZE, ""+ (long)_dataset.getDataSize());
    		}
    	}
    }

}
