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
    
    // base validator
    RecordValidator baseValidator;
    
    // project-specific validators
    Map<String, List<RecordValidator>> validators;
    
    public RecordValidatorManager(RecordValidator baseValidator, 
                                  Map<String, List<RecordValidator>> validators) throws Exception {
       
        this.baseValidator = baseValidator;       
        this.validators = validators;
        
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
