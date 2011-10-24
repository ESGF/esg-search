package esg.search.feed.web;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import esg.search.query.api.SearchInput;
import esg.search.query.api.SearchOutput;
import esg.search.query.api.SearchService;
import esg.search.query.impl.solr.SearchInputImpl;
import esg.search.query.impl.solr.SolrXmlPars;

/**
 * Controller responsible for processing ALL syndication feed requests (RSS and Atom).
 * 
 * @author Luca Cinquini
 *
 */
@Controller("datasetsFeedController")
@RequestMapping("/feed") 
public class FeedController {
    
    /**
     * Service responsible for retrieving feed records.
     */
    private SearchService searchService;
    
    public final static String DATASETS_RSS_VIEW_NAME = "datasetsRssView";
    public final static String FILES_RSS_VIEW_NAME = "filesRssView";
    public final static String MODEL_KEY_DATASETS = "datasets";
    public final static String MODEL_KEY_DATASET = "dataset";
    public final static String MODEL_KEY_FILES= "files";
    public final static String DATASETS_URI = "datasets";
                
    /**
     * Method that parses the HTTP request, executes the appropriate search for records, and redirect to the datasets-level
     * of files-level dataset view.
     * 
     * @param res
     * @param model
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/{datasetId}.rss", method=RequestMethod.GET)  
    public String rss(@PathVariable("datasetId") String datasetId, final HttpServletResponse res, final Model model) throws Exception {  
               
        if (datasetId.equals(DATASETS_URI)) {  
            
            // search for all records of type dataset
            final SearchInput input = new SearchInputImpl();
            input.setType(SolrXmlPars.TYPE_DATASET); 
            
            SearchOutput output = searchService.search(input); 
            model.addAttribute(MODEL_KEY_DATASETS, output);  
            
            // redirect to RSS top-level view
            return DATASETS_RSS_VIEW_NAME;
            
        } else {
            
            // search for single record of type dataset
            final SearchInput input1 = new SearchInputImpl();
            input1.setType(SolrXmlPars.TYPE_DATASET);
            input1.addConstraint(SolrXmlPars.FIELD_ID, datasetId); 
            SearchOutput output1 = searchService.search(input1); 
            model.addAttribute(MODEL_KEY_DATASET, output1);  
            
            // search for all records of type file, with given parent
            final SearchInput input2 = new SearchInputImpl();
            input2.setType(SolrXmlPars.TYPE_FILE); 
            input2.addConstraint(SolrXmlPars.FIELD_PARENT_ID, datasetId);
            
            SearchOutput output2 = searchService.search(input2); 
            model.addAttribute(MODEL_KEY_FILES, output2);  
            
            // redirect to RSS view for single dataset
            return FILES_RSS_VIEW_NAME;
        }
        
    }

    @Autowired
    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }  

}
