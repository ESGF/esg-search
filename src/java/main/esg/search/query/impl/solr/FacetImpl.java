package esg.search.query.impl.solr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import esg.search.query.api.Facet;

public class FacetImpl implements Facet {
	
	/**
	 * The facet key used for programmatic access (immutable).
	 */
	private final String key;
	
	/**
	 * The facet label displayed to human users (immutable).
	 */
	private final String label;
	
	/**
	 * Optional facet description (immutable).
	 */
	private final String description;
	
	/**
	 * The current facet count.
	 */
	private int counts;
	
	/**
	 * The current list of sub-facets.
	 */
	private List<Facet> subFacets = new ArrayList<Facet>();
	
	/**
	 * Constructor sets the immutable properties of the facet.
	 * @param key
	 * @param label
	 * @param description
	 */
	public FacetImpl(final String key, final String label, final String description) {
		this.key = key;
		this.label = label;
		this.description = description;
	}

	/**
	 * {@inheritDoc}
	 */
	public void addSubFacet(Facet subFacet) {
		this.subFacets.add(subFacet);
	}

	/**
	 * {@inheritDoc}
	 */
	public int getCounts() {
		return this.counts;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getDescription() {
		return this.description;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getKey() {
		return this.key;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getLabel() {
		return label;
	}

	public void setCounts(int counts) {
		this.counts = counts;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Facet> getSubFacets() {
		return Collections.unmodifiableList(this.subFacets);
	}
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("FacetImpl [key="+key+" label="+label+" description="+description+" counts=" + counts + "]\n");
		for (final Facet sf : this.getSubFacets()) {
			sb.append(sf.toString());
		}
		return sb.toString();
	}

}
