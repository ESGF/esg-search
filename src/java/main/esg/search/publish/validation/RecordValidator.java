package esg.search.publish.validation;

import esg.search.core.Record;

public interface RecordValidator {
    
    /**
     * Method to validate a document containing a single record.
     * @param record : XML record serialized as string
     * @return : stub record object if valid
     * @throws Exception : if the record is invalid
     */
    Record validate(String record) throws Exception;

}
