package esg.search.publish.thredds;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import esg.search.core.Record;
import esg.search.publish.api.MetadataEnhancer;
import esg.search.query.impl.solr.SolrXmlPars;

/**
 * Class to infer the "experiment_family" category from the "experiment" value.
 * Note that the processing is case-independent.
 * 
 * @author Luca Cinquini
 *
 */
public class ExperimentMetadataEnhancer implements MetadataEnhancer {
    
    final static String KEYIN = ThreddsPars.EXPERIMENT;
    final static String KEYOUT = SolrXmlPars.FIELD_EXPERIMENT_FAMILY;
    
    final static String FAMILY_ALL = "All";
    final static String FAMILY_DECADAL = "Decadal";
    final static String FAMILY_ESM = "ESM";
    final static String FAMILY_HISTORICAL = "Historical";
    final static String FAMILY_PALEO = "Paleo";
    final static String FAMILY_CONTROL = "Control";
    final static String FAMILY_RCP = "RCP";
    final static String FAMILY_ATMOS_ONLY = "Atmos-only";
    final static String FAMILY_IDEALIZED = "Idealized";
    
    final static Map<Pattern, String> patterns = new HashMap<Pattern, String>();
    
    static {
        
        // Decadal
        patterns.put(Pattern.compile(new String("^decadal.*").toLowerCase()), FAMILY_DECADAL);
        patterns.put(Pattern.compile(new String("^noVolc.*").toLowerCase()), FAMILY_DECADAL);
        patterns.put(Pattern.compile(new String("^volcIn.*").toLowerCase()), FAMILY_DECADAL);
        
        // ESM
        patterns.put(Pattern.compile(new String("^esm.*").toLowerCase()), FAMILY_ESM);
        
        // Historical
        patterns.put(Pattern.compile(new String(".*historical.*").toLowerCase()), FAMILY_HISTORICAL);
        
        // Paleo
        patterns.put(Pattern.compile(new String("^midHolocene$").toLowerCase()), FAMILY_PALEO);
        patterns.put(Pattern.compile(new String("^lgm$").toLowerCase()), FAMILY_PALEO);
        patterns.put(Pattern.compile(new String("^past1000$").toLowerCase()), FAMILY_PALEO);
        
        // Control
        patterns.put(Pattern.compile(new String("^sstClim$").toLowerCase()), FAMILY_CONTROL);
        patterns.put(Pattern.compile(new String(".*control.*").toLowerCase()), FAMILY_CONTROL);
        
        // RCP
        patterns.put(Pattern.compile(new String(".*rcp.*").toLowerCase()), FAMILY_RCP);
        
        // Idealized
        patterns.put(Pattern.compile(new String("^1pctCO2$").toLowerCase()), FAMILY_IDEALIZED);
        patterns.put(Pattern.compile(new String("^abrupt4xCO2$").toLowerCase()), FAMILY_IDEALIZED);
        
        // Atmos-only
        patterns.put(Pattern.compile(new String("^amip.*").toLowerCase()), FAMILY_ATMOS_ONLY);
        patterns.put(Pattern.compile(new String("^aqua.*").toLowerCase()), FAMILY_ATMOS_ONLY);
        patterns.put(Pattern.compile(new String("^sst.*").toLowerCase()), FAMILY_ATMOS_ONLY);
        
    }
        
    public ExperimentMetadataEnhancer() {}

    /**
     *  ALL = all individual experiments, as in the current scheme (about 80 items in all)
        DECADAL = decadal1960, decadal1961, decadal1962m, ... decadal2008, noVolc1960, noVolc1975, noVolc1980, noVolc1990, and volcIn2010 (about 50 items in all)
        ESM = esmControl, esmFdbk1, esmFdbk2, esmFixClim1, esmFixClim2, esmHistorical (6 items)
        HISTORICAL = historical, historicalExt, historicalGHG, historicalMisc, historicalNat, esmHistorical (6 items)
        PALEO = midHolocene, lgm, past1000 (3 items)
        CONTROL = piControl, esmControl, aquaControl, sstClim (4 items)
        RCP = rcp26, rcp45, rcp60, rcp85, esmrcp85 (5 items)
        IDEALIZED = 1pctCO2, abrupt4xCO2 (2 items)
        ATMOS-ONLY = amip, amip4K, amip4xCO2, amipFuture, aqua4K, aqua4xCO2, aquaControl, sst2030, sstClim, sstClim4xCO2, sstClimSulfate, sstClimAerosol (12 items)
     */
    @Override
    public void enhance(String name, String value, Record record) {
                
        // only process "experiment"
        if (name.equals(KEYIN)) {
            record.addField(KEYOUT, FAMILY_ALL);
            
            for (final Pattern pattern : patterns.keySet()) {
                final Matcher matcher = pattern.matcher(value.toLowerCase()); 
                if (matcher.matches()) {
                    record.addField(KEYOUT, patterns.get(pattern));
                }
            }      
        }
                
    }

}
