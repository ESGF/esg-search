package esg.search.publish.opendap;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StringUtils;

import ucar.nc2.Attribute;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.ncml.Aggregation;
import esg.search.core.Record;
import esg.search.core.RecordHelper;
import esg.search.core.RecordImpl;
import esg.search.publish.thredds.ThreddsPars;
import esg.search.publish.thredds.ThreddsUtils;
import esg.search.query.api.QueryParameters;
import esg.search.query.impl.solr.SolrXmlPars;

/**
 * Default implementation of {@link OpendapParser} based on reasonable assumptions:
 * 
 * o) it creates one ESGF record of type 'Dataset' for each OpenDAP URL
 * o) all global attributes are used to populate corresponding dataset search properties
 * o) all (non-coordinate) variables are included in the resulting dataset
 * 
 * @author Luca Cinquini
 *
 */
public class DefaultOpendapParser implements OpendapParser {
    
    /**
     * Optional properties used to set fix attributes.
     */
    private Properties properties = new Properties();
    
    private final Log LOG = LogFactory.getLog(this.getClass());
    
    /**
     * No argument constructor.
     */
    public DefaultOpendapParser() {}
    
    public DefaultOpendapParser(Properties props) {
        this.properties = props;
    }
    
    public void setProperties(Properties props) {
        this.properties = props;
    }

    @Override
    public List<Record> parse(String url, boolean publish) {
        
        if (LOG.isInfoEnabled()) LOG.info("Parsing OpenDAP url="+url);

        // create (list of) records
        Record record = new RecordImpl();

        NetcdfDataset ncd = null;
        try {
          
          // open dataset
          ncd = NetcdfDataset.openDataset(url);
          
          // global attributes -> dataset properties
          this.setAttributes(ncd, record);
          
          // 'id'
          this.setId(ncd, record);
          
          // 'master_id'
          this.setMasterId(ncd, record);
          
          // 'instance_id'
          this.setInstanceId(ncd, record);
          
          // 'type'
          record.setType(QueryParameters.TYPE_DATASET);
          
          // 'title'
          this.setTitle(ncd, record);
                              
          // OpenDAP URL
          record.addField(QueryParameters.FIELD_URL, 
                          RecordHelper.encodeUrlTuple(url, 
                                                      ThreddsUtils.getMimeType(url, ThreddsPars.SERVICE_TYPE_OPENDAP),
                                                      ThreddsPars.SERVICE_TYPE_OPENDAP));
          record.addField(QueryParameters.FIELD_ACCESS, ThreddsPars.SERVICE_TYPE_OPENDAP);
          record.addField(QueryParameters.FIELD_NUMBER_OF_FILES, "0");
          Aggregation agg = ncd.getAggregation();
          if (agg!=null) {
              record.addField(QueryParameters.FIELD_NUMBER_OF_AGGREGATIONS, "1");
          } else {
              record.addField(QueryParameters.FIELD_NUMBER_OF_AGGREGATIONS, "0");
          }
          
          // opendap URL -> "data_node"
          URL _url = new URL(url);
          record.addField(QueryParameters.FIELD_DATA_NODE, _url.getHost());
                    
          // variables
          this.setVariables(ncd, record);
          
          // fixed properties
          this.setFixedProperties(ncd, properties, record);
                              
        } catch(Exception e) {
           LOG.warn(e.getMessage());
        } finally {
          try { if (ncd!=null) ncd.close(); } catch(Exception e) { LOG.warn(e.getMessage()); }
        }        
        
        List<Record> records = new ArrayList<Record>();
        records.add(record);
        return records;
        
    }
    
    /**
     * Default method to generate the dataset 'id'.
     * This method may be overridden by subclasses.
     * 
     * @param ncd
     * @param record
     * @return
     */
    protected void setId(NetcdfDataset ncd, Record record) {
        record.setId( ncd.getLocation() );
    }
    
    /**
     * Default method to generate the dataset 'master_id':
     * the master_id is set equal to the last part of the opendap URL.
     * This method may be overridden by subclasses.
     * 
     * @param ncd
     * @param record
     * @return
     */
    protected void setMasterId(NetcdfDataset ncd, Record record) {
        String location = ncd.getLocation();
        String masterId = location.substring(location.lastIndexOf("/")+1);
        record.setField(QueryParameters.FIELD_MASTER_ID, masterId);
    }
    
    /**
     * Default method to generate the dataset 'instance_id':
     * the instance_id is set equal to the 'master_id' plus the record version.
     * This method may be overridden by subclasses.
     * 
     * @param ncd
     * @param record
     * @return
     */
    protected void setInstanceId(NetcdfDataset ncd, Record record) {
        String instanceId = record.getMasterId()+"."+record.getVersion();
        record.setField(QueryParameters.FIELD_INSTANCE_ID, instanceId);
    }
    
    /**
     * Default method to generate the dataset variables information.
     * This method may be overridden by subclasses.
     * 
     * @param ncd
     * @param record
     * @param records
     */
    protected void setVariables(NetcdfDataset ncd, Record record) {
        
        // variables
        List<Variable> variables = ncd.getVariables();
        for (Variable variable : variables) {
            String vname = variable.getName();
            
            if (!variable.isCoordinateVariable() 
                && !vname.contains("bnds")) { // skip grid boundaries
            
                // NOTE: these arrays must always have the same number of entries
                record.addField(SolrXmlPars.FIELD_VARIABLE, vname);
                
                if (StringUtils.hasText(variable.getDescription())) record.addField(SolrXmlPars.FIELD_VARIABLE_LONG_NAME, variable.getDescription());
                else record.addEmptyField(SolrXmlPars.FIELD_VARIABLE_LONG_NAME);
                
                if (StringUtils.hasText(variable.getUnitsString())) record.addField(SolrXmlPars.FIELD_VARIABLE_UNITS, variable.getUnitsString());
                else record.addEmptyField(SolrXmlPars.FIELD_VARIABLE_UNITS);
                
                // parse variable attributes for standard name (assumes CF convention)
                String standardName = null;
                for (Attribute vatt : variable.getAttributes()) {
                    if (vatt.getName().toLowerCase().equals(ThreddsPars.STANDARD_NAME)) {
                        standardName = vatt.getStringValue();
                    }
                }
                if (StringUtils.hasText(standardName)) record.addField(SolrXmlPars.FIELD_CF_STANDARD_NAME, standardName);
                else record.addEmptyField(SolrXmlPars.FIELD_CF_STANDARD_NAME);
                
            }

        }       
        
    }
    
    
    /**
     * Default implementation to set the dataset attributes from the opendap global attributes:
     * it simply transfers all opendap attributes to the searchable dataset,
     * maintaining the same name and value.
     * 
     * This method may be overridden by subclasses.
     * 
     * @param ncd
     * @param record
     */
    protected void setAttributes(NetcdfDataset ncd, Record record) {
        
        List<Attribute> attributes = ncd.getGlobalAttributes();
        for (Attribute attribute : attributes) {
            record.setField(attribute.getName(), attribute.getStringValue());
        }
        
    }
    
    /**
     * Default implementation to set the dataset title:
     * it will set it to the last part of the opendap URL,
     * unless a global attribute 'title' was previously found.
     * 
     * This method may be overridden by subclasses.
     * 
     * @param ncd
     * @param record
     */
    protected void setTitle(NetcdfDataset ncd, Record record) {
        
        // record title (may have been set from 'title' global attribute)
        String location = ncd.getLocation();
        if (!StringUtils.hasText(record.getFieldValue(QueryParameters.FIELD_TITLE))) {           
            record.setField(QueryParameters.FIELD_TITLE, location.substring(location.lastIndexOf("/")+1));
        }
        
    }
    
    /**
     * By default the resulting dataset is NOT a replica.
     * 
     * @param ncd
     * @param record
     */
    protected void setReplica(NetcdfDataset ncd, Record record) {
        record.setReplica(false);
    }
    
    /**
     * By default the resulting dataset is the latest version.
     * 
     * @param ncd
     * @param record
     */
    protected void setLatest(NetcdfDataset ncd, Record record) {
        record.setLatest(true);
    }
    
    /**
     * Default implementation to set the fixed dataset properties
     * @param ncd
     * @param props
     * @param record
     */
    protected void setFixedProperties(NetcdfDataset ncd, Properties props, Record record) {
        for (String key : properties.stringPropertyNames()) {
            String value = properties.getProperty(key);
            record.addField(key, value);
        }
    }

}
