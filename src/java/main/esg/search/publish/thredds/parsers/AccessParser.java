package esg.search.publish.thredds.parsers;

import thredds.catalog.InvAccess;
import thredds.catalog.InvDataset;
import esg.search.core.Record;
import esg.search.core.RecordHelper;
import esg.search.publish.thredds.ThreddsPars;
import esg.search.query.api.QueryParameters;

/**
 * Class that parses a THREDDS <access> element.
 * 
 * <access urlPath="/ipcc/sresb1/atm/3h/hfss/miroc3_2_hires/run1/hfss_A3_2050.nc" serviceName="GRIDFTPatPCMDI" dataFormat="NetCDF" />
 * 
 * @author Luca Cinquini
 *
 */
public class AccessParser implements ThreddsElementParser {

    @Override
    public void parse(final InvDataset dataset, final Record record, final DatasetSummary ds) {
        
        for (final InvAccess access : dataset.getAccess()) {
                                   
            String url = access.getStandardUri().toString();
            final String type = access.getService().getServiceType().toString();
            // special processing of opendap endpoints since URL in thredds catalog is unusable without a suffix
            if (type.equalsIgnoreCase(ThreddsPars.SERVICE_TYPE_OPENDAP)) url += ".html";
            
            // encode URL tuple
            record.addField(QueryParameters.FIELD_URL, 
                            RecordHelper.encodeUrlTuple(url, ThreddsPars.getMimeType(url, type), access.getService().getDescription() ));

            // add access type to summary metadata
            ds.access.add(type);
        
        }

    }

}
