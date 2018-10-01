package esg.search.publish.validation;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.springframework.util.StringUtils;

import esg.search.core.Record;
import esg.search.query.api.QueryParameters;
import esg.search.query.impl.solr.SolrXmlPars;
import esg.search.utils.XmlParser;
import esg.security.registry.service.api.ReloadableFileSetObserver;
import esg.security.registry.service.impl.ReloadableFileSet;

/**
 * Class that validates a record based on the supplied XML meta-instance.
 * 
 * @author Luca Cinquini
 *
 */
public class SchemaRecordValidator implements RecordValidator, ReloadableFileSetObserver {
    
    Set<Field> fields = new HashSet<Field>();
            
    // class that monitors the schema for changes
    private ReloadableFileSet watcher;
    
    // schema file path, stored only for debugging purposes
    private final String filepath;
    
    private final Log LOG = LogFactory.getLog(this.getClass());
    
    public SchemaRecordValidator(String filepath) throws Exception {
                
        // instantiate file watcher
        if (LOG.isInfoEnabled()) LOG.info("Schema location: "+filepath);
        watcher = new ReloadableFileSet(filepath);
        watcher.setObserver(this);
        this.filepath = filepath;
        
        // trigger first loading of configuration files
        watcher.reload();

    }
    
    
    /**
     * Method that parses the XML schema.
     */
    public void parse(List<File> files) {
        
        Set<Field> _fields = new HashSet<Field>();
        
        for (File file : files) {
            
            try {
                        
                if (LOG.isInfoEnabled()) LOG.info("Parsing XML schema: "+file.getAbsolutePath());
                
                // parse XML 
                XmlParser parser = new XmlParser(false); // validate XSD = false
                Document doc = parser.parseFile(file);
                Element root = doc.getRootElement();
                Namespace ns = root.getNamespace();
                
                for (Object obj : root.getChildren("field", ns)) {
                    Element el = (Element)obj;
                    
                    /*
                     *  <esgf:field name="model" minOccurs="1" maxOccurs="1">
                            <esgf:value>ACCESS1.0</esgf:value>
                            <esgf:value>CCSM4</esgf:value>
                        </esgf:field>
                     */            
                    Field field = new Field(el.getAttributeValue("name"));
                    
                    // minOccurs="1"
                    String minOccurs = el.getAttributeValue("minOccurs");
                    if (StringUtils.hasText(minOccurs)) {
                        field.minOccurs = Integer.parseInt(minOccurs);
                    }
                    
                    // maxOccurs="1"
                    String maxOccurs = el.getAttributeValue("maxOccurs");
                    if (StringUtils.hasText(maxOccurs)) {
                        if (maxOccurs.equalsIgnoreCase("unbounded")) {
                            field.maxOccurs = Integer.MAX_VALUE;
                        } else {
                            field.maxOccurs = Integer.parseInt(maxOccurs);
                        }
                    }
                    
                    // type="string"
                    String type = el.getAttributeValue("type");
                    if (StringUtils.hasText(type)) field.type = type;
                    
                    // minValue=Double.MIN_VALUE
                    String minValue = el.getAttributeValue("minValue");
                    if (StringUtils.hasText(minValue)) {
                        field.minValue = Double.parseDouble(minValue);
                    }
                    
                    // maxValue=Double.MAX_VALUE
                    String maxValue = el.getAttributeValue("maxValue");
                    if (StringUtils.hasText(maxValue)) {
                        field.maxValue = Double.parseDouble(maxValue);
                    }
                    
                    // record types
                    String recordType = el.getAttributeValue("recordType");
                    if (StringUtils.hasText(recordType)) {
                        for (String rct : recordType.split(",")) {
                            field.recordTypes.add(rct.trim());
                        }
                    }
                                
                    // values
                    for (Object _obj : el.getChildren("value", ns)) {
                        Element _el = (Element)_obj;
                        field.values.add(_el.getTextNormalize());
                    }
                    
                    
                    _fields.add(field);
                }
            
            } catch(IOException ioe) {
                LOG.warn(ioe.getMessage());
            } catch(JDOMException jde) {
                LOG.warn(jde.getMessage());
            } catch(NumberFormatException nfe) {
                LOG.warn(nfe.getMessage());
            }
        
        }
        
        synchronized(fields) {
            this.fields = _fields;
        }
        
    }

    @Override
    public void validate(Record record, List<String> errors) {
        
        if (LOG.isDebugEnabled()) LOG.debug("Validating record versus schema: "+this.filepath);
        
        // reload schema if needed
        watcher.reload();        
                        
        // retrieve record metadata
        Map<String,List<String>> recfields = record.getFields();
        
        // loop over schema categories
        for (Field field : fields) {
            
          if (field.recordTypes.isEmpty() || field.recordTypes.contains(record.getType())) {
            
            if (LOG.isTraceEnabled()) LOG.trace("Checking schema field name="+field.name);
            
            // special Record attribute "id"
            if (field.name.equals(QueryParameters.FIELD_ID)) {
                if (!StringUtils.hasText(record.getId())) errors.add("Missing record 'id'");
                
            // special Record attribute "version"
            } else if (field.name.equals(QueryParameters.FIELD_VERSION)) {
                // nothing to validate as version defaults to 0
                
            // all other Record attributes
            } else {
                       
                // record does not contain this field
                if (!recfields.containsKey(field.name)) {
                    if (field.minOccurs>0) errors.add("Missing field: '"+field.name+"'");
                    
                // record contains this field
                } else {
                    
                    List<String> values = recfields.get(field.name);
                    if (LOG.isTraceEnabled()) {
                        for (String value : values) {
                            LOG.trace("Field: "+field.name+" has value: "+value);
                        }
                    }
                    
                    // check number of values
                    if (values.size()<field.minOccurs || values.size()>field.maxOccurs) {
                        errors.add("Wrong number of values for field:'"+field.name+"'");
                    }
                    // check values match controlled vocabulary, if specified
                    if (!field.values.isEmpty()) {
                        for (String value : values) {
                            if (!field.values.contains(value)) {
                                errors.add("Value:'"+value+"' is not allowed for field:'"+field.name+"'");
                            }
                        }
                    }
                    
                    // url
                    if (field.name.equals(QueryParameters.FIELD_URL)) {
                        for (String value : values) {
                            String[] parts = value.split("\\|");
                            if (parts.length!=3) {
                                errors.add("Incorrect URL value:'"+value+"' (must have the form: 'url|mime type|service name'");
                            }
                        }
                    }
                    
                    // dates
                    if (field.type.equals("date")) {
                        for (String value : values) {
                            try {
                                SolrXmlPars.SOLR_DATE_TIME_FORMATTER.parse(value);
                            } catch(ParseException e) {
                                errors.add("Incorrect date-time format: "+value);
                            }
                        }
                        
                    // integers
                    } else if (field.type.equals("int")) {
                        for (String value : values) {
                            try {
                                int i = Integer.parseInt(value);
                                if (i<field.minValue) errors.add("Field: '"+field.name+"' must be >= "+(int)field.minValue);
                                if (i>field.maxValue) errors.add("Field: '"+field.name+"' must be <= "+(int)field.maxValue);
                            } catch(NumberFormatException e) {
                                errors.add("Incorrect integer value: "+value+" for field: '"+field.name+"'");
                            }
                        }
                        
                    // longs
                    } else if (field.type.equals("long")) {
                        for (String value : values) {
                            try {
                                long l = Long.parseLong(value);
                                if (l<field.minValue) errors.add("Field: '"+field.name+"' must be >= "+(long)field.minValue);
                                if (l>field.maxValue) errors.add("Field: '"+field.name+"' must be <= "+(long)field.maxValue);
                            } catch(NumberFormatException e) {
                                errors.add("Incorrect long value: "+value+" for field: '"+field.name+"'");
                            }
                        }
                        
                    // floats
                    } else if (field.type.equals("float")) {
                        for (String value : values) {
                            try {
                                float x = Float.parseFloat(value);
                                LOG.trace("FIELD MIN VALUE="+field.minValue);
                                if (x<field.minValue) errors.add("Field: '"+field.name+"' must be >= "+(float)field.minValue);
                                if (x>field.maxValue) errors.add("Field: '"+field.name+"' must be <= "+(float)field.maxValue);
                            } catch(NumberFormatException e) {
                                errors.add("Incorrect float value: "+value+" for field: '"+field.name+"'");
                            }
                        }
                    
                    // booleans
                    } else if (field.type.equals("boolean")) {
                        for (String value : values) {
                            if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false"))
                                errors.add("Incorrect boolean value: "+value+" for field: '"+field.name+"'");
                        }
                        
                    // UUIDs
                    } else if (field.type.equals("uuid")) {
                        for (String value : values) {
                            try {
                                UUID.fromString(value);
                            } catch(IllegalArgumentException e) {
                                errors.add("Incorrect UUID value: "+value+" for field: '"+field.name+"'");
                            }
                        }
                    }
                    
                } // record contains this field
                
            } // "id", "version" or anything else
            
          } // schema field definition applies to this record type
            
        } // loop over records
        
    }
    
    
    /**
     * Class holding data from a single <field> tag
     */
    private class Field {
        
        // Field attributes with default values
        String name = null;
        int minOccurs = 1;
        int maxOccurs = 1;
        String type = "string";
        double minValue =-Double.MAX_VALUE; // NOTE: NOT Double.MIN_VALUE
        double maxValue = Double.MAX_VALUE;
        Set<String> recordTypes = new HashSet<String>();
        
        Set<String> values = new HashSet<String>();
        
        public Field(String name) {
            this.name = name;
        }
        
    }

}
