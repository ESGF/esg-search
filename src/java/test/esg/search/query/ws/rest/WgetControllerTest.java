/*******************************************************************************
 * Copyright (c) 2010 Earth System Grid Federation
 * ALL RIGHTS RESERVED. 
 * U.S. Government sponsorship acknowledged.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * 
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * 
 * Neither the name of the <ORGANIZATION> nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package esg.search.query.ws.rest;

import java.io.FileReader;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.xpath.XPath;

import esg.search.core.RecordHelper;
import esg.search.publish.thredds.ThreddsPars;
import esg.search.query.api.QueryParameters;
import esg.search.query.impl.solr.SearchServiceImpl;
import esg.search.utils.XmlParser;

/**
 * Test class for {@link SearchServiceImpl}. This class uses the test harness
 * provided by the Solr libraries to create/use/destroy a temporary solr index.
 * 
 * Note that since we are inheriting from TestCase this is Junit3 !!
 * No notations will work here :-(
 * 
 */
public class WgetControllerTest extends P2PSolrTestCase {

    
    private static final int MAX_DIR_LEGTH = 100;
    /**
     * Setup the template properly before starting this test.
     * @throws Exception Not expected
     */
    private static boolean setup = false;
    private void initTemplate() throws Exception {
        if (setup) return;
        setup = true;
        
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
        
        WgetScriptGenerator.init(null);
    }
    

    public void setupSolr() throws Exception {
        //get three local cmip5 files from the default server
        defaultService = "http://esgf-data.dkrz.de/esg-search/search?";
        ingest("type=File&project=CMIP5&limit=3&distrib=false");
        
        //make sure we only have these three
        assertQ("couldn't find the records!", req("*"), "//result[@numFound=3]");
        assertQ("couldn't find *our* records!", req("project:CMIP5"), "//result[@numFound=3]");

    }
    /**
     * Tests ingestion of XML records from DATADIR directory into temporary Solr
     * index, and basic queries.
     */
    public void test() throws Exception {
        initTemplate();
        setupSolr();
        
        // get the result xml
        String result_xml = h.query(req("*"));
        final XmlParser xmlParser = new XmlParser(false);
        final Document doc = xmlParser.parseString(result_xml);
        XPath xpath = XPath.newInstance("/response/result/doc");
        
        //check we got all
        int res_count = ((Element) ((Element) xpath.selectSingleNode(doc))
                .getParent()).getAttribute("numFound").getIntValue();
        int ret_count = xpath.selectNodes(doc).size();
        assertEquals(3, res_count);
        assertEquals(res_count, ret_count);
        
        //prepare template
        WgetScriptGenerator.WgetDescriptor desc = new WgetScriptGenerator.WgetDescriptor(
                                                                                         "testServer", null, 
                                                                                         "searxhString");
        
        desc.addMessage("Message");
        
        //test path parameter
        final String emptyPath = "";
        final String downloadStructure= "url"; 
        String[] path = new String[0];
        if (downloadStructure != null && downloadStructure.length() > 0) {
            path = downloadStructure.split(",");
        }
        
        
        // loop over records
        final String[] fieldsToExtract = new String[] {
                QueryParameters.FIELD_CHECKSUM,
                QueryParameters.FIELD_CHECKSUM_TYPE,
                QueryParameters.FIELD_SIZE, 
                QueryParameters.FIELD_URL };
        StringBuilder dir = new StringBuilder();
        for (Object obj : xpath.selectNodes(doc)) {
            dir.setLength(0);
            Element docEl = (Element) obj;

            //prepare all attributes we need
            Map<String, String> attrib = new HashMap<String, String>();
            for (String s : fieldsToExtract)
                attrib.put(s, null);
            for (String s : path)
                attrib.put(s, emptyPath);

            //gather them
            for (final Object childObj : docEl.getChildren()) {
                Element childEl = (Element) childObj;
                if (attrib.containsKey(childEl.getAttributeValue("name"))) {
                    //Handle exceptions!
                    if (childEl.getAttributeValue("name")
                            .equals(QueryParameters.FIELD_URL)) {
                        String tuple = childEl.getChild("str")
                                .getTextNormalize();
                        String[] parts = RecordHelper.decodeTuple(tuple);
                        if (parts[2]
                                .equalsIgnoreCase(ThreddsPars.SERVICE_TYPE_HTTP)) {
                            attrib.put(childEl.getAttributeValue("name"), parts[0]);
                        }
                        //handle all arrays
                    } else if (childEl.getName() == "arr"){
                        attrib.put(childEl.getAttributeValue("name"), childEl
                                .getChild("str").getTextNormalize());
                        //and the rest as usual
                    } else {
                        attrib.put(childEl.getAttributeValue("name"), childEl
                                   .getTextNormalize());                        
                    }
                }
            }
            
            
            for (String facet : path) {
                //prevent strange values while generating names as well as too long names
                String value = attrib.get(facet).replaceAll("['<>?*\"\n\t\r\0]", "").replaceAll("[ /\\\\|:;]+", "_");
                if (value.length() > MAX_DIR_LEGTH) {
                    value = value.substring(0, MAX_DIR_LEGTH);
                }
                dir.append(value).append('/');
            }
            desc.addFile(attrib.get(QueryParameters.FIELD_URL),
                         dir.toString().replaceAll("/+","/"), attrib.get(QueryParameters.FIELD_SIZE), 
                         attrib.get(QueryParameters.FIELD_CHECKSUM_TYPE),
                         attrib.get(QueryParameters.FIELD_CHECKSUM));
        }
        System.out.println("\n\n >>DUMP wget description <<");
        System.out.println(desc);
        System.out.println("\n\n >>DUMP wget script <<");
        System.out.println(WgetScriptGenerator.getWgetScript(desc));
        
        
    }
}
