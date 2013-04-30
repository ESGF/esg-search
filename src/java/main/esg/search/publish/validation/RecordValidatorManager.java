package esg.search.publish.validation;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.StringUtils;

import esg.common.util.ESGFProperties;
import esg.search.core.Record;
import esg.search.query.api.QueryParameters;

/**
 * Class that manages records validation by invoking other validators
 * depending on the specified project.
 * 
 * @author Luca Cinquini
 *
 */
public class RecordValidatorManager implements RecordValidator {
        
    // project-specific validators
    Map<String, RecordValidator> validators = new HashMap<String, RecordValidator>();
    
    // optional access control validator
    RecordValidator acValidator = null;
            
    /**
     * 
     * @param schemas : map of (schema name, schema relative path) pairs
     * @param properties : ESGF Properties object containing schema location
     * @throws Exception
     */
    public RecordValidatorManager(Map<String, String> schemas, ESGFProperties properties) throws Exception {
       
        // instantiate validators
        for (String uri : schemas.keySet()) {
            
            // use location from properties object
            if (StringUtils.hasText(properties.getProperty(QueryParameters.SCHEMA_LOCATION_PROPERTY))) {
                String path = properties.getProperty(QueryParameters.SCHEMA_LOCATION_PROPERTY);
                if (!path.endsWith("/")) path = path + "/";
                validators.put(uri, new SchemaRecordValidator(path+schemas.get(uri)));
            
            // use default location
            } else {
                validators.put(uri, new SchemaRecordValidator(QueryParameters.SCHEMA_DEFAULT_LOCATION+schemas.get(uri)));
            }
            
        }
        
    }
    
    // NOTE: comment out @Autowired to disable access control validation
    @Autowired
    public void setAcValidator(@Qualifier("acValidator") RecordValidator acValidator) {
        this.acValidator = acValidator;
    }


    @Override
    public void validate(Record record, List<String> errors) throws Exception {
        
        // optional access control validator
        if (acValidator!=null) {
            acValidator.validate(record, errors);
        }
                
        // always run core validator
        validators.get(QueryParameters.SCHEMA_ESGF).validate(record, errors);
        
        // also always run geo validator
        validators.get(QueryParameters.SCHEMA_GEO).validate(record, errors);
        
        // run schema specific validators
        URI uri = record.getSchema();
        if (uri!=null) {
            String schema = uri.toString();
            if (!schema.equals(QueryParameters.SCHEMA_ESGF) && !schema.equals(QueryParameters.SCHEMA_GEO)) {
                if (validators.containsKey(schema)) {
                    validators.get(schema).validate(record, errors);
                } else {
                    throw new Exception("Unknown validation schema: "+schema);
                }
            }
        }
        
    }

}
