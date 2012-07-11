package esg.search.publish.thredds.parsers;

import java.text.ParseException;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import thredds.catalog.InvDataset;
import thredds.catalog.InvProperty;
import esg.search.core.Record;
import esg.search.publish.thredds.ThreddsPars;
import esg.search.query.api.QueryParameters;
import esg.search.query.impl.solr.SolrXmlPars;

/**
 * Class that parses a set of THREDDS <property> elements.
 * 
 * <property name="..." value="..." />
 * 
 * @author Luca Cinquini
 *
 */
public class PropertiesParser implements ThreddsElementParser {
    
    private final Log LOG = LogFactory.getLog(this.getClass());

    @Override
    public void parse(InvDataset dataset, Record record) {
    
        for (final InvProperty property : dataset.getProperties()) {
            
            if (LOG.isTraceEnabled()) LOG.trace("Property: " + property.getName() + "=" + property.getValue());
            
            if (property.getName().equals(ThreddsPars.DATASET_ID) 
                || property.getName().equals(ThreddsPars.FILE_ID)
                || property.getName().equals(ThreddsPars.AGGREGATION_ID)) {
                // note: override "master_id" with version-independent identifier
                // <property name="dataset_id" value="obs4MIPs.NASA-JPL.AIRS.mon"/>
                // <property name="file_id" value="obs4MIPs.NASA-JPL.AIRS.mon.husNobs_AIRS_L3_RetStd-v5_200209-201105.nc"/>
                record.setMasterId(property.getValue());
                
            } else if (property.getName().equals(QueryParameters.FIELD_TITLE)) {
                // note: record title already set from dataset name
                record.addField(QueryParameters.FIELD_DESCRIPTION, property.getValue());
                
            } else if (property.getName().equals(ThreddsPars.DATASET_VERSION) || property.getName().equals(ThreddsPars.FILE_VERSION)) {
                // note: map "dataset_version", "file_version" to "version"
                try {
                    record.setVersion(Long.parseLong(property.getValue()));
                } catch (NumberFormatException e) {
                    // no version, defaults to 0
                }
                
            } else if (property.getName().equals(ThreddsPars.SIZE)) {
                record.addField(SolrXmlPars.FIELD_SIZE, property.getValue());
                
            // set replica flag
            } else if (property.getName().equals(ThreddsPars.IS_REPLICA)) {
                record.setReplica(Boolean.parseBoolean(property.getValue()));
                
            
            // "creation_time", "mod_time" --> "timestamp"
            } else if (property.getName().equals(ThreddsPars.CREATION_TIME) || property.getName().equals(ThreddsPars.MOD_TIME)) {
                try {
                    final Date date = ThreddsPars.THREDDS_DATE_TIME_PARSER.parse(property.getValue());
                    record.setField(QueryParameters.FIELD_TIMESTAMP, SolrXmlPars.SOLR_DATE_TIME_FORMATTER.format(date));
                } catch(ParseException e) {
                    LOG.warn("Error parsing date/time field: property name="+property.getName()+" value="+property.getValue());
                    LOG.warn(e.getMessage());
                }
                
            // other date/time properties
            } else if (   property.getName().endsWith(ThreddsPars.DATE) || property.getName().endsWith(ThreddsPars.TIME) ) {
                try {
                    final Date date = ThreddsPars.THREDDS_DATE_TIME_PARSER.parse(property.getValue());
                    record.setField(property.getName(), SolrXmlPars.SOLR_DATE_TIME_FORMATTER.format(date));
                } catch(ParseException e) {
                    LOG.warn("Error parsing date/time field: property name="+property.getName()+" value="+property.getValue());
                    LOG.warn(e.getMessage());
                }
                
            } else {
                // index all other properties verbatim
                record.addField(property.getName(), property.getValue());
            }
        }

    }

}
