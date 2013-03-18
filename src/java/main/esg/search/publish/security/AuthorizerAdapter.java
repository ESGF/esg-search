package esg.search.publish.security;

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
import esg.search.publish.api.PublishingException;

/**
 * Class that provides authorization for publishing operations
 * by retrieving the user OpenID from the security context
 * and invoking the deployed {@link Authorizer} service.
 * 
 * @author Luca Cinquini
 *
 */
@Service("authorizerAdapter")
public class AuthorizerAdapter {
    
    /**
     * Collaborator used to secure publishing calls. 
     * If none is configured, no authorization will take place.
     */
    private Authorizer authorizer = null;
    
    private final Log LOG = LogFactory.getLog(this.getClass());
    
    // Note: comment out @Autowired to disable security
    //@Autowired
    public void setAuthorizer(Authorizer authorizer) {
        this.authorizer = authorizer;
    }
    
    /**
     * Method to check that the user is authorized to execute a publishing/unpublishing operation.
     * 
     * @param uri : the resource to be authorized
     * @throws Exception
     */
    public void checkAuthorization(String uri) throws PublishingException {
        
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
            
            // throw exception if user is not authorized
            if (!authorized) {
                String message = "User: "+openid+" is not authorized to publish/unpublish resource: "+uri;
                LOG.warn(message);
                throw new SecurityException(message);
            }
            
        }

    }

}
