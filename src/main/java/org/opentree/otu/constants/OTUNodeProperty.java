package org.opentree.otu.constants;

import org.opentree.properties.OTPropertyPredicate;

/**
 * Node properties specific to OTU. These are stored in graph nodes. Different types
 * of nodes may have different properties.
 * 
 */
public enum OTUNodeProperty implements OTPropertyPredicate {
	
	// TODO: switch as many of these as possible over to the nexson and ot: vocabulary properties as possible. waiting for that spec to stabilize to do so

	// ===== source meta nodes
	
	/**
	 * A boolean to identify source metadata nodes.
	 */
	IS_SOURCE_META ("is_source_meta", boolean.class),
	
	// ===== source meta and tree root nodes

	/**
	 * The taxon name associated with this node. SHOULD BE THE MAPPED NAME, not the original. Should not be set if the
	 * node has not been mapped (although I have no idea if this is actually the case).
	 */
	NAME ("name", String.class), // TODO: clean this / switch it over to the standard ot properties when peter has changed the nexson output
	
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
	 * The id of the graph node. This is referenced when storing graph node ids in JadeNode objects for display in the browser.
	 */
	NODE_ID ("id", Long.class),
		
	// ===== tree root nodes

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
	FOCAL_CLADE ("focal_clade_ott_id", String.class), // TODO: make this use the ot:focalClade property instead. Need nexson reader to apply this
	
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
	 * The nodeid of the root node for the designated ingroup for this tree. Should not be set unless the ingroup has been
	 * designated.
	 */
	INGROUP_START_NODE_ID ("ingroup_node_id", boolean.class),
	
	/**
	 * A boolean indicating that this tree is a working copy, thus will not be compared to copies of this tree on the remote repo.
	 */
	IS_WORKING_COPY ("is_working_copy", boolean.class),
	
	/**
	 * A boolean indicating that this tree is a saved copy, thus will be compared to copies of this tree on the remote repo.
	 */
	IS_SAVED_COPY ("is_saved_copy", boolean.class),
	
	/**
	 * Whether or not this tree contains otu nodes that have been mapped to OTT taxa. Used for user feedback during TNRS.
	 */
	PROCESSED_BY_TNRS ("processed_by_tnrs", boolean.class),
	
	/**
	 * The name of the taxomachine context that has been determined for this tree.
	 */
	CONTEXT_NAME ("context_name", String.class),
	
	
	// ===== all tree nodes

	/**
	 * An id property used within nexson files. Relevance to OTU is unclear.
	 */
	NEXSON_ID ("nexsonid", String.class),

	/**
	 * An id property used by phylografter. We assign these values to incoming trees, and also hide these properties when returning metadata.
	 */
	PHYLOGRAFTER_ID ("phylografter_id", long.class),
	
	/**
	 * A flag specifying that the clade represented by the node is the ingroup for the tree. Is only set on the root node
	 * of the ingroup clade. A phylografter property imported by NexsonReader.
	 */
	IS_INGROUP_ROOT ("ingroup_start", boolean.class),

	/**
	 * A flag specifying that this node is part of the ingroup for this tree. This property is nominally a boolean but should
	 * only be set on nodes that are actually part of the ingroup, implying that nodes without this property in trees that
	 * have their ingroup set are thus part of the outgroup.
	 */
	IS_WITHIN_INGROUP ("within_ingroup", boolean.class);
	
	/*
	 * A property of OTU nodes only. For OTU nodes, this should be set to true. Any other node should never have this property.
	 *
	IS_OTU ("is_otu", boolean.class); */ // TODO: use the ot:isOTU property instead
	
	private final String propertyName;
	private final Class<?> type;
    
	OTUNodeProperty(String name, Class<?> type) {
    	this.propertyName = name;
        this.type = type;
    }

	public String propertyName() {
		return propertyName;
	}

	public Class<?> type() {
		return type;
	}
}
