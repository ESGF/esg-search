package esg.search.query.api;

/**
 * Enumeration containing the supported return type formats for a remote metadata search.
 * 
 * @author luca.cinquini
 *
 */
public enum SearchReturnType {
	
    SOLR_XML("application/solr+xml");
	//ATOM_XML("application/atom+xml"),
	//ESGF_JSON("application/esgf+json");
	
	private final String mimeType;
	
	public String getMimeType() {
        return mimeType;
    }

    SearchReturnType(final String mimeType) {
	    this.mimeType = mimeType;
	}
    
    public static SearchReturnType forMimeType(String mimeType) {
        for (SearchReturnType rt : SearchReturnType.values()) {
            if (rt.getMimeType().equals(mimeType)) {
                return rt;
            }
        }
        return null;
    }
	
}
