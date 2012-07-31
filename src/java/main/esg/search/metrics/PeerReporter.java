package esg.search.metrics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PeerReporter extends AbstractReporter {
    
    //private final static String COMMAND = "/usr/local/bin/esgf-spotcheck localhost";
    private final static String COMMAND = "cat /Users/cinquini/myApplications/spotcheck.txt";
    
    private final static String CSV_FILE_PATH = "/esg/content/metrics_spotcheck.csv";
    private final static String XML_FILE_PATH = "/esg/content/metrics_spotcheck.xml";
    private final static String REPORT_TYPE = "Peer Nodes";
    
    // server where this program is run
    String reporter = null;
    
    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        
        PeerReporter self = new PeerReporter();
        self.run();

    }

    @Override
    public Map<String, Integer> report() throws Exception {
                       
        // run spot-check
        List<String> output = runSystemCommand(COMMAND);        
        
        // parse output
        Map<String, Integer> map = new HashMap<String, Integer>();
        String localhost = null;
        for (String line : output) {
            
            // Spot Checking [pcmdi9.llnl.gov]...
            if (line.indexOf("Spot Checking")>=0) {
                localhost = line.substring(line.indexOf("[")+1, line.indexOf("]"));
                
            // [1] looking at site http://adm07.cmcc.it/esgf-node-manager/registration.xml -> 1
            } else if (line.indexOf("looking at site http")>=0) {
                
                // parse spot-check line
                String s = line.substring(line.indexOf("http"));
                String[] ss = s.split("\\s+");
                String[] sss = ss[0].split("\\/");
                //System.out.println(sss[2]+" -> "+ss[2]);
                String peer = sss[2];
                int nPeers = Integer.parseInt(ss[2]);
                
                // store information
                map.put(peer, new Integer(nPeers));
                
            }
        }
                        
        return map;
        
    }

    @Override
    public String getCsvFilePath() {
        return CSV_FILE_PATH;
    }

    @Override
    public String getXmlFilePath() {
        return XML_FILE_PATH;
    }

    @Override
    public String getReportType() {
        return REPORT_TYPE;
    }

    
    
}
