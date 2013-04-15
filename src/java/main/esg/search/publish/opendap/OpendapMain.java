package esg.search.publish.opendap;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import esg.search.core.Record;
import esg.search.core.RecordSerializer;
import esg.search.publish.impl.solr.SolrRecordSerializer;
import esg.search.publish.validation.RecordValidator;
import esg.search.query.api.QueryParameters;

public class OpendapMain {
    
    private static String[] configLocations = new String[] { "classpath:esg/search/config/client-application-context.xml" };

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        
        if (args.length!=1) {
            System.out.println("USAGE: java -Djava.ext.dirs=<path to jars> esg.search.publish.opendap.OpendapMain <opendap url>");
            System.exit(-1);
        }
        String url = args[0];
        
        // Fixed properties
        Properties props = new Properties();
        //props.setProperty("time_frequency", "mon");
        props.setProperty(QueryParameters.FIELD_INDEX_NODE, "localhost"); // FIXME ?
        
        // create ESGF record from OpenDAP URL
        //String url = "http://esg-datanode.jpl.nasa.gov/thredds/dodsC/obs4MIPs.NASA-JPL.AIRS.mon.hus.1.aggregation.1";
        //String url = "http://esg-datanode.jpl.nasa.gov/thredds/dodsC/esg_dataroot/obs4MIPs/observations/atmos/husNobs/mon/grid/NASA-JPL/AIRS/v20110608/husNobs_AIRS_L3_RetStd-v5_200209-201105.nc";
        //OpendapParser parser = new DefaultOpendapParser(props);
        OpendapParser parser = new Cmip5OpendapParser(props);
        Record record = parser.parse(url, true).get(0);
        
        // serialize record
        RecordSerializer serializer = new SolrRecordSerializer();
        String xml = serializer.serialize(record, true);
        System.out.println(xml);
        
        // validate record
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
        }
        

    }

}
