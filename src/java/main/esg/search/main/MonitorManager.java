package esg.search.main;

import java.util.ArrayList;
import java.util.List;

/**
 * Main class to benchmark a set of Solr servers.
 * 
 * @author Luca Cinquini
 *
 */
public class MonitorManager {
    
    // the servers to probe
    private final static String[] SERVERS = new String[]{
                                                         "adm07.cmcc.it",
                                                         "dev.esg.anl.gov",
                                                         "esg-datanode.jpl.nasa.gov",
                                                         //"esg.ccs.ornl.gov",
                                                         "esg1-gw.pnl.gov",
                                                         "pcmdi11.llnl.gov",
                                                         "pcmdi9.llnl.gov",
                                                         "test-datanode.jpl.nasa.gov"
                                                         };
    
    // query parameters
    public final static String SOLR_QUERY = "q=*&replica=false&latest=true";
    public final static String API_QUERY = "query=*&replica=false&latest=true&distrib=false";
    public final static String XPATH1 = "/response/lst[@name='responseHeader']/int[@name='QTime']";
    public final static String XPATH2 = "/response/result";
    private final static String[] CORES = new String[]{"datasets","files"};
    
    public final static int CONNECTION_TIMEOUT = 1000;
    public final static int READ_TIMEOUT = 5000;
    
    // the specific Solr core to probe
    private String core;
    
    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
               
        for (String core : CORES) {
            MonitorManager self = new MonitorManager(core);
            self.runSequentially();  
            self.runInParallel();      
        }

    }
    
    public MonitorManager(String core) {
        this.core = core;
    }
    
    /**
     * Method to monitor all servers sequentially.
     * @throws Exception
     */
    public void runSequentially() throws Exception {
        
        System.out.println("\nCore="+core+" Sequential Execution");
        
        List<MonitorThread> threads = new ArrayList<MonitorThread>();
        long startTime = System.currentTimeMillis();
        for (String server : SERVERS) {
            MonitorThread mt = new MonitorThread(server, core);
            mt.run();
            threads.add(mt);
        }
                
        // print total elapsed time
        long elapsedTime = System.currentTimeMillis() - startTime;        
        print(threads);
        System.out.println("Core="+core+" Sequential Execution Total Elapsed Time="+elapsedTime+" millis");
        
    }
        
    /**
     * Method to monitor each server in a separate thread.
     * @throws Exception
     */
    public void runInParallel() throws Exception {
        
        System.out.println("\nCore="+core+" Parallel Execution");
        
        // start all threads
        List<MonitorThread> threads = new ArrayList<MonitorThread>();
        long startTime = System.currentTimeMillis();
        for (String server : SERVERS) {
            MonitorThread mt = new MonitorThread(server, core);
            mt.start();
            threads.add(mt);
        }
        
        // wait for all threads to finish
        for (Thread thread : threads) {
            thread.join();
        }
        
        // print total elapsed time
        long elapsedTime = System.currentTimeMillis() - startTime;
        print(threads);
        System.out.println("Core="+core+" Parallel Execution Total Elapsed Time="+elapsedTime+" millis");        
        
    }
    
    public final static String buildUrl(String server, String core) {
        
        // query Solr directly
        return "http://"+server+":8983/solr/"+core+"/select/?"+SOLR_QUERY;
        
        // query the search web service
        /*
        if (core.equals("datasets")) {
            return "http://"+server+"/esg-search/search?"+API_QUERY+"&type=Dataset";
        } else {
            return "http://"+server+"/esg-search/search?"+API_QUERY+"&type=File";
        }*/
    }
    
    /**
     * Method to print benchmarking stats for all threads.
     * @param threads
     */
    private void print(List<MonitorThread> threads) {
        for (MonitorThread mt : threads) {
            System.out.println("Server="+mt.server+" Core="+core+" Query Time="+mt.queryTime+" Elapsed Time="+mt.elapsedTime+" Number of Results="+mt.numFound);
        }
    }
    

}
