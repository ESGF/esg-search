package esg.search.config;

import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Service;

@Service("csrfSecurityRequestMatcher")
public class CsrfSecurityRequestMatcher implements RequestMatcher {
    
	private Pattern allowedMethods = Pattern.compile("^(GET|HEAD|TRACE|OPTIONS)$");
	
	// null == match ALL HTTP methods
	// true == case insensitive matching
    private RequestMatcher hessianMatcher = new RegexRequestMatcher("/remote/secure/client-cert/hessian/.*", null, true); 
    private RequestMatcher wsMatcher = new RegexRequestMatcher("/ws/.*", null, true);
    private RequestMatcher wgetMatcher = new RegexRequestMatcher("/wget.*", null, true);
    private RequestMatcher orMatcher = new OrRequestMatcher(hessianMatcher, wsMatcher, wgetMatcher);
    
    @Override
    public boolean matches(HttpServletRequest request) {
    	
    	// exclude GET|HEAD|TRACE|OPTIONS from CSRF validation
        if (allowedMethods.matcher(request.getMethod()).matches()) {
            return false;
        }
 
        // exclude special URLs from CSRF validation
        // enforce CSRF validation on all others
        return !orMatcher.matches(request);
        
    }
}
