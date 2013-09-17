package org.opentree.otu;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.opentree.otu.constants.OTURelType;

/**
 * Static methods for performing common tasks with the database.
 */
public class OTUDatabaseUtils {
	
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
			if (root.hasRelationship(OTURelType.CHILDOF, Direction.OUTGOING)) {
				root = root.getSingleRelationship(OTURelType.CHILDOF, Direction.OUTGOING).getEndNode();
			} else {
				break;
			}
		}
		return root;
	}
}
