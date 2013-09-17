package org.opentree.otu;

import org.opentree.graphdb.NodeIndexDescription;

/**
 * An enum to make keeping track of indexes easier. Please document new indexes here.
 * 
 * @author federation of botanist hackers
 *
 */
public enum OTUNodeIndex implements NodeIndexDescription {

	// ===== tree indexes
	
	/**
     * Root nodes for both local and remote (i.e. not imported) trees, indexed by the tree ids. The first key is either:
     * "localTreeId" or "XTreeId" where X is the name of some remote (currently we just support one remote with the prefix
     * "remote"), and second key is the tree id string, which is the study id for the tree concatenated
     * via an underscore to either the incoming treeId from the original nexson, or an arbitrary local tree identifier string
     * if the incoming tree is missing (or if the tree is not from a nexson). Root nodes for trees that have been imported are
     * indexed under "localTreeId" and trees read from the remote repo are indexed under "remoteTreeId". To get a list of all
     * known trees, search this index on the key "*" (returns independent results for local and remote trees).
	 */
    TREE_ROOT_NODES_BY_TREE_ID ("treeRootNodesByTreeId"),
        
    /**
     * Root nodes for both local and remote (i.e. not imported) trees, indexed by their originating source id. The property name
     * is either "localSourceId" or "remoteSourceId", and the key is the source id. In the case of nexsons this is the study id,
     * but for other sources (e.g. a file of newick trees, it could be any string that is a globally unique identifier to this database.
     */
    TREE_ROOT_NODES_BY_SOURCE_ID ("treeRootNodesBySourceId"),

    /**
     * Root nodes for trees including a taxon with the supplied name. Property is "name", key is taxon name.
     */
    TREE_ROOT_NODES_BY_ORIGINAL_TAXON_NAME ("treeRootNodesByOriginalTaxonName"),

    /**
     * Root nodes for trees including a taxon with the supplied name. Property is "name", key is taxon name.
     */
    TREE_ROOT_NODES_BY_MAPPED_TAXON_NAME ("treeRootNodesByMappedTaxonName"),

    /**
     * Root nodes for trees including a taxon with the supplied name. Spaces have been replaced with underscores
     * to facilitate whole-word matching. Property is "name", key is taxon name.
     */
    TREE_ROOT_NODES_BY_MAPPED_TAXON_NAME_WHITESPACE_FILLED ("treeRootNodesByMappedTaxonNameWhitespaceFilled"),
    
    /**
     * Root nodes for trees including a taxon with the supplied ott id. Property is "uid", key is ott id.
     */
    TREE_ROOT_NODES_BY_MAPPED_TAXON_OTT_ID ("treeRootNodesByMappedTaxonMappedOTTId"),

    /**
     * Root nodes for trees indexed by the specified ot namespace property. Property is the ot property name (e.g. "ot:curatorName")
     * and key is the value for that property (e.g. "Romina Gazis").
     */
    TREE_ROOT_NODES_BY_OTHER_PROPERTY ("treeRootNodesByOTProperty"),
    
    // ===== source indexes

    /**
     * Study metadata nodes indexed by the specified ot namespace properties. Property is the ot property name (e.g. "ot:curatorName")
     * and key is the value for that property (e.g. "Bryan Drew").
     */
	SOURCE_METADATA_NODES_BY_OTHER_PROPERTY ("sourceMetaNodesByOTProperty"),

    /**
     * Source metadata nodes for both local and remote (i.e. not imported) sources, indexed by their originating source id.
     * Property is either "localSourceId" or "remoteSourceId", and key is the source id. In the case of nexsons this is study id,
     * but other cases (e.g. a file of newick trees uploaded locally), this could be any identifier string globally unique to the db.
     */
    SOURCE_METADATA_NODES_BY_SOURCE_ID ("sourceMetaNodesBySourceId");
    
    String name;
    
    OTUNodeIndex(String name) {
    	this.name = name;
    }
    
    @Override
    public String indexName() {
    	return name;
    }
}
