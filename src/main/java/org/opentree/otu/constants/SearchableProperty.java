package org.opentree.otu.constants;

import org.opentree.graphdb.NodeIndexDescription;
import org.opentree.otu.OTUNodeIndex;
import org.opentree.properties.OTPropertyPredicate;
import org.opentree.properties.OTVocabularyPredicate;

/**
 * An enum containing mappings identifying node properties and the indexes for which they are searchable.
 */
public enum SearchableProperty {

	// ===== source meta nodes
	
	CURATOR_NAME(
			"ot curator name",
			OTVocabularyPredicate.OT_CURATOR_NAME,
			OTUNodeIndex.SOURCE_METADATA_NODES_BY_OTHER_PROPERTY),
			
    DATA_DEPOSIT(
    		"ot data deposit",
    		OTVocabularyPredicate.OT_DATA_DEPOSIT,
    		OTUNodeIndex.SOURCE_METADATA_NODES_BY_OTHER_PROPERTY),
    		
	PUBLICATION_REFERENCE (
			"text citation (ot pub ref)",
			OTVocabularyPredicate.OT_PUBLICATION_REFERENCE,
			OTUNodeIndex.SOURCE_METADATA_NODES_BY_OTHER_PROPERTY),
			
	SOURCE_ID (
			"source id",
			OTUNodeProperty.SOURCE_ID,
			OTUNodeIndex.SOURCE_METADATA_NODES_BY_SOURCE_ID),
			
	STUDY_PUBLICATION (
			"ot study pub",
			OTVocabularyPredicate.OT_STUDY_PUBLICATION,
			OTUNodeIndex.SOURCE_METADATA_NODES_BY_OTHER_PROPERTY),

	YEAR (
			"ot year",
			OTVocabularyPredicate.OT_YEAR,
			OTUNodeIndex.SOURCE_METADATA_NODES_BY_OTHER_PROPERTY),
    
	TAG_SOURCE (
			"ot tag (sources)",
			OTVocabularyPredicate.OT_TAG,
			OTUNodeIndex.SOURCE_METADATA_NODES_BY_OTHER_PROPERTY),
			
    // ===== tree root nodes

	DESCENDANT_ORIGINAL_TAXON_NAMES (
    		"original taxon name",
    		OTVocabularyPredicate.OT_ORIGINAL_LABEL,
    		OTUNodeIndex.TREE_ROOT_NODES_BY_ORIGINAL_TAXON_NAME),
    		
/*    DESCENDANT_MAPPED_TAXON_NAMES (
    		"current taxon name (mapped?)",
    		OTUNodeProperty.NAME,
    		OTUNodeIndex.TREE_ROOT_NODES_BY_MAPPED_TAXON_NAME), */

    DESCENDANT_MAPPED_TAXON_NAMES (
    		"mapped ott taxon name",
    		OTVocabularyPredicate.OT_OTT_TAXON_NAME,
    		OTUNodeIndex.TREE_ROOT_NODES_BY_MAPPED_TAXON_NAME),
    		
    DESCENDANT_MAPPED_TAXON_OTT_IDS (
    		"ott id",
    		OTVocabularyPredicate.OT_OTT_ID,
    		OTUNodeIndex.TREE_ROOT_NODES_BY_MAPPED_TAXON_OTT_ID),
    
	BRANCH_LENGTH_MODE (
			"ot branch length mode",
			OTVocabularyPredicate.OT_BRANCH_LENGTH_MODE,
			OTUNodeIndex.TREE_ROOT_NODES_BY_OTHER_PROPERTY),
	
	TAG_TREE (
			"ot tag (trees)",
			OTVocabularyPredicate.OT_TAG,
			OTUNodeIndex.TREE_ROOT_NODES_BY_OTHER_PROPERTY);
    		
	public final String shortName;
    public final OTPropertyPredicate property;
    public final NodeIndexDescription index;
    
    SearchableProperty(String shortName, OTPropertyPredicate property, NodeIndexDescription indexDesc) {
    	this.shortName = shortName;
        this.property = property;
        this.index = indexDesc;
    }
}
