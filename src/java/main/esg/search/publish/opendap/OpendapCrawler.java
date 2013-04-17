package esg.search.publish.opendap;

import java.net.URI;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import esg.search.core.Record;
import esg.search.publish.api.MetadataRepositoryCrawler;
import esg.search.publish.api.MetadataRepositoryCrawlerListener;
import esg.search.publish.api.MetadataRepositoryType;
import esg.search.publish.api.RecordProducer;

/**
 * Implementation of {@link MetadataRepositoryCrawler} for data accessible via OpenDAP.
 * The actual parsing of the OpenDAP dataset is delegated to the @OpendapParser implementation,
 * while this class provides the connection with the rest of the publishing infrastructure.
 * 
 * @author Luca Cinquini
 *
 */
@Service("opendapCrawler")
public class OpendapCrawler implements MetadataRepositoryCrawler {
    
    private MetadataRepositoryCrawlerListener listener = null;
    
    private final Log LOG = LogFactory.getLog(this.getClass());
    
    /**
     * Collaborator responsible for parsing the OpenDAP dataset.
     */
    private OpendapParserStrategy parser;
    
    @Autowired
    public OpendapCrawler(final OpendapParserStrategy parser) {
        this.parser = parser;
    }


    /**
     * NOTE: @param 'filter' is currently disregarded as no recusrive behavior is necessary
     * NOTE: @param 'publish' is also currently disregarded
     */
    @Override
    public void crawl(URI uri, String filter, boolean recursive, RecordProducer callback, boolean publish, URI schema) throws Exception {
    
        if (LOG.isInfoEnabled()) LOG.info("Crawling OpenDAP URI="+uri);
        
        try {
            
            // parse OpenDAP dataset into records
            List<Record> records = parser.parse(uri.toString(), schema);
            
            // publish/unpublish records
            callback.notify(records);
            
            // notify listener of successful completion
            if (listener!=null) listener.afterCrawlingSuccess(uri.toString());
            
        } catch(Exception e) {
                       
            // notify listener of crawling error
            if (listener!=null) listener.afterCrawlingError(uri.toString());
           
            // throw the exception up the stack
            throw e;
            
        }

    }

    @Override
    public MetadataRepositoryType supports() {
        return MetadataRepositoryType.OPENDAP;
    }

    @Override
    public void setListener(MetadataRepositoryCrawlerListener listener) {
        this.listener = listener;
    }

}
