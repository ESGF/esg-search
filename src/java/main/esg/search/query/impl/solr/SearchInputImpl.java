package esg.search.query.impl.solr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.StringUtils;

import esg.search.query.api.SearchInput;

/**
 * Standard bean implementation of {@link SearchInput}.
 */
public class SearchInputImpl implements SearchInput {
	
	/**
	 * The free text used in the query.
	 */
	private String text;
	
	/**
	 * The map of constraints to be used in the query, composed of (name, values) pairs.
	 */
	private Map<String, List<String>> constraints = new LinkedHashMap<String, List<String>>();
	
	/**
	 * The ordered list of facets to be returned in the search output.
	 */
	private List<String> facets = new ArrayList<String>();
	
	/**
	 * The results type,if not null it must match a specific filter query handler.
	 */
	private String type = null;

	/**
	 * The offset into the number of returned results.
	 */
	private int offset = 0;
	
	/**
	 * The maximum number of results to be returned.
	 */
	private int limit = 10;
	
	private final static String NEWLINE = System.getProperty("line.separator");

	/**
	 * {@inheritDoc}
	 */
	public void addConstraint(final String name, final String value) {
		if (StringUtils.hasText(name) && StringUtils.hasText(value)) {
			if (!constraints.containsKey(name)) {
				constraints.put(name, new ArrayList<String>());
			}
			this.constraints.get(name).add(value);
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void removeConstraint(String name) {
		constraints.remove(name);
	}



	/**
	 * {@inheritDoc}
	 */
	public void addConstraints(final String name, final List<String> values) {
		if (StringUtils.hasText(name) && !values.isEmpty()) {
				this.constraints.put(name, values);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Map<String, List<String>> getConstraints() {
		return Collections.unmodifiableMap(constraints);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getText() {
		return text;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setText(final String text) {
		this.text = text;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getLimit() {
		return limit;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getOffset() {
		return offset;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setLimit(int limit) {
		this.limit = limit;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setOffset(int offset) {
		this.offset = offset;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public List<String> getFacets() {
		return Collections.unmodifiableList(facets);
	}

	/**
	 * {@inheritDoc}
	 */
	public void addFacet(final String facet) {
		this.facets.add(facet);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setFacets(final List<String> facets) {
		this.facets = facets;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getType() {
		return type;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setType(final String type) {
		this.type = type;
	}
	
	/**
	 * Overridden method to print the instance content.
	 */
	@Override
	public String toString() {
		
		// text
		final StringBuilder s = new StringBuilder();
		s.append("Search Text:"+this.getText()).append(NEWLINE);
		// constraints
		for (final String name : this.constraints.keySet()) {
			s.append("Search Constraint: ").append(name).append("=");
			for (final String value : this.constraints.get(name)) {
				s.append(value).append(" ");
			}
			s.append(NEWLINE);
		}
		// facets
		for (final String facet : facets) {
			s.append("Search Facet: ").append(facet).append("=").append(facet).append(NEWLINE);
		}
		// offset, limit
		s.append("Search offset: "+offset+" ").append(" limit: ").append(limit);
		return s.toString();
		
	}

}
