package esg.search.ws.hessian.client;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import esg.search.publish.api.PublishingService;
import esg.search.publish.impl.PublishingServiceMain;
import esg.search.utils.CertUtils;


/**
 * Example client for a metadata harvesting web service.
 * This client relies on Spring client support for the Hessian protocol.
 * @author luca.cinquini
 *
 */
public class PublishingWebServiceClient extends PublishingServiceMain {
		
	public static void main(final String[] args) throws Exception {
		
        final ApplicationContext ctxt = new ClassPathXmlApplicationContext("classpath:esg/search/ws/hessian/client/ws-hessian-client-config.xml");
        final PublishingService publishingService = (PublishingService)ctxt.getBean("publishingWebServiceProxy");
	    
 	    // setup client certificate and trustore for mutual authentication
        CertUtils.setKeystore("esg/search/ws/hessian/client/client-cert.ks");
	    CertUtils.setTruststore("esg/search/ws/hessian/client/localhost-client-trustore.ks");
	    //CertUtils.setTruststore("esg/security/resources/esg-truststore-openid.ts");

	    final PublishingWebServiceClient self = new PublishingWebServiceClient();
	    self.run(publishingService, args);
		
	}
	

}
