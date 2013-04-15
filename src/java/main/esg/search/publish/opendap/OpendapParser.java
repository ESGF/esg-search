package esg.search.publish.opendap;

import java.util.List;

import esg.search.core.Record;

/**
 * API for parsing an OpenDAP dataset into one or more ESGF datasets
 * @author Luca Cinquini
 *
 */
public interface OpendapParser {
    
    /**
     * 
     * @param url : OpenDAP URL
     * @param publish: true to generate records to be published, false otherwise
     * @return : list of ESGF records to be published
     */
    List<Record> parse(String url, boolean publish);

}
