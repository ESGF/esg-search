package esg.search.query.ws.rest;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileReader;
import java.lang.reflect.Field;

import org.junit.BeforeClass;
import org.junit.Test;

import esg.search.query.ws.rest.WgetScriptGenerator.WgetDescriptor;

public class WgetScriptGeneratorTest {
	private static final boolean VERBOSE = true;
	
	private static WgetDescriptor createTestDescriptor() {
		WgetDescriptor desc = new WgetDescriptor("TESThostName",
				null, "TESTsearchUrl");
		desc.addFile(
				"http://bcccsm.cma.gov.cn/thredds/fileServer/cmip5_data/output/BCC/bcc-csm1-1/abrupt4xCO2/3hr/land/mrsos/r9i1p1/mrsos_3hr_bcc-csm1-1_abrupt4xCO2_r9i1p1_016009010000-016512312100.nc",
				"TESTid", "777", "TESTchksumType", "TESTchksum");
		desc.addFile(
				"http://bcccsm.cma.gov.cn/thredds/fileServer/cmip5_data/output/BCC/bcc-csm1-1/abrupt4xCO2/3hr/land/tslsi/r9i1p1/tslsi_3hr_bcc-csm1-1_abrupt4xCO2_r9i1p1_016009010000-016512312100.nc",
				"TESTid2", "7772", "TESTchksumType2", "TESTchksum2");
		return desc;
	}
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		FileReader fr = new FileReader("etc/conf/wget-template");
		StringBuilder sb = new StringBuilder();
		char[] buff = new char[1024];
		int read;
		while ((read = fr.read(buff)) == buff.length) {
			sb.append(buff);
		}
		sb.append(buff, 0, read);
		Field tmpField = WgetScriptGenerator.class.getDeclaredField("TEMPLATE");
		tmpField.setAccessible(true);
		tmpField.set(null, sb.toString());
	}

	@Test
	public void testInit() {
		WgetScriptGenerator.init(null);
	}
	
	@Test
	public void testTemplate() throws Exception{
		Field tmpField = WgetScriptGenerator.class.getDeclaredField("TEMPLATE");
		tmpField.setAccessible(true);
		Object tmp = tmpField.get(null);
		assertNotNull(tmp);
		assertTrue(tmp instanceof String);
		assertTrue(((String)tmp).length() > 100);
		if (VERBOSE) System.out.println(tmp);
	}
	
	@Test
	public void testGetWgetScript() {
		String tmp = WgetScriptGenerator.getWgetScript(createTestDescriptor());
		assertNotNull(tmp);
		assertTrue(tmp instanceof String);
		assertTrue(((String)tmp).length() > 100);
		if (VERBOSE) System.out.println(tmp);
	}




}
