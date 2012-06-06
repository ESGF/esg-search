package esg.search.publish.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opensaml.saml2.core.Action;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Service;

import esg.orp.app.Authorizer;
import esg.search.publish.api.MetadataRepositoryType;
import esg.search.publish.api.PublishingException;
import esg.search.publish.api.PublishingService;

/**
 * Implementation of {@link PublishingService} secured via an {@link Authorizer} bean.
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
    private Authorizer authorizer = null;
    
    private PublishingService publishingService;
    
    private final Log LOG = LogFactory.getLog(this.getClass());

    @Autowired
    public SecurePublishingServiceImpl(final PublishingService publishingService) {
        this.publishingService = publishingService;
    }
    
    @Autowired
    public void setAuthorizer(Authorizer authorizer) {
        this.authorizer = authorizer;
    }

    @Override
    public void publish(String uri, String filter, boolean recursive, MetadataRepositoryType metadataRepositoryType) throws PublishingException {
        
        checkAuthorization(uri);
        this.publishingService.publish(uri, filter, recursive, metadataRepositoryType);

    }

    @Override
    public void unpublish(String uri, String filter, boolean recursive, MetadataRepositoryType metadataRepositoryType) throws PublishingException {
        
        checkAuthorization(uri);
        this.publishingService.unpublish(uri, filter, recursive, metadataRepositoryType);

    }

    @Override
    public void unpublish(List<String> ids) throws PublishingException {
        
        for (String id : ids) checkAuthorization(id);
        this.publishingService.unpublish(ids);

    }
    
    /**
     * Method to check that the user is authorized to execute a publishing/unpublishing operation.
     * @param uri
     * @throws Exception
     */
    private void checkAuthorization(String uri) throws PublishingException {
        
        if (authorizer!=null) {
            
            boolean authorized = false;
            String openid = "";
            final SecurityContext secCtx = SecurityContextHolder.getContext();
            final Authentication auth = secCtx.getAuthentication();
            if (LOG.isDebugEnabled()) LOG.debug("URL="+uri+" Security context authentication="+auth);

            if (auth!=null && auth instanceof PreAuthenticatedAuthenticationToken) {
                
                openid = auth.getName();
                if (LOG.isDebugEnabled()) LOG.debug("User is authenticated, openid="+openid);

                authorized = authorizer.authorize(openid, uri, Action.WRITE_ACTION);

            }
            
            // throw exception is user is not authorized
            if (!authorized) {
                String message = "User: "+openid+" is not authorized to publish/unpublish resource: "+uri;
                LOG.warn(message);
                throw new PublishingException(message);
            }
            
        }

    }

}
