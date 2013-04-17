package esg.search.publish.opendap;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Properties;

import esg.search.core.Record;
import esg.search.core.RecordSerializer;
import esg.search.publish.impl.solr.SolrRecordSerializer;
import esg.search.publish.plugins.AllPropertiesMetadataEnhancer;

public class OpendapMain {
    
    //private static String[] configLocations = new String[] { "classpath:esg/search/config/client-application-context.xml" };

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        
        if (args.length!=1) {
            System.out.println("USAGE: java -Djava.ext.dirs=<path to jars> esg.search.publish.opendap.OpendapMain <opendap url>");
            System.exit(-1);
        }
        String url = args[0];
        
        // load fixed attributes from properties file
        Properties props = new Properties();
        try {
          props.load(new FileInputStream("/esg/config/opendap.properties"));
        } catch (IOException e) {
          e.printStackTrace();
          System.exit(-1);
        }
        
        // create ESGF record from OpenDAP URL
        OpendapParserStrategy parser = new Cmip5OpendapParserStrategyImpl(new AllPropertiesMetadataEnhancer(props));
        URI schema = new URI("cmip5");
        Record record = parser.parse(url, schema).get(0);
        
        // serialize record
        RecordSerializer serializer = new SolrRecordSerializer();
        String xml = serializer.serialize(record, true);
        System.out.println(xml);
        
        /* validate record
        final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(configLocations);
        final RecordValidator validator = (RecordValidator)context.getBean("recordValidatorManager");
        List<String> errors = new ArrayList<String>();
        validator.validate(record, errors);
        if (errors.size()==0) {
            System.out.println("Record IS valid");
        } else {
            System.out.println("Record is NOT valid:");
            for (String error : errors) {
                System.out.println(error);
            }
        }*/

    }

}
