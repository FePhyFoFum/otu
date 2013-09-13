package opentree.otu;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jade.tree.JadeNode;
import jade.tree.JadeTree;
import jade.tree.NexsonSource;
import opentree.otu.GeneralUtils;
import opentree.otu.constants.BasicType;
import opentree.otu.constants.OTUConstants;
import opentree.otu.constants.GraphProperty;
import opentree.otu.constants.NodeProperty;
import opentree.otu.constants.RelType;
import opentree.otu.exceptions.DuplicateSourceException;
import opentree.otu.exceptions.NoSuchTreeException;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.kernel.Traversal;

public class DatabaseManager extends DatabaseAbstractBase {

	private DatabaseIndexer indexer;
	private DatabaseBrowser browser;
	
	private HashSet<String> knownRemotes;
	private Node lastObservedIngroupStartNode = null;
	
	protected Index<Node> sourceMetaNodesBySourceId = getNodeIndex(NodeIndexDescription.SOURCE_METADATA_NODES_BY_SOURCE_ID);
	protected Index<Node> treeRootNodesByTreeId = getNodeIndex(NodeIndexDescription.TREE_ROOT_NODES_BY_TREE_ID);

	// ===== constructors

	/**
	 * Access the graph db through the given service object.
	 * 
	 * @param graphService
	 */
	public DatabaseManager(GraphDatabaseService graphService) {
		super(graphService);
		indexer = new DatabaseIndexer(graphDb);
		browser = new DatabaseBrowser(graphDb);
		updateKnownRemotesInternal();
	}

	/**
	 * Access the graph db through the given embedded db object.
	 * 
	 * @param embeddedGraph
	 */
	public DatabaseManager(EmbeddedGraphDatabase embeddedGraph) {
		super(embeddedGraph);
		indexer = new DatabaseIndexer(graphDb);
		browser = new DatabaseBrowser(graphDb);
		updateKnownRemotesInternal();
	}

	/**
	 * Open the graph db through the given agent object.
	 * 
	 * @param gdb
	 */
	public DatabaseManager(GraphDatabaseAgent gdb) {
		super(gdb);
		indexer = new DatabaseIndexer(graphDb);
		browser = new DatabaseBrowser(graphDb);
		updateKnownRemotesInternal();
	}

	// ========== public methods
	
	// ===== adding sources and trees
	
	/**
	 * Install a study into the db, including loading all included trees.
	 * 
	 * @param source
	 * 		A NexsonSource object that contains the source metadata and trees
	 * 
	 * @param location
	 * 		Used to indicate remote vs local studies. To recognize a study as local, pass the location
	 * 		string in DatabaseManager.LOCAL_LOCATION. Using any other value for the location will result in this study
	 * 		being treated as a remote study.
	 * 
	 * @return
	 * 		The source metadata node for the newly added study
	 * @throws DuplicateSourceException 
	 */
	public Node addSource(NexsonSource source, String location) throws DuplicateSourceException {
		return addSource(source, location, false);
	}
	
	/**
	 * Install a study into the db, including loading all included trees.
	 * 
	 * @param source
	 * 		A NexsonSource object that contains the source metadata and trees.
	 * 
	 * @param location
	 * 		Used to indicate remote vs local studies. To recognize a study as local, pass the location
	 * 		string in DatabaseManager.LOCAL_LOCATION. Using any other value for the location will result in this study
	 * 		being treated as a remote study.
	 * 
	 * @param overwrite
	 * 		Pass a value of true to cause any preexisting studies with this location and source id to be deleted and replaced
	 * 		by this source. Otherwise the method will throw an exception if there are preexisting studies.
	 * 
	 * @return
	 * 		The source metadata node for the newly added study
	 * @throws DuplicateSourceException 
	 */
	public Node addSource(NexsonSource source, String location, boolean overwrite) throws DuplicateSourceException {
		
		// TODO: return meaningful information about the result to the rest query that calls this method

		Node sourceMeta = null;
		
		Transaction tx = graphDb.beginTx();
		try {
			
			String sourceId = source.getId();

			// don't add a study if it already exists, unless overwriting is turned on
			String property = location + OTUConstants.SOURCE_ID;
			sourceMeta = DatabaseUtils.getSingleNodeIndexHit(sourceMetaNodesBySourceId, property, sourceId);
			if (sourceMeta != null) {
				if (overwrite) {
					deleteSource(sourceMeta);
				} else {
					throw new DuplicateSourceException("Attempt to add a source with the same source id as an "
							+ "existing local source. This would require merging, but merging is not (yet?) supported.");
				}
			}
			
			// create the source
			sourceMeta = graphDb.createNode();
			sourceMeta.setProperty(NodeProperty.LOCATION.name, location);
			sourceMeta.setProperty(NodeProperty.SOURCE_ID.name, sourceId);
			
			// set source properties
			setNodePropertiesFromMap(sourceMeta, source.getProperties());

			// add the trees
			boolean noValidTrees = true;
			int i = 0;
			Iterator<JadeTree> treesIter = source.getTrees().iterator();
			while (treesIter.hasNext()) {

				JadeTree tree = treesIter.next();

				// TODO: sometimes the nexson reader returns null trees. this is a hack to deal with that.
				// really we should fix the nexson reader so it doesn't return null trees
				if (tree == null) {
					continue;
				} else if (noValidTrees == true) {
					noValidTrees = false;
				}

				// get the tree id from the nexson if there is one or create an arbitrary one if not
				String treeIdSuffix = (String) tree.getObject("id");
				if (treeIdSuffix ==  null) {
					treeIdSuffix = OTUConstants.LOCAL_TREEID_PREFIX + /* .value + */ String.valueOf(i);
				}
				
				// create a unique tree id by including the study id, this is the convention from treemachine
				String treeId = sourceId + "_" + treeIdSuffix;

				// add the tree
				addTree(tree, treeId, sourceMeta);

				i++;
			}
			
			if (location == LOCAL_LOCATION) { // if this is a local study then attach it to any existing remotes
				for (Node sourceMetaHit : browser.getRemoteSourceMetaNodesForSourceId(sourceId)) {
					if (sourceMetaHit.getProperty(NodeProperty.LOCATION.name).equals(LOCAL_LOCATION) == false) {
						sourceMeta.createRelationshipTo(sourceMetaHit, RelType.LOCALCOPYOF);
					}
				}

			} else { // remote study

				// check if there is a local study to attach this remote one to
				Node localSourceMeta = DatabaseUtils.getSingleNodeIndexHit(sourceMetaNodesBySourceId, LOCAL_LOCATION + OTUConstants.SOURCE_ID, sourceId);
				if (localSourceMeta != null) {
					localSourceMeta.createRelationshipTo(sourceMeta, RelType.LOCALCOPYOF);
				}
				
				// add the remote location if necessary
				if (!knownRemotes.contains(location)) {
					addKnownRemote(location);
				}
			}
		
			indexer.addSourceMetaNodeToIndexes(sourceMeta);
			
			tx.success();
		} finally {
			tx.finish();
		}
		
		return sourceMeta;
	}
	
	/**
	 * Adds a tree in a JadeTree format into the database under the specified study.
	 * 
	 * @param tree
	 * 		A JadeTree object containing the tree to be added
	 * @param treeId
	 * 		The id string to use for this tree. Will be used in indexing so must be unique across all trees in the database
	 * @param sourceMetaNode
	 * 		The source metadata node for the source that this tree will be added to
	 * @return
	 * 		The root node for the added tree.
	 */
	public Node addTree(JadeTree tree, String treeId, Node sourceMetaNode) {

		// get the location from the source meta node
		String location = (String) sourceMetaNode.getProperty(NodeProperty.LOCATION.name);
		String sourceId = (String) sourceMetaNode.getProperty(NodeProperty.SOURCE_ID.name);

		// add the tree to the graph; only add tree structure if this is a local tree
		Node root = null;
		if (location.equals(LOCAL_LOCATION)) {
			root = preorderAddTreeToDB(tree.getRoot(), null);
			// designate the ingroup if we found one, and then reset the variable!
			if (lastObservedIngroupStartNode != null) {
				designateIngroup(lastObservedIngroupStartNode);
				lastObservedIngroupStartNode = null;
			}
		} else {
			root = graphDb.createNode();
		}

		// attach to source and set the id information
		sourceMetaNode.createRelationshipTo(root, RelType.METADATAFOR);
		root.setProperty(NodeProperty.LOCATION.name, location);
		root.setProperty(NodeProperty.SOURCE_ID.name, sourceId);
		
		// designate the root as the ingroup this is specified in the tree properties (e.g. from a nexson)
//		if (tree.getRoot().getObject(NodeProperty.IS_INGROUP.name) != null) {
//			designateIngroup(root);
//		}

		// add node properties
		root.setProperty(NodeProperty.TREE_ID.name, treeId);
		root.setProperty(NodeProperty.IS_ROOT.name, true);
		setNodePropertiesFromMap(root, tree.getAssoc());

		collectTipTaxonArrayProperties(root, tree);

		indexer.addTreeRootNodeToIndexes(root);
		
		return root;
	}
	
	// ===== delete methods

	/**
	 * Deletes a local tree
	 * @param treeId
	 */
	public void deleteTree(Node root) {

		Transaction tx = graphDb.beginTx();
		try {

			// clean up the tree indexes
			indexer.removeTreeRootNodeFromIndexes(root);

			// collect the tree nodes
			HashSet<Node> todelete = new HashSet<Node>();
			TraversalDescription CHILDOF_TRAVERSAL = Traversal.description().relationships(RelType.CHILDOF, Direction.INCOMING);
			todelete.add(root);
			for (Node curGraphNode : CHILDOF_TRAVERSAL.breadthFirst().traverse(root).nodes()) {
				todelete.add(curGraphNode);
			}
			
			// remove them
			for (Node nd : todelete) {
				for (Relationship rel : nd.getRelationships()) {
					rel.delete();
				}
				nd.delete();
			}
			
			// delete the tree root
			treeRootNodesByTreeId.remove(root);
			
			tx.success();

		} finally {
			tx.finish();
		}
	}

	/**
	 * Remove a local source and all its trees.
	 * @param sourceId
	 * @throws NoSuchTreeException 
	 */
	public void deleteSource(Node sourceMeta) {
		
		Transaction tx = graphDb.beginTx();
		try {

			// clean up the source indexes
			indexer.removeSourceMetaNodeFromIndexes(sourceMeta);

			// remove all trees
			for (Relationship rel : sourceMeta.getRelationships(RelType.METADATAFOR, Direction.OUTGOING)) {
				deleteTree(rel.getEndNode()); // will also remove the METADATAFOR rels pointing at this metadata node
			}

			// delete remaining relationships
			for (Relationship rel : sourceMeta.getRelationships()) {
				rel.delete();
			}
			
			// delete the source meta node itself
			sourceMeta.delete();			
			
			tx.success();
			
		} finally {
			tx.finish();
		}
	}
	
	// ===== other methods
	
	/**
	 * Set properties on a node according to the passed in maps
	 * @param keys
	 * @param values
	 * @param types
	 */
	public void setProperties(Node node, String[] keys, String[] values, String[] types) {
		
		Transaction tx = graphDb.beginTx();
		try {

			int i = 0;
			BasicType t;
			
			for (String key : keys) {
				try {
					t = BasicType.valueOf(types[i].toUpperCase().trim());
				} catch (IllegalArgumentException ex) {
					tx.failure();
					throw new IllegalArgumentException("The type " + types[i] + " is not valid property type.");
				}

				if (t.type == Double.class) {
					node.setProperty(key, Double.valueOf(values[i++]));

				} else if (t.type == Integer.class) {
					node.setProperty(key, Integer.valueOf(values[i++]));
				
				} else if (t.type == String.class) {
					node.setProperty(key, String.valueOf(values[i++]));

				} else if (t.type == Boolean.class) {
					node.setProperty(key, Boolean.valueOf(values[i++]));
				}
			}
			tx.success();
		} catch (ArrayIndexOutOfBoundsException ex) {
			tx.failure();
			throw new IllegalArgumentException("All the input arrays must be the same length.");
		} finally {
			tx.finish();
		}
	}
	
	/**
	 * Reroot the tree containing the `newroot` node on that node. Returns the root node of the rerooted tree.
	 * @param newroot
	 * @return
	 */
	public Node rerootTree(Node newroot) {
		
		// first get the root of the old tree
		Node oldRoot = DatabaseUtils.getRootOfTreeContaining(newroot);
		
		// not rerooting
		if (oldRoot == newroot) {
			Transaction tx1 = graphDb.beginTx();
			try {
				oldRoot.setProperty(NodeProperty.ROOTING_IS_SET.name, true);
				tx1.success();
			} finally {
				tx1.finish();
			}
			return oldRoot;
		}
		Node actualRoot = null;
		String treeID = null;
		treeID = (String) oldRoot.getProperty(NodeProperty.TREE_ID.name);
		Transaction tx = graphDb.beginTx();
		try {
			// tritomy the root
			int oldrootchildcount = DatabaseUtils.getNumberOfRelationships(oldRoot, RelType.CHILDOF, Direction.INCOMING);
					
			if (oldrootchildcount == 2) {
				boolean retvalue = tritomyRoot(oldRoot, newroot);
				if (retvalue == false) {
					tx.success();
					tx.finish();
					return oldRoot;
				}
			}
			
			// process the reroot
			actualRoot = graphDb.createNode();

			Relationship nrprel = newroot.getSingleRelationship(RelType.CHILDOF, Direction.OUTGOING);
			Node tempParent = nrprel.getEndNode();
			actualRoot.createRelationshipTo(tempParent, RelType.CHILDOF);
			nrprel.delete();
			newroot.createRelationshipTo(actualRoot, RelType.CHILDOF);
			processRerootRecursive(actualRoot);

			// switch the METADATAFOR relationship to the new root node
			Relationship prevStudyToTreeRootLinkRel = oldRoot.getSingleRelationship(RelType.METADATAFOR, Direction.INCOMING);
			Node metadata = prevStudyToTreeRootLinkRel.getStartNode();
			prevStudyToTreeRootLinkRel.delete();
		
			actualRoot.setProperty(NodeProperty.TREE_ID.name, treeID);

			metadata.createRelationshipTo(actualRoot, RelType.METADATAFOR);
			
			// clean up properties
			DatabaseUtils.exchangeAllProperties(oldRoot, actualRoot); // TODO: are there properties we don't want to exchange?
			
			// update indexes
			indexer.removeTreeRootNodeFromIndexes(oldRoot);
			indexer.addTreeRootNodeToIndexes(actualRoot);
			
			// reset the ingroup
			actualRoot.setProperty(NodeProperty.INGROUP_IS_SET.name, false);
			actualRoot.removeProperty(NodeProperty.INGROUP_START_NODE_ID.name);
			for (Node child : Traversal.description().relationships(RelType.CHILDOF, Direction.INCOMING).traverse(actualRoot).nodes()) {
				child.removeProperty(NodeProperty.IS_WITHIN_INGROUP.name);
				child.removeProperty(NodeProperty.IS_INGROUP_ROOT.name);
			}

			tx.success();
		} finally {
			tx.finish();
		}
		
		return actualRoot;
	}
	
	/**
	 * Set the ingroup for the tree containing `innode` to `innode`.
	 * @param innode
	 */
	public void designateIngroup(Node innode) {

		// first get the root of the old tree
		Node root = DatabaseUtils.getRootOfTreeContaining(innode);

		TraversalDescription CHILDOF_TRAVERSAL = Traversal.description().relationships(RelType.CHILDOF, Direction.INCOMING);
		Transaction tx = graphDb.beginTx();
		try {
			root.setProperty(NodeProperty.INGROUP_IS_SET.name, true);
			if (root != innode) {
				for (Node node : CHILDOF_TRAVERSAL.breadthFirst().traverse(root).nodes()) {
					if (node.hasProperty(NodeProperty.IS_WITHIN_INGROUP.name))
						node.removeProperty(NodeProperty.IS_WITHIN_INGROUP.name);
				}
			}
			innode.setProperty(NodeProperty.IS_WITHIN_INGROUP.name, true);
			for (Node node : CHILDOF_TRAVERSAL.breadthFirst().traverse(innode).nodes()) {
				node.setProperty(NodeProperty.IS_WITHIN_INGROUP.name, true);
			}
			root.setProperty(NodeProperty.INGROUP_IS_SET.name, true);
			root.setProperty(NodeProperty.INGROUP_START_NODE_ID.name, innode.getId());
			tx.success();
		} finally {
			tx.finish();
		}		
	}
	
	// ========== private methods
	
	/**
	 * Add a known remote to the graph property for known remotes, which is a primitive string array. We
	 * could also just add nodes for all remotes and index them
	 * @param remote
	 */
	private void addKnownRemote(String newRemote) {
		
		List<String> knownRemotesPrev = browser.getKnownRemotes();
		String[] knownRemotesNew = new String[knownRemotesPrev.size()+1];
		
		int i = 0;
		for (String r : knownRemotesPrev) {
			knownRemotesNew[i++] = r;
		}

		knownRemotesNew[i] = newRemote;
		graphDb.getNodeById((long)0).setProperty(GraphProperty.KNOWN_REMOTES.propertyName, knownRemotesNew);
		
		updateKnownRemotesInternal();
	}
	
	/**
	 * Just update the internal cache of known remotes. Called when we add a remote and also during construction.
	 * We keep this cached so we don't have to check the graph property array every time we add a source.
	 */
	private void updateKnownRemotesInternal() {
		knownRemotes = new HashSet<String>();
		for (String remote : browser.getKnownRemotes()) {
			knownRemotes.add(remote);
		}
	}
	
	/**
	 * A recursive function used to replicate the tree JadeNode structure below the passed in JadeNode in the graph.
	 * @param curJadeNode
	 * @param parentGraphNode
	 * @return
	 */
	private Node preorderAddTreeToDB(JadeNode curJadeNode, Node parentGraphNode) {

		Node curGraphNode = graphDb.createNode();

		// remember the ingroup if we hit one
		if (curJadeNode.hasAssocObject(NodeProperty.IS_INGROUP_ROOT.name) == true) {
//			withinIngroup = true;
			curGraphNode.setProperty(NodeProperty.INGROUP_START_NODE_ID.name, true);
			lastObservedIngroupStartNode = curGraphNode;
		}
		
		// set the ingroup flag if we're within the ingroup
//		if (withinIngroup) {
//			curGraphNode.setProperty(NodeProperty.IS_WITHIN_INGROUP.name, true);
//		}
		
		// add properties
		if (curJadeNode.getName() != null) {
			curGraphNode.setProperty(NodeProperty.NAME.name, curJadeNode.getName());
			setNodePropertiesFromMap(curGraphNode, curJadeNode.getAssoc()); // why not?
		}

		// TODO: add bl
		// dbnode.setProperty("bl", innode.getBL());
		// TODO: add support
		
		if (parentGraphNode != null) {
			curGraphNode.createRelationshipTo(parentGraphNode, RelType.CHILDOF);
		}

		for (JadeNode childJadeNode : curJadeNode.getChildren()) {
			preorderAddTreeToDB(childJadeNode, curGraphNode);
		}

		return curGraphNode;
	}
	
	/**
	 * Import entries from a map into the database as properties of the specified node.
	 * @param node
	 * @param properties
	 */
	private static void setNodePropertiesFromMap(Node node, Map<String, Object> properties) {
		for (Entry<String, Object> property : properties.entrySet()) {
			node.setProperty(property.getKey(), property.getValue());
		}
	}
	
	/**
	 * Collects taxonomic names and ids for all the tips of the provided JadeTree and stores this info as node properties
	 * of the provided graph node. Used to store taxonomic mapping info for the root nodes of trees in the graph.
	 * @param node
	 * @param tree
	 */
	private void collectTipTaxonArrayProperties(Node node, JadeTree tree) {
		
		List<String> originalTaxonNames = new ArrayList<String>();
		List<String> mappedTaxonNames = new ArrayList<String>();
		List<String> mappedTaxonNamesNoSpaces = new ArrayList<String>();
		List<Long> mappedOTTIds = new ArrayList<Long>();

		for (JadeNode treeNode : tree.getRoot().getDescendantLeaves()) {

			originalTaxonNames.add((String) treeNode.getObject(NodeProperty.OT_ORIGINAL_LABEL.name));

			String name = treeNode.getName(); // TODO: make sure we aren't setting these to original taxon names.
			// If the node has not been explicitly mapped, then this should be null.

			mappedTaxonNames.add(name);
			mappedTaxonNamesNoSpaces.add(name.replace("\\s+", OTUConstants.WHITESPACE_SUBSTITUTE_FOR_SEARCH /*.value*/));

			Long ottId = (Long) treeNode.getObject(NodeProperty.OT_OTT_ID.name);
			if (ottId != null) {
				mappedOTTIds.add(ottId);
			}
		}

		// store the properties in the nodes
		node.setProperty(NodeProperty.DESCENDANT_ORIGINAL_TAXON_NAMES.name, GeneralUtils.convertToStringArray(originalTaxonNames));
		node.setProperty(NodeProperty.DESCENDANT_MAPPED_TAXON_NAMES.name, GeneralUtils.convertToStringArray(mappedTaxonNames));
		node.setProperty(NodeProperty.DESCENDANT_MAPPED_TAXON_NAMES_WHITESPACE_FILLED.name, GeneralUtils.convertToStringArray(mappedTaxonNamesNoSpaces));
		node.setProperty(NodeProperty.DESCENDANT_MAPPED_TAXON_OTT_IDS.name, GeneralUtils.convertToLongArray(mappedOTTIds));
	}

	/**
	 * Used by the rerooting function
	 * @param oldRoot
	 * @param newRoot
	 * @return
	 */
	private boolean tritomyRoot(Node oldRoot, Node newRoot) {
		Node thisNode = null;// this will be the node that is sunk
		// find the first child that is not a tip
		for (Relationship rel : oldRoot.getRelationships(RelType.CHILDOF, Direction.INCOMING)) {
			Node tnode = rel.getStartNode();
			if (tnode.hasRelationship(Direction.INCOMING, RelType.CHILDOF) && tnode.getId() != newRoot.getId()) {
				thisNode = tnode;
				break;
			}
		}
		if (thisNode == null) {
			return false;
		}
		for (Relationship rel : thisNode.getRelationships(RelType.CHILDOF, Direction.INCOMING)) {
			Node eNode = rel.getStartNode();
			eNode.createRelationshipTo(oldRoot, RelType.CHILDOF);
			rel.delete();
		}
		thisNode.getSingleRelationship(RelType.CHILDOF, Direction.OUTGOING).delete();
		thisNode.delete();
		return true;
	}

	/**
	 * Recursive function to process a re-rooted tree to fix relationship direction, etc.
	 * @param innode
	 */
	private void processRerootRecursive(Node innode) {
		if (innode.hasProperty(NodeProperty.IS_ROOT.name) || innode.hasRelationship(Direction.INCOMING, RelType.CHILDOF) == false) {
			return;
		}
		Node parent = null;
		if (innode.hasRelationship(Direction.OUTGOING, RelType.CHILDOF)) {
			parent = innode.getSingleRelationship(RelType.CHILDOF, Direction.OUTGOING).getEndNode();
			processRerootRecursive(parent);
		}

		DatabaseUtils.exchangeNodeProperty(parent, innode, NodeProperty.NAME.name);

		// Rearrange topology
		innode.getSingleRelationship(RelType.CHILDOF, Direction.OUTGOING).delete();
		parent.createRelationshipTo(innode, RelType.CHILDOF);
	}

}
