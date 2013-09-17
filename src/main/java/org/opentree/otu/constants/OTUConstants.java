package org.opentree.otu.constants;

import org.opentree.properties.OTProperty;
import org.opentree.properties.OTVocabulary;

public enum OTUConstants {

	;
	
	public static final String SOURCE_ID = "SourceId";
	public static final String TREE_ID = "TreeId";
	public static final String WHITESPACE_SUBSTITUTE_FOR_SEARCH = "%s%";
	public static final String LOCAL_TREEID_PREFIX = "__local_id_";

	// all tree root node properties not specified here are fair game for user editing
	public static final OTProperty[] PROTECTED_TREE_PROPERTIES = {
		OTUNodeProperty.DESCENDANT_MAPPED_TAXON_NAMES,
		OTUNodeProperty.DESCENDANT_MAPPED_TAXON_NAMES_WHITESPACE_FILLED,
		OTUNodeProperty.DESCENDANT_MAPPED_TAXON_OTT_IDS,
		OTUNodeProperty.DESCENDANT_ORIGINAL_TAXON_NAMES,
		OTUNodeProperty.PHYLOGRAFTER_ID,
		OTUNodeProperty.INGROUP_IS_SET,
		OTUNodeProperty.INGROUP_START_NODE_ID,
		OTUNodeProperty.NEXSON_ID,
		OTUNodeProperty.IS_INGROUP_ROOT,
		OTUNodeProperty.IS_ROOT,
		OTUNodeProperty.IS_WITHIN_INGROUP,
		OTUNodeProperty.LOCATION,
		OTUNodeProperty.ROOTING_IS_SET,
		OTUNodeProperty.SOURCE_ID,
		OTUNodeProperty.TREE_ID
	};

	// all source meta node properties not specified here are fair game for user editing
	public static final OTProperty[] PROTECTED_SOURCE_PROPERTIES = {
		OTUNodeProperty.SOURCE_ID,
		OTUNodeProperty.LOCATION
	};
	
/*	public static final NodeProperty[] VISIBLE_OTU_PROPERTIES = {
		NodeProperty.IS_WITHIN_INGROUP,
		NodeProperty.OT_ORIGINAL_LABEL
	}; */
	
	public static final OTProperty[] VISIBLE_JSON_TREE_PROPERTIES = {
		OTUNodeProperty.NAME, // TODO: need to change to use the correct taxon names
		OTUNodeProperty.NODE_ID,
		OTUNodeProperty.IS_WITHIN_INGROUP,
		OTUNodeProperty.PROCESSED_BY_TNRS,
		OTVocabulary.OT_ORIGINAL_LABEL,
		OTVocabulary.OT_OTT_ID,
		OTVocabulary.OT_OTT_TAXON_NAME,
		OTUNodeProperty.IS_SAVED_COPY,
		OTUNodeProperty.IS_WORKING_COPY
	};
	
	public static final SearchableProperty[] TREE_PROPERTIES_FOR_SIMPLE_INDEXING = {
		SearchableProperty.BRANCH_LENGTH_MODE,
		SearchableProperty.TAG_TREE
	};
	
	public static final SearchableProperty[] SOURCE_PROPERTIES_FOR_SIMPLE_INDEXING = {
		SearchableProperty.CURATOR_NAME,
		SearchableProperty.DATA_DEPOSIT,
		SearchableProperty.PUBLICATION_REFERENCE,
		SearchableProperty.SOURCE_ID,
		SearchableProperty.STUDY_PUBLICATION,
		SearchableProperty.YEAR,
		SearchableProperty.TAG_SOURCE
	};
	
	// We just use the enum to hold arbitrary constant variables as above, so no need to set a generalized structure.
	OTUConstants() {}
}
