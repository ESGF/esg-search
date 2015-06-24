package esg.search.config;

import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Service;

@Service("csrfSecurityRequestMatcher")
public class CsrfSecurityRequestMatcher implements RequestMatcher {
    
	private Pattern allowedMethods = Pattern.compile("^(GET|HEAD|TRACE|OPTIONS)$");
	
	// null == match ALL HTTP methods
	// true == case insensitive matching
    private RegexRequestMatcher unprotectedMatcher 
    	= new RegexRequestMatcher("/remote/secure/client-cert/hessian/.*", null, true); 
 
    @Override
    public boolean matches(HttpServletRequest request) {
    	
    	// exclude GET|HEAD|TRACE|OPTIONS from CSRF validation
        if (allowedMethods.matcher(request.getMethod()).matches()) {
            return false;
        }
 
        // exclude special URLs from CSRF validation
        // enforce CSRF validation on all others
        return !unprotectedMatcher.matches(request);
        
    }
}
