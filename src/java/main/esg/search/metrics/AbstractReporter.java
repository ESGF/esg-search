package esg.search.metrics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;

import esg.search.utils.Serializer;

/**
 * Abstract superclass of node metrics reporters.
 * This class contains most of the functionality needed to report metrics.
 * 
 * @author Luca Cinquini
 *
 */
public abstract class AbstractReporter implements Reporter {
    
    // parameters
    protected final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss Z");
    protected final static Namespace NAMESPACE_ESGF = Namespace.getNamespace("esgf","http://www.esgf.org/");
    protected final static String NEWLINE = System.getProperty("line.separator");
    private static String REPORTER_COMMAND = "hostname";
    private static String SEPARATOR = "|";
    
    // the agent collecting the metrics
    private String reporter;
    
    // the date when the metrics were collected
    private Date date;
    
    static {
        // use GMT to merge metrics across nodes
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    // global ordered list of dynamic keys
    Set<String> keys = new TreeSet<String>();

    // map of records ordered by time
    Map<Date, Map<String, Integer>> records = new LinkedHashMap<Date, Map<String, Integer>>();
    
    /**
     * Method that drives the collection of metrics.
     * 
     * @throws Exception
     */
    public void run() throws Exception {
                    
        // metrics parameters
        this.date = new Date();
        this.reporter = getReporter();
        
        // read current CSV
        read_csv();
        
        // collect metrics
        Map<String, Integer> map = this.report();
                
        // store this record
        records.put(this.date, map);
        
        // update global keys with new values ?
        for (String key : map.keySet()) {
            this.keys.add(key);
        }
        
        // write out CSV file
        write_csv();
        
        // write out newest record
        write_xml(this.date, this.reporter, this.getReportType(), map);
        
    }

    /**
     * Method to run a system command and return the output as a list of lines.
     * 
     * @param command
     * @return
     */
    protected List<String> runSystemCommand(final String command) {
        
        List<String> output = new ArrayList<String>();

        try {

            // run the command
            //System.out.println("Running command: "+command);
            Process p = Runtime.getRuntime().exec(command);
            
            // read standard input
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

            // read standard error
            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

            // read the output from the command
            String s = null;
            while ((s = stdInput.readLine()) != null) {
                output.add(s);
            }
            
            // read any errors from the attempted command
            while ((s = stdError.readLine()) != null) {
                System.out.println("Error detected: "+s);
                System.exit(-1);
            }
                        
        } catch (IOException e) {
            System.out.println("Error running command: "+command);
            e.printStackTrace();
            System.exit(-1);
        }
        
        return output;

    }
    
    /**
     * Method to return the name of the agent reporting the metrics.
     * 
     * @return
     * @throws Exception
     */
    protected String getReporter() throws Exception {
        
        List<String> output = runSystemCommand(REPORTER_COMMAND);
        if (output.size()==1) {
            return output.get(0);
        } else {
            return "";
        }
        
    }
    
    /**
     * Method to write the latest metrics to the XML file.
     * 
     * @param path
     * @param date
     * @param hostname
     * @param type
     * @param data
     * @throws Exception
     */
    protected void write_xml(Date date, String reporter, String type, Map<String, Integer> data) throws Exception {
        
        // create directory if not existing already
        File f = new File(this.getXmlFilePath());
        if (!f.getParentFile().exists()) f.getParentFile().mkdirs();
        
        // root element
        final Element root = new Element("data", NAMESPACE_ESGF);
        root.setAttribute("time", DATE_FORMAT.format(date));
        root.setAttribute("reporter", reporter);
        root.setAttribute("type", type);
        
        // data elements
        for (String key : data.keySet()) {
            Element datum = new Element("datum");
            // split key into mandatory "key" and optional "type" attributes
            int i = key.indexOf("=");
            if (i>0) {
                datum.setAttribute("type", key.substring(0,i));
                datum.setAttribute("key", key.substring(i+1));
            } else {
                datum.setAttribute("key", key);
            }
            datum.setAttribute("value", Integer.toString(data.get(key)));
            root.addContent(datum);
        }
               
        // write out to file
        Serializer.JDOMtoFile(new Document(root), this.getXmlFilePath());        
        
    }
    
    /**
     * Method to read the current data from the CVS file.
     * 
     * @throws IOException
     * @throws ParseException
     */
    protected void read_csv() throws IOException, ParseException {
        
        File f = new File(this.getCsvFilePath());
        if (f.exists()) {
            
            BufferedReader input = new BufferedReader(new FileReader(f));
            String line = null;
            boolean first = true;
            List<String> mykeys = new ArrayList<String>(); // list of keys in the order they are stored in the file
            while ((line = input.readLine()) != null) {
                
                String[] parts = line.split("\\"+SEPARATOR);
                
                // read header
                if (first) {
                    for (int i=1; i<parts.length; i++) {
                        String peer = parts[i].trim();
                        keys.add(peer);
                        mykeys.add(peer);
                    }
                    first = false;
                    
                // read other lines
                } else {
                    Map<String, Integer> map = new HashMap<String, Integer>();
                    Date date = DATE_FORMAT.parse(parts[0]);
                    for (int i=1; i<parts.length; i++) {
                        map.put(mykeys.get(i-1), Integer.parseInt(parts[i].trim()));
                    }
                    records.put(date, map);
                }
                
            }        
            input.close();
        }
        
    }
    
    /**
     * Method to write all data to the CSV file.
     * 
     * @throws IOException
     */
    protected void write_csv() throws IOException {
        
        
        File f1 = new File(this.getCsvFilePath()+".tmp");
        
        // create parent directory directory if not existing already
        if (!f1.getParentFile().exists()) f1.getParentFile().mkdirs();
        
        FileWriter fw = new FileWriter(f1, false); // append=false
        
        // header line
        fw.write("Time"+SEPARATOR+StringUtils.join(keys, SEPARATOR) + NEWLINE);
        
        // record lines
        for (Date date : records.keySet()) {
            fw.write(DATE_FORMAT.format(date));
            Map<String, Integer> map = records.get(date);
            // loop over global peers
            for (String peer : keys) {
                if (map.containsKey(peer)) {
                    fw.write(SEPARATOR+map.get(peer));
                } else {
                    fw.write(SEPARATOR+"0");
                }
            }
            fw.write(NEWLINE);
        }

        fw.flush();
        fw.close();
        
        // rename files
        File f2 = new File(this.getCsvFilePath());
        f1.renameTo(f2);
    }


}
