package esg.search.feed.web;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.servlet.view.feed.AbstractRssFeedView;

import com.sun.syndication.feed.rss.Channel;
import com.sun.syndication.feed.rss.Item;

import esg.search.core.Record;
import esg.search.query.api.QueryParameters;
import esg.search.query.api.SearchOutput;

/**
 * View responsible for building the RSS XML document for a list of datasets.
 * 
 * @author Luca Cinquini
 */
public class DatasetsRssView extends AbstractRssFeedView {
            
    /**
     * Configuration properties:
     */
    private Properties properties;

    @Override
    protected List<Item> buildFeedItems(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        
        SearchOutput output = (SearchOutput)model.get(FeedController.MODEL_KEY_DATASETS);
        //final String requestUrl = request.getRequestURL().toString();
        
        List<Item> feedItems = new ArrayList<Item>();  
        for (Record datasetRecord : output.getResults()) {
            
            Item feedItem = new Item();
            
            // <title>obs4MIPs NASA-JPL AIRS L3 Monthly Data</title>
            RssViewBuilder.addTitle(feedItem, datasetRecord);
            
            // <link>http://esg-datanode.jpl.nasa.gov/thredds/esgcet/1/obs4MIPs.NASA-JPL.AIRS.mon.v1.html#obs4MIPs.NASA-JPL.AIRS.mon.v1</link>
            feedItem.setLink( datasetRecord.getFieldValue(QueryParameters.FIELD_URL) ); 
            
            // <description>obs4MIPs.NASA-JPL.AIRS.mon</description>
            RssViewBuilder.addDescription(feedItem, datasetRecord);             
            
            // <pubDate>Wed, 24 Aug 2011 16:43:47 GMT</pubDate>
            RssViewBuilder.addPubDate(feedItem, datasetRecord); 
            
            // <enclosure url="http://esg-datanode.jpl.nasa.gov/las/getUI.do?catid=893EB2D5C79AD40EE2436A3F118649CE_ns_obs4MIPs.NASA-JPL.AIRS.mon.husNobs.1.aggregation" type="text/html" />
            // <enclosure url="http://esg-datanode.jpl.nasa.gov/thredds/dodsC/obs4MIPs.NASA-JPL.AIRS.mon.husNobs.1.aggregation.1.dods" type="application/opendap-dods" />
            RssViewBuilder.addEnclosures(feedItem, datasetRecord);

            // <source url="http://localhost:8080/esg-search/feed/obs4MIPs.NASA-JPL.AIRS.mon.rss">obs4MIPs NASA-JPL AIRS L3 Monthly Data</source>
            RssViewBuilder.addSource(feedItem, 
                                     datasetRecord.getFieldValue(QueryParameters.FIELD_TITLE),
                                     RssViewBuilder.getRssBaseUri(request)+datasetRecord.getId()+".rss");
                        
            // <guid isPermaLink="false">obs4MIPs.NASA-JPL.AIRS.mon</guid>
            RssViewBuilder.addGuid(feedItem, datasetRecord);
            
            // <category domain="http://www.esgf.org/cv/0.1/experiment">obs</category>
            RssViewBuilder.addCategories(feedItem, datasetRecord); 
             
            feedItems.add(feedItem);  

        }
        
        return feedItems;
    }

    @Override
    protected void buildFeedMetadata(Map<String, Object> model, Channel feed, HttpServletRequest request) {
        
        // <title>ESGF-JPL Datasets RSS feed</title>
        feed.setTitle( RssViewBuilder.getFeedTitle(properties) );
        
        // <description>Datasets accessible from the ESGF-JPL Node</description>
        feed.setDescription(RssViewBuilder.getFeedDesc(properties));
        
        // <link>http://esg-datanode.jpl.nasa.gov/thredds/catalog.html</link>
        feed.setLink( RssViewBuilder.getFeedLink(properties, request) ); 
        
        // <language>en-us</language>
        feed.setLanguage("en-us");
        
        // <pubDate>Mon, 22 Aug 2011 22:00:20 GMT</pubDate>
        feed.setPubDate( new Date() );
        
        // <ttl>30</ttl>
        feed.setTtl(RssViewBuilder.TTL);
        
        super.buildFeedMetadata(model, feed, request);
        
    }

    @Required
    public void setProperties(Properties properties) {
        this.properties = properties;
    }
    
}