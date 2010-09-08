package esg.search.harvest.impl;

import org.springframework.context.support.ClassPathXmlApplicationContext;


/**
 * Main class to run the {@link RecordGenerator} with a given subscribed record consumer.
 */
public class RecordGeneratorMain {
	
	
    private static String[] configLocations = new String[] { "classpath:esg/search/config/harvest-context.xml" };
	
	public static void main(String[] args) throws Exception {
		
	    final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(configLocations);
	    final RecordGenerator recordGenerator = (RecordGenerator)context.getBean("recordGenerator");
	    recordGenerator.generate();
		
	}
	
}
