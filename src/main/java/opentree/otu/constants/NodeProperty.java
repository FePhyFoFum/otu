package opentree.otu.constants;

/**
 * Node properties as defined within the graph itself. These are stored in graph nodes. Different types
 * of nodes may have different properties. For more information see:
 * 
 * https://github.com/OpenTreeOfLife/treemachine/wiki/Vocabulary
 * 
 */
public enum NodeProperty {

	/**
	 * The taxon name associated with this node. SHOULD BE THE MAPPED NAME, not the original. Should not be set if the
	 * node has not been mapped (although I have no idea if this is actually the case).
	 */
	NAME ("name", String.class), // TODO: clean this / switch it over when peter has changed the nexson output
	
	/**
	 * A unique string used to identify this tree within the db. The convention is to use the study id concatenated
	 * by an underscore to an id unique for trees within studies, e.g. 10_1. For trees incoming from nexsons, we attempt
	 * to use any incoming tree id. If this is absent, or if the tree is not coming from a nexson, we assign an arbitrary
	 * id string that is unique for trees within the originating study, e.g. 10____local_id_1.
	 */
	TREE_ID ("tree_id", String.class),
			
	/**
	 * A unique string used to identify this source. For nexsons, this is the study id. For local sources, this is assigned
	 * on import.
	 */
	SOURCE_ID ("source_id", String.class),
	
	/**
	 * A unique string identifying the repository to which tree and source nodes belong. Currently, the only options are
	 * "remote" and "local", although multiple repos could be indicated by using other values.
	 */
	LOCATION ("location", String.class),
	
	/**
	 * A primitive string array containing all the original taxon names applied to tip children of a given tree node.
	 * This is stored as a property of the root of each imported tree.
	 */
	DESCENDANT_ORIGINAL_TAXON_NAMES ("tip_original_names", String[].class),
	
	/**
	 * A primitive string array containing all the currently mapped taxon names applied to tip children of a given tree node.
	 * This is stored as a property of the root of each imported tree.
	 */
	DESCENDANT_MAPPED_TAXON_NAMES ("tip_mapped_names", String[].class),

	/**
	 * A primitive string array containing all the currently mapped taxon names applied to tip children of a given tree node,
	 * with whitespace replaced by the whitespace replacement substring specified in GeneralConstants. This is stored as a
	 * property of the root of each imported tree.
	 */
	DESCENDANT_MAPPED_TAXON_NAMES_WHITESPACE_FILLED ("tip_mapped_names_no_spaces", String[].class),
	
	/**
	 * A primitive string array containing all the ott ids for taxa mapped to the tip children of a given tree node.
	 * This is stored as a property of the root of each imported tree.
	 */
	DESCENDANT_MAPPED_TAXON_OTT_IDS ("tip_mapped_ottids", long[].class),
	
	/**
	 * The OTT id of the focal clade for this source. A phylografter property that we may never use.
	 */
	FOCAL_CLADE ("focal_clade_ott_id", String.class),

	/**
	 * An id property used within nexson files. Relevance to OTU is unclear.
	 */
	NEXSON_ID ("nexsonid", String.class),

	/**
	 * An id property used by phylografter. Not relevant to OTU, but we include it here to allow us to hide it when returning metadata.
	 */
	PHYLOGRAFTER_ID ("id", long.class),
	
	/**
	 * A boolean indicating whether this tree has been rooted. Stored as a property of the root node. If the tree root lacks
	 * this property then it can be inferred that the tree has not been rooted.
	 */
	ROOTING_IS_SET ("is_rooted", boolean.class),
	
	/**
	 * A boolean indicating that this node is the root for its tree. Should always (and only) be set to true for the root
	 * node. All other tree nodes should lack this property entirely.
	 */
	IS_ROOT ("is_root", boolean.class),
	
	/**
	 * A boolean indicating whether the ingroup is set for this tree. Stored as a property of the tree root node. If the
	 * tree root lacks this property, then the ingroup can be inferred not to be set.
	 */
	INGROUP_IS_SET ("ingroup_is_set", boolean.class),
	
	/**
	 * A flag specifying that the clade represented by the node is the ingroup for the tree. Is only set on the root node
	 * of the ingroup clade. A phylografter property imported by NexsonReader.
	 */
	IS_INGROUP_ROOT ("ingroup_start", boolean.class),

	/**
	 * The nodeid of the root node for the designated ingroup for this tree. Should not be set unless the ingroup has been
	 * designated.
	 */
	INGROUP_START_NODE_ID ("ingroup_node_id", boolean.class),
	
	/**
	 * A flag specifying that this node is part of the ingroup for this tree. This property is nominally a boolean but should
	 * only be set on nodes that are actually part of the ingroup, implying that nodes without this property in trees that
	 * have their ingroup set are thus part of the outgroup.
	 */
	IS_WITHIN_INGROUP ("within_ingroup", boolean.class),
	
	/**
	 * Whether or not this tree contains otu nodes that have been mapped to OTT taxa. Used for user feedback during TNRS.
	 */
	PROCESSED_BY_TNRS ("processed_by_tnrs", boolean.class),
	
	/**
	 * The name of the taxomachine context that has been determined for this tree.
	 */
	CONTEXT_NAME ("context_name", String.class),
	
	// ===== ot namespace node properties
	
	/**
	 * The type of branch lengths for this tree. A property of tree root nodes.
	 */
	OT_BRANCH_LENGTH_MODE("ot:branchLengthMode", String.class),

	/**
	 * The name of curator who uploaded the source. Used for study sources.
	 */
	OT_CURATOR_NAME("ot:curatorName", String.class),

	/**
	 * A URI (or other identifier) for the published data. Used for study sources.
	 */
	OT_DATA_DEPOSIT("ot:dataDeposit", String.class),

	/**
	 * Some unknown identifier node that is the designated ingroup for this tree. A property of tree root nodes.
	 * 
	 * TODO: Not clear if this is a supported ot property, it is not listed in Peter's spreadsheet. Should be the id of the
	 * element in the file, should it not? Otherwise it has no integrity. Need to figure this out...
	 */
	OT_INGROUP_CLADE("ot:inGroupClade", String.class),
	
	/**
	 * The original label assigned to this node.
	 */
	OT_ORIGINAL_LABEL ("ot:originalLabel", String.class),	

	/**
	 * The ott id associated with the node. A property of tip nodes. Should not be set if the node has not been mapped.
	 */
	OT_OTT_ID ("ot:ottolid", Long.class), // TODO: will change to ot:ottId, change here when necessary
	
	/**
	 * The ottol name associated with this taxon
	 */
	OT_OTT_TAXON_NAME ("ot:ottTaxonName", String.class),
	
	/**
	 * The citation string for published studies. A property of source meta nodes.
	 */
    OT_PUBLICATION_REFERENCE ("ot:studyPublicationReference", String.class),

    /**
     * The phylografter study id. A property of source meta nodes.
     */
    OT_STUDY_ID ("ot:studyId", String.class),
    
    /**
     * A URI (or other identifier) for published studies. A property of source meta nodes.
     */
    OT_STUDY_PUBLICATION("ot:studyPublication", String.class),
    
    /**
     * The tag field exported from phylografter. A property of source meta nodes.
     */
    OT_TAG ("ot:tag", String.class),
    
    /**
     * The year the study was published. A property of source meta nodes.
     */
    OT_YEAR ("ot:studyYear", int.class);
	
	public final String name; // the property name. Used to identify this property in indexes and on nodes
	public final Class<?> type; // indicates the datatype for this property
    
    NodeProperty(String name, Class<?> type) {
    	this.name = name;
        this.type = type;
    }
}
