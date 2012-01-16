package esg.search.feed.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import esg.search.query.api.QueryParameters;
import esg.search.query.api.SearchInput;
import esg.search.query.api.SearchOutput;
import esg.search.query.api.SearchService;
import esg.search.query.impl.solr.SearchInputImpl;

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
    public final static String MODEL_KEY_FEED_TITLE = "feed_title";
    
    // last update time span for returned records
    private final static String TIME_SPAN = "NOW-10DAY";
    
    private final static String NODES_FEED_TITLE = "ESGF RSS Feed";
    
    /**
     * Method that handles RSS feeds for records of type Dataset, across all nodes.
     * @param response
     * @param model
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/nodes.rss", method=RequestMethod.GET)  
    public String nodesFeed(final HttpServletResponse response, final Model model) throws Exception {  
        
        // set feed title
        model.addAttribute(MODEL_KEY_FEED_TITLE, NODES_FEED_TITLE);
        
        // build distributed dataset feed with no other constraints
        return this.datasetFeed(model, true, null);

    }
    
    /**
     * Method that handles RSS feeds for records of type Dataset, for only one node.
     * @param response
     * @param model
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/node.rss", method=RequestMethod.GET)  
    public String nodeFeed(final HttpServletResponse response, final Model model) throws Exception {  
        
        // build non-distributed dataset feed with no other constraints
        // use default feed title for this node
        return this.datasetFeed(model, false, null);

    }
    
    /**
     * Method that handles RSS feeds for records of type Dataset, for a specific facet name and value.
     * @param response
     * @param model
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/{facetName}/{facetValue}.rss", method=RequestMethod.GET)  
    public String facetFeed(@PathVariable("facetName") String facetName, @PathVariable("facetValue") String facetValue, 
                            final HttpServletResponse response, final Model model) throws Exception {  
        
        // set feed title
        model.addAttribute(MODEL_KEY_FEED_TITLE, "ESGF RSS Feed for "+facetName+"="+facetValue);
        
        // build distributed dataset feed with given project constraint
        final Map<String, String> constraints = new HashMap<String, String>();
        constraints.put(facetName, facetValue);
        
        return this.datasetFeed(model, false, constraints);

    }
                    
    /**
     * Method that handles RSS feeds for records of type File, for a specified dataset.
     * 
     * @param res
     * @param model
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/dataset/{datasetId}.rss", method=RequestMethod.GET)  
    public String datasetFeed(@PathVariable("datasetId") String datasetId, final HttpServletResponse res, final Model model) throws Exception {  
                           
        // search for single record of type dataset, across all nodes
        final SearchInput input1 = newSearchInput(QueryParameters.TYPE_DATASET, true);
        input1.addConstraint(QueryParameters.FIELD_ID, datasetId); 
        
        SearchOutput output1 = searchService.search(input1); 
        model.addAttribute(MODEL_KEY_DATASET, output1);  
        
        // search for all records of type file, with given parent
        final SearchInput input2 = newSearchInput(QueryParameters.TYPE_FILE, true);
        input2.addConstraint(QueryParameters.FIELD_DATASET_ID, datasetId);
        
        SearchOutput output2 = searchService.search(input2); 
        model.addAttribute(MODEL_KEY_FILES, output2);  
        
        // redirect to RSS view for single dataset
        return FILES_RSS_VIEW_NAME;
        
    }
    
    /**
     * Utility method to instantiate a query configured with specified parameters for RSS feeds
     * @param type
     * @param distrib
     */
    private SearchInput newSearchInput(String type, boolean distrib) {
        
        final SearchInput searchInput = new SearchInputImpl(type);
        searchInput.setDistrib(distrib);
        searchInput.setLimit(QueryParameters.MAX_LIMIT);
        return searchInput;
        
    }
    
    /**
     * Base method to build an RSS feed for record of type dataset.
     * @param response
     * @param model
     * @return
     * @throws Exception
     */
    private String datasetFeed(final Model model, boolean distrib, Map<String,String> constraints) throws Exception {  
        
        // search for all records of type dataset (across one node, or all nodes)
        final SearchInput input = newSearchInput(QueryParameters.TYPE_DATASET, distrib);
        
        // add additional constraints
        if (constraints!=null) {
            for (final String key : constraints.keySet()) {
                input.addConstraint(key, constraints.get(key));
            }
        }
        
        // only return most recent datasets
        input.addConstraint(QueryParameters.FROM, TIME_SPAN);
        
        SearchOutput output = searchService.search(input); 
        model.addAttribute(MODEL_KEY_DATASETS, output);  
        
        // redirect to RSS top-level view
        return DATASETS_RSS_VIEW_NAME;

    }

    @Autowired
    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }  

}
