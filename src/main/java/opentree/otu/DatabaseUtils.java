package opentree.otu;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import opentree.otu.constants.RelType;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.Evaluator;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.kernel.Traversal;

/**
 * Static methods for performing common tasks with the database.
 */
public class DatabaseUtils {
	
	public static final TraversalDescription DESCENDANT_OTU_TRAVERSAL = Traversal
			.description()
			.relationships(RelType.CHILDOF, Direction.INCOMING)
			.evaluator(new Evaluator() {
				@Override
				public Evaluation evaluate(Path inPath) {
					if (inPath.endNode().hasRelationship(RelType.CHILDOF, Direction.INCOMING)) {
						return Evaluation.EXCLUDE_AND_CONTINUE;
					} else {
						return Evaluation.INCLUDE_AND_PRUNE;
					}
				}
			});

	
	/**
	 * Count the number of relationships of the specified type and direction connected to the node
	 * @param node
	 * @param relType
	 * @param direction
	 * @return
	 * 		number of relationships
	 */
	public static int getNumberOfRelationships(Node node, RelType relType, Direction direction) {
		int relCount = 0;
		for (Relationship r : node.getRelationships(relType, direction)) {
			relCount++;
		}
		return relCount;
	}

	/**
	 * Count the number of relationships of the specified type connected to the node
	 * @param node
	 * @param relType
	 * @return
	 * 		number of relationships
	 */
	public static int getNumberOfRelationships(Node node, RelType relType) {
		int relCount = 0;
		for (Relationship r : node.getRelationships(relType)) {
			relCount++;
		}
		return relCount;
	}

	/**
	 * Count the number of relationships of the specified direction connected to the node
	 * @param node
	 * @param direction
	 * @return
	 * 		number of relationships
	 */
	public static int getNumberOfRelationships(Node node, Direction direction) {
		int relCount = 0;
		for (Relationship r : node.getRelationships(direction)) {
			relCount++;
		}
		return relCount;
	}
	
	/**
	 * Return the root node from the graph for the tree containing the specified node.
	 * @param node
	 * 		The node to start traversing from
	 * @return
	 */
	public static Node getRootOfTreeContaining(Node node) {
		Node root = node;
		boolean going = true;
		while (going) {
			if (root.hasRelationship(RelType.CHILDOF, Direction.OUTGOING)) {
				root = root.getSingleRelationship(RelType.CHILDOF, Direction.OUTGOING).getEndNode();
			} else {
				break;
			}
		}
		return root;
	}
	
	/**
	 * A convenience wrapper for querying node indexes that validates a unique result. Returns null if no corresponding
	 * node is found.
	 * 
	 * @param index
	 * 		The index to query
	 * @param property
	 * 		The property to use for the query
	 * @param key
	 * 		The value to be searched for under the specified property
	 * @return
	 * 		A single node resulting from the query, or null if no such node exists
	 * @throws IllegalStateException
	 * 		If multiple nodes are found
	 */
	public static Node getSingleNodeIndexHit(Index<Node> index, String property, Object key) {
		Node result = null;
		IndexHits<Node> hits = index.get(property, key);
		try {
			if (hits.size() == 1) {
				result = hits.getSingle();
				
			} else if (hits.size() > 1) {
				throw new IllegalStateException("More than one hit found for " + property + " == " + key + ". "
						+ "The database is probably corrupt.");
			}
		} finally {
			hits.close();
		}
		return result;
	}
	
	/**
	 * A convenience wrapper for querying node indexes that retrieves all results and then closes the IndexHits object.
	 * Returns an empty Iterable<Node> if no nodes are found. This will be inefficient when the list of results
	 * is large, as it must iterate over the results to store them in the iterator to be returned. For queries that
	 * could produce many hits, use direct access to the node indexes themselves.
	 * 
	 * @param index
	 * 		The index to query
	 * @param property
	 * 		The property to use for the query
	 * @param key
	 * 		The value to be searched for under the specified property
	 * @return
	 * 		An iterable over the nodes resulting from the query
	 */
	public static Iterable<Node> getMultipleNodeIndexHits(Index<Node> index, String property, Object key) {
		LinkedList<Node> result = new LinkedList<Node>();
		IndexHits<Node> hits = null;
		try {
			hits = index.query(property+":"+key);
			for (Node hit : hits) {
				result.add(hit);
			}
		} finally {
			hits.close();
		}
		return result;
	}
	
	/**
	 * Switches all the properties of two nodes. If only one node has any property, it will be removed from that
	 * node and set on the other.
	 * @param n1
	 * @param n2
	 */
	public static void exchangeAllProperties(Node n1, Node n2) {

		HashSet<String> propertiesObserved = new HashSet<String>();
		
		for (String p : n1.getPropertyKeys()) {
			propertiesObserved.add(p);
		}
		
		for (String p : n2.getPropertyKeys()) {
			propertiesObserved.add(p);
		}
		
		for (String p : propertiesObserved) {
			exchangeNodeProperty(n1, n2, p);
		}
	}
	
	/**
	 * Copies all properties of one node to the other. Does not replace properties on the target that are not set on the original node.
	 * node and set on the other.
	 * @param original
	 * @param target
	 */
	public static void copyAllProperties(Node original, Node target) {
		for (String p : original.getPropertyKeys()) {
			target.setProperty((p), original.getProperty(p));
		}
	}
	
	/**
	 * Copies all properties of one relationship to the other. Does not replace properties on the target that are not set on the original node.
	 * node and set on the other.
	 * @param original
	 * @param target
	 */
	public static void copyAllProperties(Relationship original, Relationship target) {
		for (String p : original.getPropertyKeys()) {
			target.setProperty((p), original.getProperty(p));
		}
	}

	/**
	 * Switches the value of the specified property between two nodes. If only one node has the property, it will be
	 * removed from that node and set on the other. If neither node has the property, there is no effect.
	 * @param n1
	 * @param n2
	 * @param p
	 */
	public static void exchangeNodeProperty(Node n1, Node n2, String p) {
	
		if (n1.hasProperty(p)) {
			Object n1Value = n1.getProperty(p);
	
			if (n2.hasProperty(p)) { // both nodes have the property
				Object n2Value = n2.getProperty(p);
				n2.setProperty(p, n1Value);
				n1.setProperty(p, n2Value);
	
			} else { // only node 1 has the property
				n2.setProperty(p, n1Value);
				n1.removeProperty(p);	
			}
	
		} else if (n2.hasProperty(p)) { // only node 2 has the property
			Object n2Value = n2.getProperty(p);
			n1.setProperty(p, n2Value);
			n2.removeProperty(p);
	
		} else { // neither node has the property
			return;
		}
	}
	
	/**
	 * Alternative method kept in case currently implemented one fails for some reason
	 * @param n1
	 * @param n2
	 * @param p
	 */
	@Deprecated
	public static void exchangeNodePropertyALTERNATIVE(Node n1, Node n2, String p) {
	
		Object n1Value = null;
		Object n2Value = null;
		
		boolean n1HasProperty = false;
		boolean n2HasProperty = false;

		if (n1.hasProperty(p)) {
			n1HasProperty = true;
			n1Value = n1.getProperty(p);
		}

		if (n2.hasProperty(p)) {
			n2HasProperty = true;
			n2Value = n2.getProperty(p);
		}

		if (n1HasProperty) {
			n2.setProperty(p, n1Value);
		} else if (n2.hasProperty(p)) {
			n2.removeProperty(p);
		}
		
		if (n2HasProperty) {
			n1.setProperty(p, n2Value);
		} else if (n1.hasProperty(p)) {
			n1.removeProperty(p);
		}
	}
}
