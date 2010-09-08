package esg.search.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


/**
 * Standard bean implementation of the {@link Record} interface.
 */
public class RecordImpl implements Record {
	
	/**
	 * The record unique identifier.
	 */
	String id;
	

	/**
	 * A map of multi-valued fields applicable to this record.
	 * The map is ordered on keys to allow for easier testing.
	 */
	final Map<String,List<String>> fields = new TreeMap<String, List<String>>();
	
	/**
	 * Constructor for yet unknown record identifier.
	 */
	public RecordImpl() {}
	
	/**
	 * Constructor for known unique record identifier.
	 * @param id
	 */
	public RecordImpl(String id) {
		this.id = id;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Map<String, List<String>> getFields() {
		return Collections.unmodifiableMap(fields);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Note that this implementation does not add pairs where the key or the value is null or blank.
	 */
	public void addField(final String name, final String value) {
		
		if (this.hasText(name) && this.hasText(value)) {
			if (!fields.containsKey(name)) {
				fields.put(name, new ArrayList<String>());
			}
			fields.get(name).add(value);
		}
		
	}
	
	private boolean hasText(final String s) {
		return s!=null && s.trim().length()>0;
	}
	
	public String toString() {
		
		final StringBuilder sb = new StringBuilder();
		sb.append("Record ID="+id);
		for (final String key : fields.keySet()) {
			sb.append(" [field name="+key+" values="+fields.get(key)+"] ");
		}
		return sb.toString();
		
	}

}