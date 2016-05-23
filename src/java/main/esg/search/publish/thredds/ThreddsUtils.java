package esg.search.publish.thredds;

import java.net.URI;
import java.util.List;

import org.springframework.util.StringUtils;

import esg.search.core.Record;
import esg.search.query.api.QueryParameters;
import esg.search.query.impl.solr.SolrXmlPars;
import esg.search.utils.GeoUtils;
import thredds.catalog.InvAccess;
import thredds.catalog.InvCatalogRef;
import thredds.catalog.InvDataset;
import thredds.catalog.InvDatasetImpl;

/**
 * Class containing utilities for parsing THREDDS catalogs.
 * 
 * @author Luca Cinquini
 *
 */
public class ThreddsUtils {
    
    /**
     * Method to assert whether a THREDDS dataset represents a file.
     * @param dataset
     * @return
     */
    public static boolean isFile(final InvDataset dataset) {
        
        // 1st criteria: <property name="file_id" value="..."/>
        if ( StringUtils.hasText( dataset.findProperty(ThreddsPars.FILE_ID) ) ) {
            return true;
        } 
        
        // 2nd criteria: URL ending in .nc, .h5, .hdf, .he5
        // <dataset name="uas_ARMBE_ARM_Oklahoma_v1p1_19930101-20081231.nc" ID="ARMBE/armbe/ARMBE_Northward_Near_Surface_Wind_data/uas_ARMBE_ARM_Oklahoma_v1p1_19930101-20081231.nc" 
        //          urlPath="ARM/armbe/ARMBE_Northward_Near_Surface_Wind_data/uas_ARMBE_ARM_Oklahoma_v1p1_19930101-20081231.nc">
        for (final InvAccess access : dataset.getAccess()) {
            if (   access.getUrlPath().endsWith(".nc")
            	|| access.getUrlPath().endsWith(".h5")
            	|| access.getUrlPath().endsWith(".hdf")
            	|| access.getUrlPath().endsWith(".he5")) return true;
        }
        
        // not a file
        return false;
        
    }
    
    /**
     * Method to assert whether a THREDDS dataset represents an aggregation.
     * 
     * @param dataset
     * @return
     */
    public static boolean isAggregation(final InvDataset dataset) {
        
        if ( StringUtils.hasText( dataset.findProperty(ThreddsPars.AGGREGATION_ID) ) ) {
            return true;
        } else {
            return false;
        }
        
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
            if (ThreddsPars.mimeTypes.containsKey(_serviceType)) {
                return ThreddsPars.mimeTypes.get(_serviceType);
            } else {
                return serviceType;
            }
        }
    
    }
    
    public static URI getCatalogRef(final InvDataset dataset) throws Exception {

        final InvCatalogRef catalogRef = (InvCatalogRef) dataset;
        String uriString = InvDatasetImpl.resolve(dataset, catalogRef.getXlinkHref());
        uriString = uriString.replace("/./", "/");
        uriString = uriString.replace("\\.\\", "\\");
        final URI uri = new URI(uriString);
        uri.normalize();
        return uri;
        
    }
    
    /**
     * Method to add an element of the form:
     * <field name="geo">ENVELOPE(1.0, 180.0, 89.5, -89.5)</field>
     * from north/south/east/west degree elements of the form:
     * <field name="south_degrees">-89.5</field>
     * @param record
     */
    public static void addGeoCoverage(Record record) {
    	
		// complete geo-spatial location
        // ENVELOPE(-10, 20, 15, 10) # ENVELOPE(minX, maxX, maxY, minY)
        // <field name="geo">ENVELOPE(-74.093, -69.347, 44.558, 41.042)</field>
		if (   record.getFieldValue(SolrXmlPars.FIELD_WEST)!=null  && record.getFieldValue(SolrXmlPars.FIELD_EAST)!=null
			&& record.getFieldValue(SolrXmlPars.FIELD_SOUTH)!=null && record.getFieldValue(SolrXmlPars.FIELD_NORTH)!=null) {
			
			List<float[]> latRanges = GeoUtils.convertLongitudeRangeto180( 
					                     Float.parseFloat(record.getFieldValue(SolrXmlPars.FIELD_WEST)),
					                     Float.parseFloat(record.getFieldValue(SolrXmlPars.FIELD_EAST)) );
			for (float[] latRange : latRanges) {
    			record.addField(SolrXmlPars.FIELD_GEO, // "minLon minLat maxLon maxLat"
    					        "ENVELOPE("
    					       + latRange[0]  + ", "
    					       + latRange[1]  + ", "
    					       + record.getFieldValue(SolrXmlPars.FIELD_NORTH) + ", "
    					       + record.getFieldValue(SolrXmlPars.FIELD_SOUTH) 
    					       + ")" );
			}
				  			
		}
    	
    }
    
    private ThreddsUtils() {}

}
