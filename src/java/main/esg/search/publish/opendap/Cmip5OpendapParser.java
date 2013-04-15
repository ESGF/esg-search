package esg.search.publish.opendap;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ucar.nc2.Attribute;
import ucar.nc2.dataset.NetcdfDataset;
import esg.search.core.Record;
import esg.search.publish.thredds.ThreddsPars;
import esg.search.query.api.QueryParameters;
import esg.search.query.impl.solr.SolrXmlPars;
import esg.search.utils.DateUtils;

/**
 * CMIP5 specific implementation of {@link OpendapParser}:
 * parses the OpenDAP stream to generate ESGF datasets 
 * that conform to the CMIP5 schema.
 * 
 * @author Luca Cinquini
 *
 */
public class Cmip5OpendapParser extends DefaultOpendapParser {
    
    private final Log LOG = LogFactory.getLog(this.getClass());
    
    public Cmip5OpendapParser(Properties props) {
        super( props );
    }
    
    protected void setAttributes(NetcdfDataset ncd, Record record) {
        
        // global attributes -> dataset properties
        List<Attribute> attributes = ncd.getGlobalAttributes();
        for (Attribute attribute : attributes) {
            String aname = attribute.getName();
            String avalue = attribute.getStringValue();
            
            // remove trailing '_id' from global attributes:
            // 'institute_id' -> 'institute'
            // 'project_id' -> 'project'
            // 'model_id' -> 'model'
            // 'experiment_id' -> 'experiment'
            aname = aname.replaceAll("_id$", "");
            
            // correct attribute name
            if (aname.equals("frequency")) aname = "time_frequency";
            
            // transfer attribute (name, value) pairs
            record.setField(aname, avalue);
                         
            // global attributes that are used for special record fields                
            // mip_specs: CMIP5 --> used to set the validation schema
            if (aname.equalsIgnoreCase(ThreddsPars.MIP_SPECS)) {
                try {
                    record.setSchema(new URI(avalue.toLowerCase()));
                } catch(URISyntaxException e) {
                    LOG.warn(e.getMessage());
                }
            
            // creation_date: 2011-06-07T20:27:48Z --> used to set the dataset timestamp
            } else if (aname.equalsIgnoreCase(ThreddsPars.CREATION_DATE)) {
                try {
                    Date date = DateUtils.parse(avalue) ;
                    record.setField(QueryParameters.FIELD_TIMESTAMP, 
                                    SolrXmlPars.SOLR_DATE_TIME_FORMATTER.format(date));
                } catch(ParseException e) {
                    LOG.warn(e.getMessage());
                }
            }
            
        }
        
    }      
    
}