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
package esg.search.query.impl.solr;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StringUtils;

import esg.search.query.api.QueryParameters;
import esg.search.query.api.SearchInput;
import esg.search.query.api.SearchReturnType;

/**
 * Utility class to generate URL according to the Solr REST API.
 */
public class SolrUrlBuilder {
	
	/**
	 * The base URL of the Solr server.
	 */
	private final URL url;
	
	/**
	 * The search input constraints.
	 */
	private SearchInput input;
	
	/**
	 * The facets to be retrieved as part of the search.
	 */
	private List<String> facets;
	
	/**
	 * The set of Solr shards to query for distributed search.
	 */
	private Set<String> shards = new HashSet<String>();
	
	/**
	 * Flag for pretty-formatting of output.
	 */
	//private boolean indent = false;
	
	private final static String UTF8 = "UTF-8";
	
	private static final Log LOG = LogFactory.getLog(SolrUrlBuilder.class);
	
	/**
	 * Constructor is initialized with the base URL of the Apache-Solr server.
	 * @param url
	 * @throws MalformedURLException
	 */
	public SolrUrlBuilder(final URL url) {
		this.url = url;
	}
	
	/**
	 * Method to set the search constraints to be included in the query part of URL.
	 * @param input
	 */
	public void setSearchInput(final SearchInput input) {
		this.input = input;
	}
	
	/**
	 * Method to set the search facet keys to be retrieved as part of the search.
	 * @param facets
	 */
	public void setFacets(final List<String> facets) {
		this.facets = facets;
	}
	
	/**
	 * Method to set the shards for distributed search.
	 * @param shards
	 */
	public void setShards(Set<String> shards) {
        this.shards = shards;
    }

    /**
	 * Method to generate the "update" URL to a specific core.
	 * This method is independent of the specific state of the object.
	 * @return
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 */
	public URL buildUpdateUrl(final String core, final boolean commit) throws MalformedURLException, UnsupportedEncodingException {
		
		final StringBuilder sb = new StringBuilder(url.toString()).append("/").append(core).append("/update");
		if (commit) sb.append("?commit=true");
		return new URL(sb.toString());
		
	}
	
	/**
	 * Method to generate the final "select" URL according to the instance's state.
	 * @return
	 */
	public URL buildSelectUrl() throws MalformedURLException, UnsupportedEncodingException {
			
	    // q=... AND .... AND
	    final List<String> qs = new ArrayList<String>();
	    // fq=name1:value1&fq=name2:value2
	    final StringBuilder fq = new StringBuilder("");
	    // facet=true&facet.field=...&facet.field=...
	    final StringBuilder ff = new StringBuilder("");
        // &fl=...&fl=...
	    final StringBuilder fl = new StringBuilder("");
	    
	    // The specific Solr core, as determined by the results type
	    final String core = SolrXmlPars.CORES.get(input.getType());
	    if (!StringUtils.hasText(core)) {
	        throw new MalformedURLException("Unsupported results type: "+input.getType()+" is not mapped to any Solr core");
	    }
	    
		// search input query --> q=....
		if (StringUtils.hasText(input.getQuery())) {
			qs.add( URLEncoder.encode(input.getQuery(), "UTF-8") );
		}
		// wildcard id --> q=id:....
		if (StringUtils.hasText(input.getConstraint(QueryParameters.ID))) {
		    qs.add(QueryParameters.FIELD_ID+":"+URLEncoder.encode(input.getConstraint(QueryParameters.ID), "UTF-8") );
		}
		// dataset_id
        //if (StringUtils.hasText(input.getConstraint(QueryParameters.FIELD_DATASET_ID))) {
        //    fq.append("&fq="+URLEncoder.encode( QueryParameters.FIELD_DATASET_ID+":"+input.getConstraint(QueryParameters.FIELD_DATASET_ID), "UTF-8" ));
        //}

		// replica=true|false
		if (StringUtils.hasText(input.getConstraint(QueryParameters.REPLICA))) {
		    fq.append("&fq="+URLEncoder.encode( QueryParameters.FIELD_REPLICA+":"+input.getConstraint(QueryParameters.REPLICA), "UTF-8" ));
		}
	    // master_id=...
        //if (StringUtils.hasText(input.getConstraint(QueryParameters.MASTER_ID))) {
        //    fq.append("&fq="+URLEncoder.encode( QueryParameters.FIELD_MASTER_ID+":"+input.getConstraint(QueryParameters.MASTER_ID), "UTF-8" ));
        //}

		// from,to --> q="timestamp:[2010-10-19T22:00:00Z TO NOW]"
		if (StringUtils.hasText(input.getConstraint(QueryParameters.FROM)) || StringUtils.hasText(input.getConstraint(QueryParameters.TO))) {
		    // set both defaults to "*"
		    if (!StringUtils.hasText(input.getConstraint(QueryParameters.FROM))) input.addConstraint(QueryParameters.FROM, "*");
		    if (!StringUtils.hasText(input.getConstraint(QueryParameters.TO))) input.addConstraint(QueryParameters.TO, "*");
		    qs.add( QueryParameters.FIELD_TIMESTAMP+
		            URLEncoder.encode(":["+input.getConstraint(QueryParameters.FROM)+" TO "+input.getConstraint(QueryParameters.TO)+"]", "UTF-8") );
		}		
		// start --> start <= datetime_stop --> datetime_stop:[start to *]
		if (StringUtils.hasText(input.getConstraint(QueryParameters.START))) {
		    qs.add( SolrXmlPars.FIELD_DATETIME_STOP+URLEncoder.encode(":["+input.getConstraint(QueryParameters.START)+" TO *]", "UTF-8") );
		}
		// stop --> datetime_start <= stop --> datetime_start:[* TO stop]
		if (StringUtils.hasText(input.getConstraint(QueryParameters.END))) {
		    qs.add(SolrXmlPars.FIELD_DATETIME_START+URLEncoder.encode(":[* TO "+input.getConstraint(QueryParameters.END)+"]", "UTF-8") );
		}
		
		// // [west, south, east, north]
		if (StringUtils.hasText(input.getConstraint(QueryParameters.BBOX))) {
		    // parse coordinate limits
		    String bbox = input.getConstraint(QueryParameters.BBOX);
		    bbox = bbox.substring(1,bbox.length()-1);
		    String[] coords = bbox.split("\\s*,\\s*");
		    
		    // west -> west <= east_degrees -> east_degrees:[west TO *]
		    qs.add(SolrXmlPars.FIELD_EAST+URLEncoder.encode(":["+coords[0]+" TO *]", "UTF-8") );
		    
		    // south -> south <= north_degrees -> north_degrees:[south TO *]
		    qs.add(SolrXmlPars.FIELD_NORTH+URLEncoder.encode(":["+coords[1]+" TO *]", "UTF-8") );
		    
		    // east -> west_degrees <= east -> west_degrees:[* TO east]
		    qs.add(SolrXmlPars.FIELD_WEST+URLEncoder.encode(":[* TO "+coords[2]+"]", "UTF-8") );
		    
		    // north -> south_degrees <= north --> south_degrees:[* TO north]
		    qs.add(SolrXmlPars.FIELD_SOUTH+URLEncoder.encode(":[* TO "+coords[3]+"]", "UTF-8") );
		    
		}
				
		// no text constraint
		if (qs.isEmpty()) qs.add(URLEncoder.encode("*", "UTF-8"));		
		
		// search input type --> fq=type:Dataset
		if (StringUtils.hasText(input.getType())) {
			fq.append("&fq="+URLEncoder.encode( QueryParameters.FIELD_TYPE+":"+"\""+input.getType()+"\"","UTF-8" ));
		}
		
		// search input constraints --> fq=facet_name:"facet_value"
		final Map<String, List<String>> constraints = input.getConstraints();
		// experiment=1pctCO2 --> fq=experiment:"1pctCO2"
		// experiment=1pctCO2&variable=huss --> fq=experiment:"1pctCO2"&fq=variable:"huss"
		// experiment=1pctCO2&variable=huss&variable=clt --> fq=experiment:"1pctCO2"&fq=variable:"huss"+||+variable:"clt"
		// experiment=1pctCO2&variable=!huss --> fq=experiment:"1pctCO2"&fq=-variable:"huss"
		// experiment=1pctCO2&variable=!huss&variable=!clt --> fq=experiment:"1pctCO2"&fq=-variable:"huss"&fq=-variable:"clt"
		// experiment=1pctCO2&variable=!huss&variable=clt --> fq=experiment:"1pctCO2"&fq=variable:"clt"&fq=-variable:"huss"
		if (!constraints.isEmpty()) {
			for (final String facet : constraints.keySet()) {
			    if (!QueryParameters.KEYWORDS.contains(facet)) { // skip keywords
			        //fq.append("&fq=");
			        StringBuilder yesClause = new StringBuilder("");
			        StringBuilder noClause = new StringBuilder("");
    				for (final String value : constraints.get(facet)) {	
    				    if (value.startsWith("!")) {
    				        noClause.append("&fq=-").append(URLEncoder.encode( facet+":"+"\""+value.substring(1)+"\"","UTF-8" ));
    				    } else {
    				        // combine multiple values for the same facet in logical "OR"
    				        if (yesClause.length()==0) {
    				            yesClause.append("&fq=");
    				        } else {
    				            yesClause.append(URLEncoder.encode(" || ", "UTF-8"));
    				        }
    				        yesClause.append( URLEncoder.encode( facet+":"+"\""+value+"\"","UTF-8" ) );
    				    }
    				}
    				if (yesClause.length()>0) fq.append(yesClause);
    				if (noClause.length()>0) fq.append(noClause);
			    }
			}
		}

		/*
		String geospatialRangeConstraints = input.getGeospatialRangeConstraint();
		// search input geospatial range constraints --> fq=(west_degrees:[* TO 45] AND east_degrees:[40 TO *]...)
		if(geospatialRangeConstraints!=null) {
			String value = geospatialRangeConstraints;
			fq.append("&fq="+URLEncoder.encode("(" + value + ")","UTF-8" ));
		} */
		
		/*
		String temporalRangeConstraints = input.getTemporalRangeConstraint();
        // search input geospatial range constraints --> fq=(datetime_start:[NOW/DAY-YEAR TO NOW] AND datetime_stop:[NOW/DAY-3MONTH TO NOW]...)
        if(temporalRangeConstraints!=null) {
            String value = temporalRangeConstraints;
            fq.append("&fq="+URLEncoder.encode("(" + value + ")","UTF-8" ));
        }*/
        
        // &facet.field=...&facet.field=...
        if (this.facets!=null) {
            ff.append("&facet=true");
            for (final String facet : this.facets) {
                ff.append("&facet.field=").append( URLEncoder.encode(facet, UTF8 ));
            }
        }
        
        // &fl=...&fl=...
        if (!input.getFields().isEmpty()) {
            fl.append("&fl=");
            for (String field : input.getFields()) {
                fl.append(field+",");
            }
            // always return score
            fl.append("score");
            System.out.println("FIELDS="+fl.toString());
        }
        
        // compose final URL
        final StringBuilder sb = new StringBuilder(url.toString()).append("/").append(core)
                                                                  .append("/select/?indent=true");
        // q=...
        sb.append("&q=");
        boolean first = true;
        for (final String q : qs) {
            if (!first) sb.append(URLEncoder.encode(" AND ","UTF-8"));
            sb.append(q);
            first = false;
        }
        // fq, ff, fl
        sb.append(fq).append(ff).append(fl);
        // &start=...@rows=...
        sb.append("&start=").append(input.getOffset())
          .append("&rows=").append(input.getLimit());
        
        // indent=true
        //if (this.indent) {
        //  sb.append("indent=true");
        //}
        
        // distributed search
        // only attach shards if available, otherwise default to local search
        // &shards=localhost:8983/solr/datasets
        //if (input.isDistrib()) sb.append("&qt=/distrib");
        if (LOG.isInfoEnabled()) LOG.info("Search distrib="+input.isDistrib()+" shards size="+shards.size());
        if (input.isDistrib() && shards.size()>0) {
            sb.append("&shards=");
            for (String shard : shards) {
                if (sb.charAt(sb.length()-1) != '=') sb.append(",");
                sb.append(shard).append("/").append(core);
            }
        }
        
        // return type
        if (input.getFormat().equals(SearchReturnType.SOLR_JSON.getMimeType())) {
            sb.append("&wt=json");
        }        
        
		if (LOG.isInfoEnabled()) LOG.info("Select URL=" + sb.toString());
		return new URL(sb.toString());
		
	}
	
	
}
