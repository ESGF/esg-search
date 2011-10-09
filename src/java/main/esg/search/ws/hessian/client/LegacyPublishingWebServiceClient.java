package esg.search.ws.hessian.client;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import esg.search.publish.api.LegacyPublishingService;
import esg.search.publish.impl.LegacyPublishingServiceMain;
import esg.search.utils.CertUtils;


/**
 * Example client to a remote Legacy Publishing Service.
 * This client relies on Spring client support for the Hessian protocol.
 * 
 * @author luca.cinquini
 *
 */
public class LegacyPublishingWebServiceClient extends LegacyPublishingServiceMain {
		
	public static void main(final String[] args) throws Exception {
		
        final ApplicationContext ctxt = new ClassPathXmlApplicationContext("classpath:esg/search/ws/hessian/client/ws-hessian-client-config.xml");
        final LegacyPublishingService publishingService = (LegacyPublishingService)ctxt.getBean("legacyPublishingWebServiceProxy");
                	    
 	    // setup client certificate and trustore for mutual authentication
        CertUtils.setKeystore("esg/search/ws/hessian/client/client-cert.ks");
	    CertUtils.setTruststore("esg/search/ws/hessian/client/localhost-client-trustore.ks");
        //CertUtils.setTruststore("esg/search/ws/hessian/client/esg-truststore.ts");
	    
	    //CertUtils.setTruststore("esg/security/resources/esg-truststore-openid.ts");
	    
	    
        final LegacyPublishingWebServiceClient self = new LegacyPublishingWebServiceClient();
        self.run(publishingService, args);
		
	}
	

}
