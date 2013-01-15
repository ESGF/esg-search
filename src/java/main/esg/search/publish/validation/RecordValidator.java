package esg.search.publish.validation;

import java.util.List;

import esg.search.core.Record;

public interface RecordValidator {
    
    /**
     * Method to validate a document containing a single record.
     * @param record : XML record serialized as string
     * @param errors : list of validation errors (unless an exception is thrown)
     * @return : stub record object if valid
     * @throws Exception : if the record validation failed for some reason 
     *                     (implying that the record must be considered invalid)
     */
    Record validate(String record, List<String> errors) throws Exception;

}