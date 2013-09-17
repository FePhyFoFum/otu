package org.opentree.otu.constants;

import org.opentree.properties.OTProperty;

/**
 * Graph properties are stored in node 0. These are basic pieces of information relevant at the 
 * scale of the entire graph.
 */
public enum OTUGraphProperty implements OTProperty {
	
	/** The directory on the current system where the nexson git lies */
	NEXSON_GIT_DIR ("nexsonGitDir", String.class), //, to add more

	/** An array containing the names for all known remotes. To facilitate multiple remotes */
	KNOWN_REMOTES ("known_remotes", String[].class),
	
	/** Whether or not the taxonomy has been installed into the OTU db */
	HAS_TAXONOMY ("has_taxonomy", boolean.class);

	private String propertyName;
	private final Class<?> type;
    
    OTUGraphProperty(String propertyName, Class<?> T) {
        this.propertyName = propertyName;
        this.type = T;
    }

	@Override
	public String propertyName() {
		return propertyName;
	}

	@Override
	public Class<?> type() {
		return type;
	}
}
