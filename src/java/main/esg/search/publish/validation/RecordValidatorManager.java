package esg.search.publish.validation;

import java.util.List;
import java.util.Map;

import org.springframework.util.StringUtils;

import esg.search.core.Record;

/**
 * Class that manages records validation by invoking other validators
 * depending on the specified project.
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
        if (StringUtils.hasText(project)) {
            if (validators.containsKey(project)) {
                for (RecordValidator validator : validators.get(project)) {
                    validator.validate(record, errors);
                }
            }
        }
        
    }

}
