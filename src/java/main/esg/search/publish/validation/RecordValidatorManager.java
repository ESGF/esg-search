package esg.search.publish.validation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import esg.search.core.Record;

/**
 * Class that validates records by invoking multiple other validators.
 * 
 * @author Luca Cinquini
 *
 */
public class RecordValidatorManager implements RecordValidator {
    
    // core validator
    RecordValidator baseValidator;
    
    // map (project, validators)
    Map<String, RecordValidator[]> validators = new HashMap<String, RecordValidator[]>();
    
    public RecordValidatorManager() throws Exception {
       
        baseValidator = new SchemaRecordValidator("esg/search/config/esgf.xml");
        
        // FIXME: read map of project-specific validators from Spring configuration
        RecordValidator geoValidator = new SchemaRecordValidator("esg/search/config/geo.xml");
        RecordValidator cmip5Validator = new SchemaRecordValidator("esg/search/config/cmip5.xml");
        validators.put("cmip5", new RecordValidator[] { geoValidator, cmip5Validator } );      
        validators.put("obs4MIPs", new RecordValidator[] { geoValidator, cmip5Validator } );
        
    }

    @Override
    public void validate(Record record, List<String> errors) throws Exception {
        
        // always run core validator
        baseValidator.validate(record, errors);
        
        // run project specific validators
        String project = record.getFieldValue("project");
        if (validators.containsKey(project)) {
            for (RecordValidator validator : validators.get(project)) {
                validator.validate(record, errors);
            }
        }
        
    }

}
