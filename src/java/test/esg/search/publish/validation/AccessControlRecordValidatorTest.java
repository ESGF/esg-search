package esg.search.publish.validation;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import esg.common.util.ESGFProperties;
import esg.search.core.Record;
import esg.search.core.RecordImpl;
import esg.search.query.api.QueryParameters;

public class AccessControlRecordValidatorTest {
    
    AccessControlRecordValidator validator;
    
    @Before
    public void setup() throws Exception {
        
        String filepath = "esg/search/publish/validation/esgf_project_test_restrictions.xml";
        ESGFProperties props = new ESGFProperties();
        props.put(AccessControlRecordValidator.PROPERTIES_KEY, "esg-datanode.jpl.nasa.gov");        
        validator = new AccessControlRecordValidator(filepath, props);
        
    }
    
    @Test
    public void testNotValid() throws Exception {
        
        // project restricted, dataset not matching any policy
        List<String> errors = new ArrayList<String>();
        Record record = createRecord("obs4MIPs", "obs4MIPs.NASA-JPL.TES.mon.v1|esg-datanode.jpl.nasa.gov");
        validator.validate(record, errors);
        Assert.assertEquals(1, errors.size()); 
        
        // project restricted, dataset matches policy for different host
        errors.clear();
        record = createRecord("obs4MIPs", "obs4MIPs.NASA-GSFC.GPCP.atmos.mon.v20121102|esgdata1.nccs.nasa.gov");
        validator.validate(record, errors);
        Assert.assertEquals(1, errors.size()); 
        
        // project restricted, dataset matches policy for different host
        errors.clear();
        record = createRecord("CMIP5", "cmip5.X");
        validator.validate(record, errors);
        Assert.assertEquals(1, errors.size()); 
        
        // project restricted but supplied in wrong case, dataset matches policy for different host
        errors.clear();
        record = createRecord("CmIp5", "cmip5.X");
        validator.validate(record, errors);
        Assert.assertEquals(1, errors.size()); 
        
    }
    
    @Test
    public void testValid() throws Exception {
    
        // project not restricted
        List<String> errors = new ArrayList<String>();
        Record record = createRecord("MyProject", "my.id");
        validator.validate(record, errors);
        Assert.assertEquals(0, errors.size()); 
        
        // project restricted, dataset matches policy for this host
        record = createRecord("obs4MIPs", "obs4MIPs.NASA-JPL.AIRS.mon.v1|esg-datanode.jpl.nasa.gov");
        validator.validate(record, errors);
        Assert.assertEquals(0, errors.size()); 
        
        // project restricted, dataset matches policy for this host
        errors.clear();
        record = createRecord("obs4MIPs", "obs4MIPs.NASA-JPL.MLS.mon.v1|esg-datanode.jpl.nasa.gov");
        validator.validate(record, errors);
        Assert.assertEquals(0, errors.size()); 
        
        // project restricted but supplied in wrong case, dataset matches policy for this host
        errors.clear();
        record = createRecord("obs4mips", "obs4MIPs.NASA-JPL.MLS.mon.v1|esg-datanode.jpl.nasa.gov");
        validator.validate(record, errors);
        Assert.assertEquals(0, errors.size()); 
        
    }
    
    
    private Record createRecord(String project, String id) {
        
        Record record = new RecordImpl(id);
        record.setType(QueryParameters.TYPE_DATASET);
        record.setField(QueryParameters.PROJECT, project);
        return record;
        
    }
    
}
