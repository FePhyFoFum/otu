package org.opentree.otu.constants;

import org.opentree.graphdb.NodeIndexDescription;
import org.opentree.otu.OTUNodeIndex;
import org.opentree.properties.OTProperty;
import org.opentree.properties.OTVocabulary;

/**
 * An enum containing mappings identifying node properties and the indexes for which they are searchable.
 */
public enum SearchableProperty {

	// ===== source meta nodes
	
	CURATOR_NAME(
			"ot curator name",
			OTVocabulary.OT_CURATOR_NAME,
			OTUNodeIndex.SOURCE_METADATA_NODES_BY_OTHER_PROPERTY),
			
    DATA_DEPOSIT(
    		"ot data deposit",
    		OTVocabulary.OT_DATA_DEPOSIT,
    		OTUNodeIndex.SOURCE_METADATA_NODES_BY_OTHER_PROPERTY),
    		
	PUBLICATION_REFERENCE (
			"text citation (ot pub ref)",
			OTVocabulary.OT_PUBLICATION_REFERENCE,
			OTUNodeIndex.SOURCE_METADATA_NODES_BY_OTHER_PROPERTY),
			
	SOURCE_ID (
			"source id",
			OTUNodeProperty.SOURCE_ID,
			OTUNodeIndex.SOURCE_METADATA_NODES_BY_SOURCE_ID),
			
	STUDY_PUBLICATION (
			"ot study pub",
			OTVocabulary.OT_STUDY_PUBLICATION,
			OTUNodeIndex.SOURCE_METADATA_NODES_BY_OTHER_PROPERTY),

	YEAR (
			"ot year",
			OTVocabulary.OT_YEAR,
			OTUNodeIndex.SOURCE_METADATA_NODES_BY_OTHER_PROPERTY),
    
	TAG_SOURCE (
			"ot tag (sources)",
			OTVocabulary.OT_TAG,
			OTUNodeIndex.SOURCE_METADATA_NODES_BY_OTHER_PROPERTY),
			
    // ===== tree root nodes

	DESCENDANT_ORIGINAL_TAXON_NAMES (
    		"original taxon name",
    		OTVocabulary.OT_ORIGINAL_LABEL,
    		OTUNodeIndex.TREE_ROOT_NODES_BY_ORIGINAL_TAXON_NAME),
    		
    DESCENDANT_MAPPED_TAXON_NAMES (
    		"current taxon name (mapped?)",
    		OTUNodeProperty.NAME,
    		OTUNodeIndex.TREE_ROOT_NODES_BY_MAPPED_TAXON_NAME),

    DESCENDANT_MAPPED_TAXON_OTT_IDS (
    		"ott id",
    		OTVocabulary.OT_OTT_ID,
    		OTUNodeIndex.TREE_ROOT_NODES_BY_MAPPED_TAXON_OTT_ID),
    
	BRANCH_LENGTH_MODE (
			"ot branch length mode",
			OTVocabulary.OT_BRANCH_LENGTH_MODE,
			OTUNodeIndex.TREE_ROOT_NODES_BY_OTHER_PROPERTY),
	
	TAG_TREE (
			"ot tag (trees)",
			OTVocabulary.OT_TAG,
			OTUNodeIndex.TREE_ROOT_NODES_BY_OTHER_PROPERTY);
    		
	public final String shortName;
    public final OTProperty property;
    public final NodeIndexDescription index;
    
    SearchableProperty(String shortName, OTProperty property, NodeIndexDescription indexDesc) {
    	this.shortName = shortName;
        this.property = property;
        this.index = indexDesc;
    }
}
