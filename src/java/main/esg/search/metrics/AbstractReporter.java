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
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * Abstract superclass or node metrics reporters.
 * 
 * @author Luca Cinquini
 *
 */
public abstract class AbstractReporter implements Reporter {
    
    private static String COMMAND = "hostname";
    
    protected final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss Z");
    protected final static Namespace NAMESPACE_ESGF = Namespace.getNamespace("esgf","http://www.esgf.org/");
    protected final static String NEWLINE = System.getProperty("line.separator");
    
    protected String reporter;
    protected Date date;
    
    static {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    // global ordered list of keys
    Set<String> keys = new TreeSet<String>();

    // map of records ordered by time
    Map<Date, Map<String, Integer>> records = new LinkedHashMap<Date, Map<String, Integer>>();
    
    /**
     * Driver method
     * @throws Exception
     */
    public void run(String csvFile, String xmlFile, String type) throws Exception {
               
        // read current CSV
        read_csv(csvFile);
        
        // metrics parameters
        this.date = new Date();
        this.reporter = getHostName();
        
        // collect metrics
        Map<String, Integer> map = this.report();
                
        // store this record
        records.put(this.date, map);
        
        // update global keys
        for (String key : map.keySet()) {
            this.keys.add(key);
        }
        
        // write out CSV file
        write_csv(csvFile);
        
        // write out newest record
        write_xml(xmlFile, this.date, this.reporter, type, map);
        
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
            System.out.println("Running command: "+command);
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
     * Method to return the host name where the program is running.
     * @return
     * @throws Exception
     */
    protected String getHostName() throws Exception {
        
        List<String> output = runSystemCommand(COMMAND);
        if (output.size()==1) {
            return output.get(0);
        } else {
            return "";
        }
        
    }
    
    protected void print(List<String> output) {
        for (String s : output) {
            System.out.println(s);
        }
    }
    
    protected void write_xml(String path, Date date, String hostname, String type, Map<String, Integer> data) throws Exception {
        
        // root element
        final Element root = new Element("data", NAMESPACE_ESGF);
        root.setAttribute("time", DATE_FORMAT.format(date));
        root.setAttribute("reporter", hostname);
        root.setAttribute("type", type);
        
        // data elements
        for (String key : data.keySet()) {
            Element datum = new Element("datum");
            datum.setAttribute("key", key);
            datum.setAttribute("value", Integer.toString(data.get(key)));
            root.addContent(datum);
        }
               
        // write out to file
        File file = new File(path);
        XMLOutputter outputter = getXMLOutputter();
        FileWriter writer = new FileWriter(file);
        outputter.output(new Document(root), writer);
        writer.close();
        
        
    }
    
    protected void read_csv(final String filepath) throws IOException, ParseException {
        
        File f = new File(filepath);
        if (f.exists()) {
            
            BufferedReader input = new BufferedReader(new FileReader(f));
            String line = null;
            boolean first = true;
            List<String> _lkeys = new ArrayList<String>(); // list of keys in the order they are stored in the file
            while ((line = input.readLine()) != null) {
                
                String[] parts = line.split(",");
                
                // read header
                if (first) {
                    for (int i=1; i<parts.length; i++) {
                        String peer = parts[i].trim();
                        keys.add(peer);
                        _lkeys.add(peer);
                    }
                    first = false;
                    
                // read other lines
                } else {
                    Map<String, Integer> map = new HashMap<String, Integer>();
                    Date date = DATE_FORMAT.parse(parts[0]);
                    for (int i=1; i<parts.length; i++) {
                        map.put(_lkeys.get(i-1), Integer.parseInt(parts[i].trim()));
                    }
                    records.put(date, map);
                }
                
            }        
            input.close();
        }
        
    }
    
    protected void write_csv(final String filepath) throws IOException {
        
        FileWriter fw = new FileWriter(filepath+".tmp", false); // append=false
        
        // header line
        fw.write("Time, "+StringUtils.join(keys, ", ") + NEWLINE);
        
        // record lines
        for (Date date : records.keySet()) {
            fw.write(DATE_FORMAT.format(date));
            Map<String, Integer> map = records.get(date);
            // loop over global peers
            for (String peer : keys) {
                if (map.containsKey(peer)) {
                    fw.write(", "+map.get(peer));
                } else {
                    fw.write(", 0");
                }
            }
            fw.write(NEWLINE);
        }

        fw.flush();
        fw.close();
        
        // rename files
        File f1 = new File(filepath+".tmp");
        File f2 = new File(filepath);
        f1.renameTo(f2);
    }
    
    private static org.jdom.output.XMLOutputter getXMLOutputter() {
        
        Format format = Format.getPrettyFormat();
        format.setLineSeparator(NEWLINE);
        org.jdom.output.XMLOutputter outputter = new org.jdom.output.XMLOutputter(format);
        return outputter;
        
    }

}
