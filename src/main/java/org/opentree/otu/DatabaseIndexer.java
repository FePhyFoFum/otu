package org.opentree.otu;

import org.opentree.graphdb.GraphDatabaseAgent;
import org.opentree.otu.constants.OTUConstants;
import org.opentree.otu.constants.OTUNodeProperty;
import org.opentree.otu.constants.OTURelType;
import org.opentree.otu.constants.SearchableProperty;
import org.opentree.properties.OTVocabularyPredicate;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;

public class DatabaseIndexer extends OTUDatabase {

	// tree root indexes
	public final Index<Node> treeRootNodesByTreeId = getNodeIndex(OTUNodeIndex.TREE_ROOT_NODES_BY_TREE_ID);
	public final Index<Node> treeRootNodesBySourceId = getNodeIndex(OTUNodeIndex.TREE_ROOT_NODES_BY_SOURCE_ID);
	public final Index<Node> treeRootNodesByOtherProperty = getNodeIndex(OTUNodeIndex.TREE_ROOT_NODES_BY_OTHER_PROPERTY);
	
	public final Index<Node> treeRootNodesByOriginalTaxonName = getNodeIndex(OTUNodeIndex.TREE_ROOT_NODES_BY_ORIGINAL_TAXON_NAME);
	public final Index<Node> treeRootNodesByMappedTaxonName = getNodeIndex(OTUNodeIndex.TREE_ROOT_NODES_BY_MAPPED_TAXON_NAME);
	public final Index<Node> treeRootNodesByMappedTaxonNameNoSpaces = getNodeIndex(OTUNodeIndex.TREE_ROOT_NODES_BY_MAPPED_TAXON_NAME_WHITESPACE_FILLED);
	public final Index<Node> treeRootNodesByMappedTaxonOTTId = getNodeIndex(OTUNodeIndex.TREE_ROOT_NODES_BY_MAPPED_TAXON_OTT_ID);

	// source meta indexes
	public final Index<Node> sourceMetaNodesBySourceId = getNodeIndex(OTUNodeIndex.SOURCE_METADATA_NODES_BY_SOURCE_ID);
	public final Index<Node> sourceMetaNodesByOtherProperty = getNodeIndex(OTUNodeIndex.SOURCE_METADATA_NODES_BY_OTHER_PROPERTY);
	
	// ===== constructors
	
	public DatabaseIndexer(GraphDatabaseAgent gdba) {
		super(gdba);
	}
	
	public DatabaseIndexer(GraphDatabaseService gdbs) {
		super(gdbs);
	}

	// ===== indexing source metadata nodes
	
	/**
	 * Generalized method for adding source metadata nodes to indexes. This method uses properties stored in
	 * the graph during study import, and thus should be called *after* a study has been added to the graph.
	 * 
	 * requires the study to 
	 * @param sourceMetaNode
	 * @param property
	 */
	public void addSourceMetaNodeToIndexes(Node sourceMetaNode) {
		sourceMetaNodesBySourceId.add(sourceMetaNode,
				(String) sourceMetaNode.getProperty(OTUNodeProperty.LOCATION.propertyName())+OTUConstants.SOURCE_ID_SUFFIX,
				sourceMetaNode.getProperty(OTUNodeProperty.SOURCE_ID.propertyName()));
		indexNodeBySearchableProperties(sourceMetaNode, OTUConstants.SOURCE_PROPERTIES_FOR_SIMPLE_INDEXING);
	}

	/**
	 * Remove the indicated node from all source metadata node indexes.
	 */
	public void removeSourceMetaNodeFromIndexes(Node sourceMetaNode) {
		sourceMetaNodesBySourceId.remove(sourceMetaNode);
		sourceMetaNodesByOtherProperty.remove(sourceMetaNode);
	}
		
	// ===== indexing tree root nodes

	/**
	 * Install the indicated tree root node into the indexes. Uses graph traversals and node properties set during study
	 * import, and thus should be called *after* the study has been added to the graph.
	 * 
	 * @param treeRootNode
	 */
	public void addTreeRootNodeToIndexes(Node treeRootNode) {

		treeRootNodesByTreeId.add(treeRootNode,
				(String) treeRootNode.getProperty(OTUNodeProperty.LOCATION.propertyName()) + OTUConstants.TREE_ID_SUFFIX,
				treeRootNode.getProperty(OTUNodeProperty.TREE_ID.propertyName()));
		
		treeRootNodesBySourceId.add(treeRootNode,
				(String) treeRootNode.getProperty(OTUNodeProperty.LOCATION.propertyName()) + OTUConstants.SOURCE_ID_SUFFIX,
				treeRootNode.getSingleRelationship(OTURelType.METADATAFOR, Direction.INCOMING)
					.getEndNode().getProperty(OTUNodeProperty.SOURCE_ID.propertyName()));
		
		// add to property indexes
		indexNodeBySearchableProperties(treeRootNode, OTUConstants.TREE_PROPERTIES_FOR_SIMPLE_INDEXING);

		// add to taxonomy indexes
		addTreeToTaxonomicIndexes(treeRootNode);
	}
	
	/**
	 * Remove the indicated node from the tree root node indexes.
	 *
	 * @param treeRootNode
	 */
	public void removeTreeRootNodeFromIndexes(Node treeRootNode) {
		treeRootNodesByTreeId.remove(treeRootNode);
		treeRootNodesBySourceId.remove(treeRootNode);
		treeRootNodesByOtherProperty.remove(treeRootNode);
		treeRootNodesByMappedTaxonName.remove(treeRootNode);
		treeRootNodesByMappedTaxonNameNoSpaces.remove(treeRootNode);
		treeRootNodesByMappedTaxonOTTId.remove(treeRootNode);
	}
	
	// === private methods used during tree root indexing
	
	/**
	 * Add the tree to the taxonomic indexes
	 * @param treeRootNode
	 */
	private void addTreeToTaxonomicIndexes(Node root) {
		
		addStringArrayEntriesToIndex(root,
				treeRootNodesByOriginalTaxonName,
				OTUNodeProperty.DESCENDANT_ORIGINAL_TAXON_NAMES.propertyName(),
				OTVocabularyPredicate.OT_ORIGINAL_LABEL.propertyName());

		addStringArrayEntriesToIndex(root,
				treeRootNodesByMappedTaxonName,
				OTUNodeProperty.DESCENDANT_MAPPED_TAXON_NAMES.propertyName(),
				OTVocabularyPredicate.OT_OTT_TAXON_NAME.propertyName());
		
		addStringArrayEntriesToIndex(root,
				treeRootNodesByMappedTaxonNameNoSpaces,
				OTUNodeProperty.DESCENDANT_MAPPED_TAXON_NAMES_WHITESPACE_FILLED.propertyName(),
				OTVocabularyPredicate.OT_OTT_TAXON_NAME.propertyName());
		
		addLongArrayEntriesToIndex(root,
				treeRootNodesByMappedTaxonOTTId,
				OTUNodeProperty.DESCENDANT_MAPPED_TAXON_OTT_IDS.propertyName(),
				OTVocabularyPredicate.OT_OTT_ID.propertyName());
	}
	
	// ===== generalized private methods used during indexing

	/**
	 * Index a node into the supplied index under all the specified properties.
	 * @param node
	 * @param index
	 */
	private void indexNodeBySearchableProperties(Node node, SearchableProperty[] searchablePoperties) {
		for (SearchableProperty search : searchablePoperties) {
			Index<Node> index = getNodeIndex(search.index);
			if (node.hasProperty(search.property.propertyName())) {
				index.add(node, search.property.propertyName(), node.getProperty(search.property.propertyName()));
			}
		}
	}
	
	private void addStringArrayEntriesToIndex(Node node, Index<Node> index, String nodePropertyName, String indexProperty) {
		if (node.hasProperty(nodePropertyName)) {
			String[] array = (String[]) node.getProperty(nodePropertyName);
			for (int i = 0; i < array.length; i++) {
				index.add(node, indexProperty, array[i]);
			}
		}
	}

	private void addLongArrayEntriesToIndex(Node node, Index<Node> index, String nodePropertyName, String indexProperty) {
		if (node.hasProperty(nodePropertyName)) {
			long[] array = (long[]) node.getProperty(nodePropertyName);
			for (int i = 0; i < array.length; i++) {
				index.add(node, indexProperty, array[i]);
			}
		}
	}
}
