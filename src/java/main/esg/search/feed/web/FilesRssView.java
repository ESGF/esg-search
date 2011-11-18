package esg.search.feed.web;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.view.feed.AbstractRssFeedView;

import com.sun.syndication.feed.rss.Channel;
import com.sun.syndication.feed.rss.Item;

import esg.search.core.Record;
import esg.search.query.api.QueryParameters;
import esg.search.query.api.SearchOutput;

/**
 * View responsible for building the RSS XML document for a list of files belonging to a single dataset.
 * 
 * @author Luca Cinquini
 *
 */
public class FilesRssView extends AbstractRssFeedView {
    
    /**
     * Configuration properties:
     */
    private Properties properties;

    @Override
    protected List<Item> buildFeedItems(final Map<String, Object> model, final HttpServletRequest req, final HttpServletResponse resp) throws Exception {
        
        // enclosing Dataset record
        SearchOutput datasetSearchOutput = (SearchOutput)model.get(FeedController.MODEL_KEY_DATASET);
        Record datasetRecord = datasetSearchOutput.getResults().get(0);
        
        // File records
        SearchOutput filesSearchOutput = (SearchOutput)model.get(FeedController.MODEL_KEY_FILES);
                
        // loop over File records
        List<Item> feedItems = new ArrayList<Item>();  
        for (final Record fileRecord : filesSearchOutput.getResults()) {
            
            Item feedItem = new Item();
            
            // <title>husNobs_AIRS_L3_RetStd-v5_200209-201105.nc</title>
            RssViewBuilder.addTitle(feedItem, fileRecord);  
            
            // <link>http://esg-datanode.jpl.nasa.gov/thredds/esgcet/1/obs4MIPs.NASA-JPL.AIRS.mon.v1.html
            //       ?dataset=obs4MIPs.NASA-JPL.AIRS.mon.v1.husNobs_AIRS_L3_RetStd-v5_200209-201105.nc</link>
            String datasetCatalogUrl = RssViewBuilder.getThreddsCatalogUrl(datasetRecord);
            String fileCatalogUrl = datasetCatalogUrl.replaceAll("\\#.*", "") + "?dataset=" + fileRecord.getId();
            feedItem.setLink( fileCatalogUrl.replace(".xml", ".html") );
            
            // <datacasting:acquisitionStartDate>Mon, 11 Jul 2011 00:00:00 GMT</datacasting:acquisitionStartDate>
            // <datacasting:acquisitionEndDate>Tue, 12 Jul 2011 00:00:00 GMT</datacasting:acquisitionEndDate>

            // <georss:where>
            
            // <enclosure url="http://esg-datanode.jpl.nasa.gov/thredds/fileServer/esg_dataroot/obs4MIPs/observations/atmos/husNobs/mon/grid/NASA-JPL/AIRS/v20110608/husNobs_AIRS_L3_RetStd-v5_200209-201105.nc" type="application/x-netcdf" />
            // <enclosure url="http://esg-datanode.jpl.nasa.gov/thredds/dodsC/esg_dataroot/obs4MIPs/observations/atmos/husNobs/mon/grid/NASA-JPL/AIRS/v20110608/husNobs_AIRS_L3_RetStd-v5_200209-201105.nc.html" type="text/html" />
            RssViewBuilder.addEnclosures(feedItem, fileRecord);
            
            // <datacasting:preview>
            
            // <description>MODSCW_P2011192_C4_1750_1755_1930_1935_GL05_closest_chlora.hdf</description>
            RssViewBuilder.addDescription(feedItem, fileRecord);
            
            // <pubDate>Wed, 24 Aug 2011 16:43:47 GMT</pubDate>
            RssViewBuilder.addPubDate(feedItem, fileRecord); 

            // <guid isPermaLink="false">my.file.identifier</guid>
            RssViewBuilder.addGuid(feedItem, fileRecord);
                        
            // <category domain="http://www.esgf.org/cv/0.1/experiment">obs</category>
            RssViewBuilder.addCategories(feedItem, fileRecord); 
            
            // <source url="http://esg-datanode.jpl.nasa.gov/thredds/esgcet/1/obs4MIPs.NASA-JPL.AIRS.mon.v1.xml?dataset=obs4MIPs.NASA-JPL.AIRS.mon.v1.husNobs_AIRS_L3_RetStd-v5_200209-201105.nc">ESGF-JPL RSS</source>
            RssViewBuilder.addSource(feedItem, fileRecord.getId(), fileCatalogUrl);
            
            feedItems.add(feedItem);  
            
        }
        
        return feedItems;
    }
    
    @Override
    protected void buildFeedMetadata(final Map<String, Object> model, final Channel feed, final HttpServletRequest request) {
        
        SearchOutput output = (SearchOutput)model.get(FeedController.MODEL_KEY_DATASET);
        Record datasetRecord = output.getResults().get(0);
        
        // <title>obs4MIPs NASA-JPL AIRS L3 Monthly Data</title>
        feed.setTitle(datasetRecord.getFieldValue(QueryParameters.FIELD_TITLE));  
        
        // <link>http://esg-datanode.jpl.nasa.gov/thredds/esgcet/1/obs4MIPs.NASA-JPL.AIRS.mon.v1.html#obs4MIPs.NASA-JPL.AIRS.mon.v1</link>
        feed.setLink(RssViewBuilder.getThreddsCatalogUrl(datasetRecord).replace(".xml", ".html"));  
        
        // <description>obs4MIPs.NASA-JPL.AIRS.mon</description>
        if (StringUtils.hasText(datasetRecord.getFieldValue(QueryParameters.FIELD_DESCRIPTION))) {
            feed.setDescription(datasetRecord.getFieldValue(QueryParameters.FIELD_DESCRIPTION));
        } else {
            feed.setDescription(datasetRecord.getId());
        }
        
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