package esg.search.publish.validation;

import java.util.List;

import esg.search.core.Record;

public interface RecordValidator {
    
    /**
     * Method to validate a document containing a single record.
     * @param record : record to be validated as Java object
     * @param errors : list of validation errors (unless an exception is thrown)
     *
     * @throws Exception : if the record validation failed for some reason 
     *                     (implying that the record must be considered invalid)
     */
    void validate(Record record, List<String> errors) throws Exception;
    
}