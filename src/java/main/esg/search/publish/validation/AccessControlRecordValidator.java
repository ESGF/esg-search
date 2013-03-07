package esg.search.publish.validation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;

import esg.common.util.ESGFProperties;
import esg.search.core.Record;
import esg.search.query.api.QueryParameters;
import esg.search.utils.XmlParser;

/**
 * Special {@link RecordValidator} that enforces restrictions
 * for the publishing Datasets into Projects.
 * 
 * @author Luca Cinquini
 *
 */
public class AccessControlRecordValidator implements RecordValidator {
    
    // location of configuration schema
    //private final String FILEPATH = "esg/search/config/esgf_project_restrictions.xml";
    private String filepath;
    
    private final Log LOG = LogFactory.getLog(this.getClass());
    
    private String hostname;
    
    // /esg/config/esgf.properties: esgf.host=....
    public final static String PROPERTIES_KEY = "esgf.host";
    
    // set containing restricted projects
    Set<String> projects = new HashSet<String>();
    
    // map containing entries of the form (project, index_node, pattern)
    private MultiKeyMap cache = new MultiKeyMap();

    public AccessControlRecordValidator(String filepath, ESGFProperties properties) throws Exception {
        
        // parse restrictions from XML file
        this.filepath = filepath;
        // FIXME: load only if file changes
        parseConfig();
        
        // load local hostname
        if (properties.containsKey(PROPERTIES_KEY)) {
            hostname = properties.get(PROPERTIES_KEY).toString().trim();   
        } else {
            hostname = java.net.InetAddress.getLocalHost().getHostName();
        }
        if (LOG.isInfoEnabled()) LOG.info("Using local hostname="+hostname);
        
    }

    @Override
    public void validate(Record record, List<String> errors) throws Exception {
        
        String type = record.getType();
        String id = record.getId();
        
        if (type.equals(QueryParameters.TYPE_DATASET)) {
            
            if (LOG.isInfoEnabled()) LOG.info("Enforcing access control for publication of dataset with id="+id);
            
            String project = record.getFieldValue(QueryParameters.PROJECT);
            if (StringUtils.hasText(project)) {
                
                if (projects.contains(project)) {
                    
                    boolean valid = false;
                    
                    // if project is found in XML file, it is by default restricted...
                    if (cache.containsKey(project, this.hostname)) {
                        
                        List<Pattern> patterns = (List<Pattern>)cache.get(project, hostname);
                        for (Pattern pattern : patterns) {
                            // unless it matches one of the regular expressions for the local hostname
                            Matcher matcher = pattern.matcher(id);
                            if (matcher.matches()) {                 
                                valid = true;
                                break;
                            }
                        }
                        
                    }
                    
                    if (!valid) {
                        errors.add("This index node '"+this.hostname+"' is not authorized to publish the requested dataset '"+id+"' into project: '"+project+"'");
                    }
                    
                } // XML config contains this project
                
            } // dataset has 'project' field
            
        } // record is a dataset
        
    }
    
    /**
     * Method to load the publication restrictions from the XML configuration file.
     */
    private void parseConfig() throws IOException, JDOMException {
        
        File file = new ClassPathResource(filepath).getFile();
        if (LOG.isInfoEnabled()) LOG.info("Parsing XML config file: "+file.getAbsolutePath());
        
        // parse XML 
        XmlParser parser = new XmlParser(false); // validate XSD = false
        Document doc = parser.parseFile(file);
        Element root = doc.getRootElement();
        Namespace ns = root.getNamespace();
        
        /*
         * <esgf:project name="obs4MIPs">
                <esgf:index_node hostname="localhost">
                    <esgf:datasets pattern=".*"/>
                </esgf:index_node>
           </esgf:project>
         */          
        for (Object obj1 : root.getChildren("project", ns)) {
            Element el1 = (Element)obj1;
            String project = el1.getAttributeValue("name");
            projects.add(project);
                        
            for (Object obj2 : el1.getChildren("index_node", ns)) {
                Element el2 = (Element)obj2;
                String indexNode = el2.getAttributeValue("hostname");
                
                List<Pattern> patterns = new ArrayList<Pattern>();
                for (Object obj3 : el2.getChildren("datasets", ns)) {
                    Element el3 = (Element)obj3;
                    
                    String pattern = el3.getAttributeValue("pattern");
                    if (LOG.isInfoEnabled()) LOG.info("Restriction: project="+project+" index_node="+indexNode+" pattern="+pattern);
                    patterns.add(Pattern.compile(pattern));
                    
                }               
                cache.put(project, indexNode, patterns);
                
            }
            
        }
        
    }

}
