package esg.search.publish.validation;

public interface RecordValidator {
    
    /**
     * Method to validate a document containing a single record.
     * @param record : XML record serialized as string
     * @return : the record type if valid
     * @throws Exception : if the record is invalid
     */
    String validate(String record) throws Exception;

}
