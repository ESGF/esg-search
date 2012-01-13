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
package esg.search.publish.thredds;

import java.util.HashMap;
import java.util.Map;

import esg.search.query.api.QueryParameters;

/**
 * Class containing THREDDS constants.
 */
public class ThreddsPars {

	public final static String CF = "CF-1.0";
	
	public final static String ID = "ID";
	public final static String DATASET_ID = "dataset_id";
	public final static String FILE_ID = "file_id";
	public final static String AGGREGATION_ID = "aggregation_id";
	public final static String DATASET_VERSION = "dataset_version";
	public final static String FILE_VERSION = "file_version";
	public final static String SIZE = "size";
	//public final static String EXPERIMENT = "experiment";
	
	// replica properties
	public final static String IS_REPLICA = "is_replica";
	//public final static String MASTER_NODE = "master_node";
	//public final static String REPLICA_NODE = "replica_node";
	
	
	// THREDDS service types
	/**
	  <!-- ServiceTypeEnum -->
      <xsd:simpleType name="serviceTypes">
        <xsd:union memberTypes="xsd:token">
          <xsd:simpleType>
            <xsd:restriction base="xsd:token">
              <!-- client/server -->
              <xsd:enumeration value="ADDE"/>
              <xsd:enumeration value="DODS"/>  <!-- same as OpenDAP -->
              <xsd:enumeration value="OpenDAP"/>
              <xsd:enumeration value="OpenDAP-G"/>
    
              <!-- bulk transport -->
              <xsd:enumeration value="HTTPServer"/>
              <xsd:enumeration value="FTP"/>
              <xsd:enumeration value="GridFTP"/>
              <xsd:enumeration value="File"/>
    
              <xsd:enumeration value="NetcdfSubset"/>
              <xsd:enumeration value="NetcdfStream"/>
              <xsd:enumeration value="RemotePointFeature"/>
    
              <!-- web services -->
              <xsd:enumeration value="LAS"/>
              <xsd:enumeration value="WMS"/>
              <xsd:enumeration value="WFS"/>
              <xsd:enumeration value="WCS"/>
              <xsd:enumeration value="WSDL"/>
    
              <!--offline -->
              <xsd:enumeration value="WebForm"/>
    
              <!-- THREDDS -->
              <xsd:enumeration value="Catalog"/>
              <xsd:enumeration value="QueryCapability"/>
              <xsd:enumeration value="Resolver"/>
              <xsd:enumeration value="Compound"/>
            </xsd:restriction>
          </xsd:simpleType>
        </xsd:union>
      </xsd:simpleType>
	 */
	public final static String SERVICE_TYPE_CATALOG = "Catalog";
	public final static String SERVICE_TYPE_HTTP = "HTTPServer";
	public final static String SERVICE_TYPE_OPENDAP = "OpenDAP";
	public final static String SERVICE_TYPE_LAS = "LAS";
	public final static String SERVICE_TYPE_GRIDFTP = "GridFTP";
	public final static String SERVICE_TYPE_FTP = "FTP";
		
	private final static Map<String,String> mimeTypes = new HashMap<String,String>();
	
	// static initializer populates the mime types map
	static {
	    
	    // make keys case-insensitive
	    mimeTypes.put(SERVICE_TYPE_CATALOG.toLowerCase(), QueryParameters.MIME_TYPE_THREDDS);
	    mimeTypes.put(SERVICE_TYPE_OPENDAP.toLowerCase(), QueryParameters.MIME_TYPE_OPENDAP);
	    mimeTypes.put(SERVICE_TYPE_LAS.toLowerCase(), QueryParameters.MIME_TYPE_LAS);
	    mimeTypes.put(SERVICE_TYPE_GRIDFTP.toLowerCase(), QueryParameters.MIME_TYPE_GRIDFTP);
	    mimeTypes.put(SERVICE_TYPE_FTP.toLowerCase(), QueryParameters.MIME_TYPE_FTP);
	    mimeTypes.put(SERVICE_TYPE_HTTP.toLowerCase(), QueryParameters.MIME_TYPE_HTML);
	    
	}
	
	/**
	 * Method to map the THREDDS service type to a known mime/type is possible, otherwise return the service type itself.
	 * Note that in some cases, it is necessary to parse the URL extension to determine the proper mime type.
	 * 
	 * @param serviceType
	 * @return
	 */
	public static final String getMimeType(final String url, final String serviceType) {
	    
	    // execute case-insensitive comparisons
	    final String _serviceType = serviceType.toLowerCase();
	    final String _url = url.toLowerCase();
	    
	    // special mapping of HTTP URLs
	    if (_serviceType.equalsIgnoreCase(ThreddsPars.SERVICE_TYPE_HTTP)) {
	        if (_url.endsWith(".nc") || _url.endsWith(".cdf")) {
	            return QueryParameters.MIME_TYPE_NETCDF;
	        } else if (_url.endsWith(".hdf") || _url.endsWith(".h5")) {
	            return QueryParameters.MIME_TYPE_HDF;
	        } else {
	            return QueryParameters.MIME_TYPE_HTML;
	        }
	        
	    // special mapping of OpenDAP URLs
	    } else if (_serviceType.equalsIgnoreCase(ThreddsPars.SERVICE_TYPE_OPENDAP)) {
	        if (_url.endsWith(".html")) {
	            return QueryParameters.MIME_TYPE_OPENDAP_HTML;
	        } else if (_url.endsWith(".dods")) {
	            return QueryParameters.MIME_TYPE_OPENDAP_DODS;
	        } else if (_url.endsWith(".das")) {
                return QueryParameters.MIME_TYPE_OPENDAP_DAS;
            } else if (_url.endsWith(".dds")) {
                return QueryParameters.MIME_TYPE_OPENDAP_DDS;
            } else {
                return QueryParameters.MIME_TYPE_OPENDAP;
            }
	        
	    } else {	    
    	    if (mimeTypes.containsKey(_serviceType)) {
    	        return mimeTypes.get(_serviceType);
    	    } else {
    	        return serviceType;
    	    }
	    }
	
	}
	
	private ThreddsPars() {};
	
}