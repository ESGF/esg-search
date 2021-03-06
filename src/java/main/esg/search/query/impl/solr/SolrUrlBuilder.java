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
import java.util.LinkedHashSet;
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
	 * The set of default Solr shards to query for distributed search,
	 * if the shards are not explicitely specified.
	 */
	private LinkedHashSet<String> defaultShards = new LinkedHashSet<String>();
	
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
	 * Method to set the list of default shards for distributed search.
	 * @param shards
	 */
	public void setDefaultShards(LinkedHashSet<String> shards) {
        this.defaultShards = shards;
    }

    /**
	 * Method to generate the "update" URL to a specific core.
	 * 
	 * This method is independent of the specific state of the object.
	 * @return
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 */
	public URL buildUpdateUrl(final String core) throws MalformedURLException, UnsupportedEncodingException {
		
		final StringBuilder sb = new StringBuilder(url.toString()).append("/").append(core).append("/update");
		//if (commit) sb.append("?commit=true&optimize=true");
		//if (commit) sb.append("?commit=true");
		return new URL(sb.toString());
		
	}
	
	/**
	 * Method to generate the "select" URL to a specific Solr core (depending on the requested results type)
	 * @return
	 */
	public String buildSelectUrl() throws MalformedURLException {
	    	    
        // the core-specific select URL
        final StringBuilder sb = new StringBuilder(url.toString()).append("/").append(this.getCore()).append("/select/");
        
        final String url = sb.toString();
        if (LOG.isInfoEnabled()) LOG.info("Select URL: "+url);
        return url;

	}
	
	/**
	 * Method to generate the "select" query string according to the instance's state.
	 * @return
	 */
	public String buildSelectQueryString() throws MalformedURLException, UnsupportedEncodingException {
			
	    // q=... AND .... AND
	    final List<String> qs = new ArrayList<String>();
	    // fq=name1:value1&fq=name2:value2
	    final StringBuilder fq = new StringBuilder("");
	    // facet=true&facet.field=...&facet.field=...
	    final StringBuilder ff = new StringBuilder("");
        // &fl=...&fl=...
	    final StringBuilder fl = new StringBuilder("");
	    	    
		// search input query --> q=....
		if (StringUtils.hasText(input.getQuery())) {
			qs.add( URLEncoder.encode(input.getQuery().trim(), "UTF-8") );
		}
		
		// from,to --> q="timestamp:[2010-10-19T22:00:00Z TO NOW]"
        // note: these special fields must be processed together
		if (StringUtils.hasText(input.getFrom()) || StringUtils.hasText(input.getTo())) {
		    // set both defaults to "*"
		    if (!StringUtils.hasText(input.getFrom())) input.setFrom("*");
		    if (!StringUtils.hasText(input.getTo())) input.setTo("*");
		    qs.add( QueryParameters.FIELD_TIMESTAMP_+URLEncoder.encode(":["+input.getFrom()+" TO "+input.getTo()+"]", "UTF-8") );
		}		
										
		// search input constraints --> fq=facet_name:"facet_value"
		final Map<String, List<String>> constraints = input.getConstraints();
		// experiment=1pctCO2 --> fq=experiment:"1pctCO2"
		// experiment=1pctCO2&variable=huss --> fq=experiment:"1pctCO2"&fq=variable:"huss"
		// experiment=1pctCO2&variable=huss&variable=clt --> fq=experiment:"1pctCO2"&fq=variable:"huss"+||+variable:"clt"
		// experiment=1pctCO2&variable=!huss --> fq=experiment:"1pctCO2"&fq=-variable:"huss"
		// experiment=1pctCO2&variable=!huss&variable=!clt --> fq=experiment:"1pctCO2"&fq=-variable:"huss"&fq=-variable:"clt"
		// experiment=1pctCO2&variable=!huss&variable=clt --> fq=experiment:"1pctCO2"&fq=variable:"clt"&fq=-variable:"huss"
		String type = QueryParameters.TYPE_DATASET; // default record type
		if (!constraints.isEmpty()) {
			for (final String name : constraints.keySet()) {
			      
			    // first process single-valued constraints
		        if (name.equals(QueryParameters.FIELD_TYPE)) {
		            fq.append("&fq="+URLEncoder.encode( name+":"+input.getConstraint(name), "UTF-8" ));
		            type = input.getConstraint(name);
		           
		        // boolean replica=true|false, latest=true|false
		        } else if (  name.equals(QueryParameters.FIELD_REPLICA) 
		        		  || name.equals(QueryParameters.FIELD_LATEST)  
		        		  || name.equals(QueryParameters.FIELD_RETRACTED)) {   
		            if (StringUtils.hasText(input.getConstraint(name))) {
		                fq.append("&fq="+URLEncoder.encode( name+":"+input.getConstraint(name), "UTF-8" ));
		            }
		            
		        // start --> start <= datetime_stop --> datetime_stop:[start to *]
		        } else if (name.equals(QueryParameters.FIELD_START)) { 
		            if (StringUtils.hasText(input.getConstraint(name))) {
		                qs.add( SolrXmlPars.FIELD_DATETIME_STOP+URLEncoder.encode(":["+input.getConstraint(name)+" TO *]", "UTF-8") );
		            }
		               
		       // stop --> datetime_start <= stop --> datetime_start:[* TO stop]
		       } else if (name.equals(QueryParameters.FIELD_END)) {
		            if (StringUtils.hasText(input.getConstraint(name))) {
		                qs.add(SolrXmlPars.FIELD_DATETIME_START+URLEncoder.encode(":[* TO "+input.getConstraint(name)+"]", "UTF-8") );
		            }

		       // max_version=20110608 --> q= ... AND ${vers}:[* TO 20110608]
		       // min_version=20110608 --> q= ... AND ${vers}:[20110608 TO *]
		       //                          fq=... &vers=version
		       // the implicit 'field' operator transforms the string 'version' into a numerical value
		       } else if (name.equals(QueryParameters.FIELD_MAX_VERSION) || name.equals(QueryParameters.FIELD_MIN_VERSION)) {
		    	   
		    	   if (StringUtils.hasText(input.getConstraint(name))) {
		    		   
		    		   if (name.equals(QueryParameters.FIELD_MAX_VERSION)) {
		    			   qs.add( URLEncoder.encode( "${vers}:[* TO "+input.getConstraint(name)+"]", "UTF-8") );
		    		   } else {
		    			   qs.add( URLEncoder.encode( "${vers}:["+input.getConstraint(name)+" TO *]", "UTF-8") );
		    		   }
		    		   
		    		   if (fq.indexOf("&vers=version")<0) fq.append("&vers=version"); // one time only
		    	   }
		            
		       } else if (name.equals(QueryParameters.FIELD_BBOX)) {
		           
		           // [west, south, east, north]
		           if (StringUtils.hasText(input.getConstraint(name))) {
		               
		               // parse coordinate limits
		               String bbox = input.getConstraint(name);
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

		       } else if ( name.equals(QueryParameters.FROM) || name.equals(QueryParameters.TO) ) {
		           // do nothing, already processed
		            
		        // all other multi-valued constraints, positive and negative
		        } else {
		            
			        StringBuilder yesClause = new StringBuilder("");
			        StringBuilder noClause = new StringBuilder("");
    				for (final String value : constraints.get(name)) {	
    				    String val = quote(value);
    				    if (name.endsWith("!")) {
    				        String _name = name.substring(0, name.length()-1 ); // remove trailing '!'
    				        noClause.append("&fq=-").append(URLEncoder.encode( _name+":"+val,"UTF-8" ));
    				    } else {
    				        // combine multiple values for the same facet in logical "OR"
    				        if (yesClause.length()==0) {
    				            yesClause.append("&fq=");
    				        } else {
    				            yesClause.append(URLEncoder.encode(" || ", "UTF-8"));
    				        }
    				        yesClause.append( URLEncoder.encode( name+":"+val,"UTF-8" ) );
    				    }
    				}
    				if (yesClause.length()>0) fq.append(yesClause);
    				if (noClause.length()>0) fq.append(noClause);
				
		        }
		        
		    }
			
		}
		
	    // if no text constraint -> use '*'
        if (qs.isEmpty()) qs.add(URLEncoder.encode("*:*", "UTF-8"));      
        
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
        }
        
        // compose query string
        final StringBuilder sb = new StringBuilder("indent=true");
        
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
        
        // &start=...&rows=...
        sb.append("&start=").append(input.getOffset())
          .append("&rows=").append(input.getLimit());
                
        // distributed search
        // only attach shards if available, otherwise default to local search
        // &shards=localhost:8983/solr/datasets
        //if (input.isDistrib()) sb.append("&qt=/distrib");
        if (LOG.isInfoEnabled()) LOG.info("Search distrib="+input.isDistrib()+" shards size="+input.getShards().size()+" default shards size="+this.defaultShards.size());
        if (input.isDistrib()) {
            
            // use provided shards
            if (input.getShards().size()>0) {
                setShards(input.getShards(), this.getCore(), sb);
                
            // or use all shards
            } else if (this.defaultShards.size()>0) {
                setShards(this.defaultShards, this.getCore(), sb);
                
            }
        }
        
        // return type
        if (input.getFormat().equals(SearchReturnType.SOLR_JSON.getMimeType())) {
            sb.append("&wt=json");
        } else {
        		sb.append("&wt=xml");
        }
        
        // if requested, sort records by timestamp descending
        if (input.isSort()) {
            sb.append("&sort="+URLEncoder.encode(QueryParameters.FIELD_TIMESTAMP_+" desc","UTF-8"));
        // otherwise by default sort Files by id ascending
        } else {
        	if (type.equals(QueryParameters.TYPE_FILE)) {
        		sb.append("&sort="+URLEncoder.encode(QueryParameters.FIELD_ID+" asc","UTF-8"));
        	}
        }
        
        final String queryString = sb.toString();
		if (LOG.isInfoEnabled()) LOG.info("Select Query String: "+queryString);
		return queryString;
		
	}
	
	private String quote(String s) {
	    if (!s.startsWith("\"")) s = "\"" +s;
	    if (!s.endsWith("\"")) s = s + "\"";
	    return s;
	}
	
	private void setShards(final Set<String> shards, final String core, final StringBuilder sb) {
        sb.append("&shards=");
        for (String shard : shards) {
            if (sb.charAt(sb.length()-1) != '=') sb.append(",");
            sb.append(shard).append("/").append(core);
        }
	}
	
	/**
	 * Method to determine the Solr core depending on the requested results type.
	 * @return
	 * @throws MalformedURLException
	 */
	private String getCore() throws MalformedURLException {
	        
        // The specific Solr core, as determined by the results type
        final String type = input.getConstraint(QueryParameters.FIELD_TYPE);
        final String core = SolrXmlPars.CORES.get(type);
        if (!StringUtils.hasText(core)) {
            throw new MalformedURLException("Unsupported results type: "+type+" is not mapped to any Solr core");
        }
        
        return core;
	}
	
}
