package org.opentree.otu;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.opentree.graphdb.DatabaseAbstractBase;
import org.opentree.graphdb.GraphDatabaseAgent;
import org.opentree.graphdb.NodeIndexDescription;

public class OTUDatabase extends DatabaseAbstractBase {

	public static final String LOCAL_LOCATION = "local";
	
	public OTUDatabase(GraphDatabaseService graphService) {
		super(graphService);
	}

	public OTUDatabase(EmbeddedGraphDatabase embeddedGraph) {
		super(embeddedGraph);
	}

	public OTUDatabase(GraphDatabaseAgent gdb) {
		super(gdb);
	}
	
	// Currently we always want fulltext indexes for OTU itself, so just we code that in here.
	// To specify other types of indexes we can just pass the relevant parameters as String... arguments,
	// which will invoke the analagous underlying method in DatabaseAbstractBase. See DatabaseManager for an example.
	public Index<Node> getNodeIndex(NodeIndexDescription index) {
		return graphDb.getNodeIndex(index.indexName(), IndexManager.PROVIDER, "lucene", "type", "fulltext");
	}
}
