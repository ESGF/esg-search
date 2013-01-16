package esg.search.publish.validation;

import java.util.ArrayList;
import java.util.List;

import esg.search.core.Record;

/**
 * Class that validates records by invoking multiple other validators.
 * 
 * @author Luca Cinquini
 *
 */
public class RecordValidatorManager implements RecordValidator {
    
    List<RecordValidator> validators = new ArrayList<RecordValidator>();
    
    public RecordValidatorManager() throws Exception {
        validators.add(new CoreRecordValidator());
    }

    @Override
    public Record validate(String record, List<String> errors) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

}
