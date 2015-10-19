package esg.search.publish.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import esg.search.publish.api.PublishingService;
import esg.search.publish.security.AuthorizerAdapter;

/**
 * Subclass of {@link SecurePublishingServiceImpl} configured for publishing to the local Solr index.
 * It uses the same {@link AuthorizerAdapter} bean as its superclass.
 * 
 * @author Luca Cinquini
 *
 */
@Service("securePublishingServiceLocal")
public class SecurePublishingServiceImplLocal extends SecurePublishingServiceImpl {
    
    @Autowired
    public SecurePublishingServiceImplLocal(final @Qualifier("publishingServiceLocal") PublishingService publishingService) {
    	super(publishingService);
    }
    
}
