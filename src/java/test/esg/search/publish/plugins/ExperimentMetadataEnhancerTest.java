package esg.search.publish.plugins;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.util.Assert;

import esg.search.core.Record;
import esg.search.core.RecordImpl;
import esg.search.publish.plugins.ExperimentMetadataEnhancer;
import esg.search.publish.thredds.ThreddsPars;
import esg.search.query.impl.solr.SolrXmlPars;

/**
 * Test class for {@link ExperimentMetadataEnhancer}.
 * @author Luca Cinquini
 *
 */
public class ExperimentMetadataEnhancerTest {
    
    ExperimentMetadataEnhancer eme;
    
    @Before
    public void setup() {
        eme = new ExperimentMetadataEnhancer("");
    }
    
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
    @Test
    public void testPatterns() {
        
        check("decadal1961", ExperimentMetadataEnhancer.FAMILY_DECADAL);
        check("noVolc1975", ExperimentMetadataEnhancer.FAMILY_DECADAL);
        check("volcIn2010", ExperimentMetadataEnhancer.FAMILY_DECADAL);
        
        check("esmFixClim1", ExperimentMetadataEnhancer.FAMILY_ESM);
        check("esmHistorical", ExperimentMetadataEnhancer.FAMILY_ESM);
        check("esmControl", ExperimentMetadataEnhancer.FAMILY_ESM);
        
        check("historical", ExperimentMetadataEnhancer.FAMILY_HISTORICAL);
        check("historicalGHG", ExperimentMetadataEnhancer.FAMILY_HISTORICAL);
        check("esmHistorical", ExperimentMetadataEnhancer.FAMILY_HISTORICAL);
        
        check("midHolocene", ExperimentMetadataEnhancer.FAMILY_PALEO);
        check("lgm", ExperimentMetadataEnhancer.FAMILY_PALEO);
        check("past1000", ExperimentMetadataEnhancer.FAMILY_PALEO);
        
        check("piControl", ExperimentMetadataEnhancer.FAMILY_CONTROL);
        check("esmControl", ExperimentMetadataEnhancer.FAMILY_CONTROL);
        check("aquaControl", ExperimentMetadataEnhancer.FAMILY_CONTROL);
        check("sstClim", ExperimentMetadataEnhancer.FAMILY_CONTROL);
        
        check("rcp26", ExperimentMetadataEnhancer.FAMILY_RCP);
        check("esmrcp85", ExperimentMetadataEnhancer.FAMILY_RCP);
                
        check("1pctCO2", ExperimentMetadataEnhancer.FAMILY_ALL);
        check("abrupt4xCO2", ExperimentMetadataEnhancer.FAMILY_ALL);
        
        check("amip", ExperimentMetadataEnhancer.FAMILY_ATMOS_ONLY);
        check("amip4K", ExperimentMetadataEnhancer.FAMILY_ATMOS_ONLY);
        check("amip4xCO2", ExperimentMetadataEnhancer.FAMILY_ATMOS_ONLY);
        check("amipFuture", ExperimentMetadataEnhancer.FAMILY_ATMOS_ONLY);
        check("aqua4K", ExperimentMetadataEnhancer.FAMILY_ATMOS_ONLY);
        check("aqua4xCO2", ExperimentMetadataEnhancer.FAMILY_ATMOS_ONLY);
        check("aquaControl", ExperimentMetadataEnhancer.FAMILY_ATMOS_ONLY);
        check("sst2030", ExperimentMetadataEnhancer.FAMILY_ATMOS_ONLY);
        check("sstClim", ExperimentMetadataEnhancer.FAMILY_ATMOS_ONLY);
        check("sstClim4xCO2", ExperimentMetadataEnhancer.FAMILY_ATMOS_ONLY);
        check("sstClimSulfate", ExperimentMetadataEnhancer.FAMILY_ATMOS_ONLY);
        check("sstClimAerosol", ExperimentMetadataEnhancer.FAMILY_ATMOS_ONLY);
        
    }
    
    private void check(String experiment, String experiment_family) {
        final Record record = new RecordImpl();
        List<String> values = new ArrayList<String>();
        values.add(experiment);
        eme.enhance(ThreddsPars.EXPERIMENT, values, record);
        Assert.isTrue(record.getFieldValues(SolrXmlPars.FIELD_EXPERIMENT_FAMILY).contains(experiment_family));
        Assert.isTrue(record.getFieldValues(SolrXmlPars.FIELD_EXPERIMENT_FAMILY).contains(ExperimentMetadataEnhancer.FAMILY_ALL));
    }

}
