package esg.search.query.impl.solr;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Utility class to check a collection of Solr shards.
 * 
 * @author Luca Cinquini
 */
public class ShardMonitor {
    
    public static LinkedHashSet<String> monitor(final LinkedHashSet<String> shards, String query) {
        
        // probe each shard in a separate thread
        List<ShardProbe> probes = new ArrayList<ShardProbe>();
        for (final String shard : shards) {

            final ShardProbe probe = new ShardProbe(shard, query);
            probe.start();
            probes.add(probe);

        }
        
        // wait for all threads to finish
        for (Thread probe : probes) {
            try {
                probe.join();
            } catch(InterruptedException e) {}
        }

        // inspect all probes
        LinkedHashSet<String> _shards = new LinkedHashSet<String>();
        for (ShardProbe probe : probes) {
            if (probe.getNumFound()>=0) _shards.add(probe.getShard());
        }
        
        return _shards;
        
    }

}
