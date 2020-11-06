package esg.search.publish.impl;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import esg.search.publish.api.MetadataRepositoryType;
import esg.search.publish.api.PublishingException;
import esg.search.publish.api.PublishingService;
import esg.search.publish.security.AuthorizerAdapter;

/**
 * Implementation of {@link PublishingService} secured via an {@link AuthorizerAdapter} bean.
 * After enforcing security, this class delegates all publishing/unpublishing operations to the underlying PublishingService
 * 
 * @author Luca Cinquini
 *
 */
@Service("securePublishingService")
public class SecurePublishingServiceImpl implements PublishingService {
    
    /**
     * Collaborator used to secure publishing calls. 
     * If none is configured, no authorization will take place.
     */
    private AuthorizerAdapter authorizer = null;
    
    private PublishingService publishingService;
    
    //private final Log LOG = LogFactory.getLog(this.getClass());

    @Autowired
    public SecurePublishingServiceImpl(final @Qualifier("publishingService") PublishingService publishingService) {
        this.publishingService = publishingService;
    }
    
    public void setAuthorizerAdpater(AuthorizerAdapter authorizer) {
        this.authorizer = authorizer;
    }

    @Override
    public void publish(String uri, String filter, boolean recursive, MetadataRepositoryType metadataRepositoryType, URI schema) throws PublishingException {
        
    	if (authorizer!=null) authorizer.checkAuthorization(uri);
        this.publishingService.publish(uri, filter, recursive, metadataRepositoryType, schema);

    }

    @Override
    public void unpublish(String uri, String filter, boolean recursive, MetadataRepositoryType metadataRepositoryType) throws PublishingException {
        
    	if (authorizer!=null) authorizer.checkAuthorization(uri);
        this.publishingService.unpublish(uri, filter, recursive, metadataRepositoryType);

    }

    @Override
    public void unpublish(List<String> ids) throws PublishingException {
        
    	if (authorizer!=null)  {
    		for (String id : ids) authorizer.checkAuthorization(id);
    	}
        this.publishingService.unpublish(ids);

    }
    
    @Override
    public void retract(List<String> ids) throws PublishingException {
        
    	if (authorizer!=null)  {
    		for (String id : ids) authorizer.checkAuthorization(id);
    	}
        this.publishingService.retract(ids);

    }
    


}
