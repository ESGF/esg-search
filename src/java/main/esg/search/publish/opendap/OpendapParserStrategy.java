package esg.search.publish.opendap;

import java.net.URI;
import java.util.List;

import esg.search.core.Record;

/**
 * API for parsing an OpenDAP dataset into one or more ESGF datasets
 * @author Luca Cinquini
 *
 */
public interface OpendapParserStrategy {
    
    /**
     * Method to parse a remote OpenDAP dataset into one or more searchable ESGF records.
     * 
     * @param url : OpenDAP URL
     * @param schema: optional compliance schema to be assigned to the datasets
     * @return : list of ESGF records to be published
     */
    List<Record> parse(String url, URI schema) throws Exception;

}
