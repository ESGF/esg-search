package esg.search.publish.validation;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    
    public RecordValidatorManager(Map<String, String> schemas) throws Exception {
       
        // instantiate validators
        for (String uri : schemas.keySet()) {
            validators.put(uri, new SchemaRecordValidator(schemas.get(uri)));
        }
        
    }

    @Override
    public void validate(Record record, List<String> errors) throws Exception {
                
        // always run core validator
        validators.get(QueryParameters.SCHEMA_ESGF).validate(record, errors);
        
        // also always run geo validator
        validators.get(QueryParameters.SCHEMA_GEO).validate(record, errors);
        
        // run schema specific validators
        URI uri = record.getSchema();
        if (uri!=null) {
            String schema = uri.toString();
            if (validators.containsKey(schema)) {
                validators.get(schema).validate(record, errors);
            } else {
                throw new Exception("Unknown validation schema: "+schema);
            }
        }
        
    }

}
