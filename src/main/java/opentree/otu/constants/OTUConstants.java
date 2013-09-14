package opentree.otu.constants;

public enum OTUConstants {

	;
	
	public static final String SOURCE_ID = "SourceId";
	public static final String TREE_ID = "TreeId";
	public static final String WHITESPACE_SUBSTITUTE_FOR_SEARCH = "%s%";
	public static final String LOCAL_TREEID_PREFIX = "__local_id_";

	// all tree root node properties not specified here are fair game for user editing
	public static final NodeProperty[] PROTECTED_TREE_PROPERTIES = {
		NodeProperty.DESCENDANT_MAPPED_TAXON_NAMES,
		NodeProperty.DESCENDANT_MAPPED_TAXON_NAMES_WHITESPACE_FILLED,
		NodeProperty.DESCENDANT_MAPPED_TAXON_OTT_IDS,
		NodeProperty.DESCENDANT_ORIGINAL_TAXON_NAMES,
		NodeProperty.PHYLOGRAFTER_ID,
		NodeProperty.INGROUP_IS_SET,
		NodeProperty.INGROUP_START_NODE_ID,
		NodeProperty.NEXSON_ID,
		NodeProperty.IS_INGROUP_ROOT,
		NodeProperty.IS_ROOT,
		NodeProperty.IS_WITHIN_INGROUP,
		NodeProperty.LOCATION,
		NodeProperty.ROOTING_IS_SET,
		NodeProperty.SOURCE_ID,
		NodeProperty.TREE_ID
	};

	// all source meta node properties not specified here are fair game for user editing
	public static final NodeProperty[] PROTECTED_SOURCE_PROPERTIES = {
		NodeProperty.SOURCE_ID,
		NodeProperty.LOCATION
	};
	
/*	public static final NodeProperty[] VISIBLE_OTU_PROPERTIES = {
		NodeProperty.IS_WITHIN_INGROUP,
		NodeProperty.OT_ORIGINAL_LABEL
	}; */
	
	public static final NodeProperty[] VISIBLE_JSON_TREE_PROPERTIES = {
		NodeProperty.NAME, // TODO: need to change to use the correct taxon names
		NodeProperty.IS_WITHIN_INGROUP,
		NodeProperty.PROCESSED_BY_TNRS,
		NodeProperty.OT_ORIGINAL_LABEL,
		NodeProperty.OT_OTT_ID,
		NodeProperty.OT_OTT_TAXON_NAME,
		NodeProperty.IS_SAVED_COPY,
		NodeProperty.IS_WORKING_COPY
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
