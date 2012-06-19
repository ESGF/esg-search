package esg.search.query.ws.rest;

import java.util.List;
import java.util.Map;

/**
 * Interface that defines the functionality for parsing search output documents
 * (to extract all the information necessary to generate wget scripts).
 * 
 * @author Luca Cinquini
 *
 */
public interface OutputDocumentParser {
    
    /**
     * Parses the output document of a search for datasets.
     * Returns the matching dataset identifiers organized by index node.
     * 
     * @param responseDocument
     * @return
     */
    Map<String, List<String>> extractDatasets(String responseDocument) throws Exception;

    /**
     * Parses the output document of a search for files and populates a Wget script object.
     * Returns the number of files found in the document.
     * 
     * @param responseDocument
     * @param desc
     * @return
     * @throws Exception
     */
    int extractFiles(String responseDocument, WgetScriptGenerator.WgetDescriptor desc) throws Exception;

}