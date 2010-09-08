package esg.search.query.impl.solr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import esg.search.core.Record;
import esg.search.query.api.SearchOutput;

/**
 * Standard bean implementation of {@link SearchOutput}.
 */
public class SearchOutputImpl implements SearchOutput {
	
	/**
	 * The list of records returned by the search.
	 */
	private List<Record> results = new ArrayList<Record>();
	
	/**
	 * The total number of records matching the search input criteria.
	 */
	private int counts = 0;

	/**
	 * The paging offset into the returned results.
	 */
	private int offset = 0;
	
	private final static String NEWLINE = System.getProperty("line.separator");
	
	/**
	 * {@inheritDoc}
	 */
	public void addResult(final Record record) {
		this.results.add(record);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Record> getResults() {
		return Collections.unmodifiableList(results);
	}

	/**
	 * {@inheritDoc}
	 */
	public int getCounts() {
		return counts;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setCounts(int counts) {
		this.counts = counts;
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
	public void setOffset(int offset) {
		this.offset = offset;
	}
	
	/**
	 * Overridden method to print out the {@link Record} content.
	 */
	@Override
	public String toString() {
		final StringBuilder s = new StringBuilder();
		s.append("Total number of results=").append(this.counts).append(NEWLINE);
		int i=0;
		for (final Record r : this.getResults()) {
			s.append("Result #").append(i++).append(": ")
			 .append(r.toString()).append(NEWLINE);
		}
		return s.toString();
	}

}
