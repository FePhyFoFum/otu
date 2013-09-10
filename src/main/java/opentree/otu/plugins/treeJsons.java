package opentree.otu.plugins;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import jade.tree.*;
import opentree.otu.DatabaseBrowser;
import opentree.otu.DatabaseManager;
import opentree.otu.DatabaseUtils;
import opentree.otu.GraphDatabaseAgent;
import opentree.otu.constants.NodeProperty;
import opentree.otu.constants.RelType;
import opentree.otu.exceptions.NoSuchTreeException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.server.plugins.*;
import org.neo4j.server.rest.repr.OpentreeRepresentationConverter;
import org.neo4j.server.rest.repr.Representation;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

public class treeJsons extends ServerPlugin{
	
	/**
	 * @param nodeId
	 * @return
	 * @throws NoSuchTreeException 
	 */
	@Description( "Get the neo4j root node for a given tree id" )
	@PluginTarget( GraphDatabaseService.class )
	public Long getRootNodeIdForTreeId(@Source GraphDatabaseService graphDb,
			@Description( "The id of the tree to be found.")
			@Parameter(name = "treeId", optional = false) String treeId) throws NoSuchTreeException {

		DatabaseBrowser browser = new DatabaseBrowser(graphDb);

		// TODO: add check for whether tree is imported. If not then return this information
		
		Node rootNode = browser.getTreeRootNode(treeId, browser.LOCAL_LOCATION);
		return rootNode.getId();
	}
	
	/**
	 * @param nodeId
	 * @return
	 */
	@Description( "Remove a previously imported tree from the graph" )
	@PluginTarget( GraphDatabaseService.class )
	public Representation deleteTreeFromTreeId(@Source GraphDatabaseService graphDb,
			@Description( "The id of the tree to be deleted")
			@Parameter(name = "treeId", optional = false) String treeId) {
		
		DatabaseManager manager = new DatabaseManager(graphDb);
		DatabaseBrowser browser = new DatabaseBrowser(graphDb);
		
		Node root = browser.getTreeRootNode(treeId, browser.LOCAL_LOCATION);
		manager.deleteTree(root);

		// return result
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("event", "success");
		result.put("treeId", treeId);
		return OpentreeRepresentationConverter.convert(result);
	}
	
	/**
	 * @param nodeId
	 * @return
	 */
	@Description( "Reroot the tree containing the indicated node, using that node as the new root. Returns the neo4j node id of the new root." )
	@PluginTarget( GraphDatabaseService.class )
	public Long rerootTree(@Source GraphDatabaseService graphDb,
			@Description( "The Neo4j node id of the node to be used as the root for its tree.")
			@Parameter(name = "nodeId", optional = false) Long nodeId) {
		DatabaseManager manager = new DatabaseManager(graphDb);
		Node rootNode = graphDb.getNodeById(nodeId);
		Node newroot = manager.rerootTree(rootNode);
		return newroot.getId();
	}
	
	/**
	 * @param nodeId
	 * @return
	 */
	@Description( "Set the ingroup of the tree containing the indicated node to that node." )
	@PluginTarget( GraphDatabaseService.class )
	public Long ingroupSelect(@Source GraphDatabaseService graphDb,
			@Description( "The Neo4j node id of the node to be used as the ingroup for its tree.")
			@Parameter(name = "nodeId", optional = false) Long nodeId) {
		DatabaseManager manager = new DatabaseManager(graphDb);
		Node rootNode = graphDb.getNodeById(nodeId);
		manager.designateIngroup(rootNode);
		return rootNode.getId();
	}
	
	/**
	 * @param nodeId
	 * @return
	 */
	@Description( "Return a string containing a JSON string for the subtree below the indicated tree node" )
	@PluginTarget( GraphDatabaseService.class )
	public String getTreeJson(@Source GraphDatabaseService graphDb,
			@Description( "The Neo4j node id of the node to be used as the root for the tree (can be used to extract subtrees as well).")
			@Parameter(name = "nodeId", optional = false) Long nodeId) {
//		DatabaseBrowser browser = new DatabaseBrowser(graphDb);

		// TODO: add check for whether tree is imported. If not then return error instead of just empty tree
		Node rootNode = graphDb.getNodeById(nodeId);
		JadeTree t = DatabaseBrowser.getTreeFromNode(rootNode, 300);

		return t.getRoot().getJSON(false);
	}
	
	@Description( "Get tree metadata" )
	@PluginTarget( GraphDatabaseService.class )
	public Representation getTreeMetaData(@Source GraphDatabaseService graphDb,
			@Description( "The database tree id for the tree")
			@Parameter(name = "treeId", optional = false) String treeId) {
		
		DatabaseBrowser browser = new DatabaseBrowser(graphDb);
		Node root = browser.getTreeRootNode(treeId, browser.LOCAL_LOCATION);
		return OpentreeRepresentationConverter.convert(browser.getMetadataForTree(root));
	}
	
	@Description( "Get the id for the source associated with the specified tree id" )
	@PluginTarget( GraphDatabaseService.class )
	public String getSourceIdForTreeId(@Source GraphDatabaseService graphDb,
			@Description( "The tree id to use")
			@Parameter(name = "treeId", optional = false) String treeId) {
	
		DatabaseBrowser browser = new DatabaseBrowser(graphDb);

		Node treeRoot = browser.getTreeRootNode(treeId, browser.LOCAL_LOCATION);
		Node sourceMeta = treeRoot.getSingleRelationship(RelType.METADATAFOR, Direction.INCOMING).getStartNode();

		return (String) sourceMeta.getProperty(NodeProperty.SOURCE_ID.name);
	}
	
	@Description( "Get OTU metadata" )
	@PluginTarget( Node.class )
	public Representation getOTUMetaData(@Source Node node) {

		// TODO: use this to fill out the node editor
		
		DatabaseBrowser browser = new DatabaseBrowser(node.getGraphDatabase());
		return OpentreeRepresentationConverter.convert(browser.getMetadataForOTU(node));
	}
	
	@Description ("Hit the TNRS for all the names in a subtree. Return the results.")
	@PluginTarget( Node.class )
	public Representation doTNRSForDescendants(@Source Node root,
		@Description ("The url of the TNRS service to use. If not supplied then the public OT TNRS will be used.")
			@Parameter (name="TNRS Service URL", optional=true) String tnrsURL,
		@Description ("NOT IMPLEMENTED. If it were, this would just say: If set to false (default), only the original " +
				"otu labels will be used for TNRS. If set to true, currently mapped names will be used (if they exist).")
			@Parameter(name="useMappedNames", optional=true) boolean useMappedNames) throws IOException, ParseException {
		
		LinkedList<Long> nodeIds = new LinkedList<Long>();
		LinkedList<String> names = new LinkedList<String>();
		
		// make a map of these with ids and original names
		for (Node otu : DatabaseUtils.DESCENDANT_OTU_TRAVERSAL.traverse(root).nodes()) {
			
			// TODO: allow the choice to use mapped or original names... currently that leads to nullpointerexceptions

			if (otu.hasProperty(NodeProperty.NAME.name)) {
				nodeIds.add(otu.getId());
				names.add((String) otu.getProperty(NodeProperty.NAME.name));
			}
		}
		
		if (tnrsURL == null) {
			tnrsURL = "http://dev.opentreeoflife.org/taxomachine/ext/TNRS/graphdb/contextQueryForNames/";
		}
		
		// gather the data to be sent to tnrs
		Map<String, Object> query = new HashMap<String, Object>();
		query.put("names", names);
		query.put("idInts", nodeIds);

        // set up the connection
        ClientConfig cc = new DefaultClientConfig();
        Client c = Client.create(cc);
        WebResource tnrs = c.resource(tnrsURL);

        // send the query (get the response)
        String respJSON = tnrs.accept(MediaType.APPLICATION_JSON_TYPE)
        		.type(MediaType.APPLICATION_JSON_TYPE).post(String.class, new JSONObject(query).toJSONString());
        
        JSONParser parser = new JSONParser();
        JSONObject response = (JSONObject) parser.parse(respJSON);

        GraphDatabaseAgent graphDb = new GraphDatabaseAgent(root.getGraphDatabase()) ;
        
        Transaction tx = graphDb.beginTx();
        
        root.setProperty(NodeProperty.CONTEXT_NAME.name, response.get("context"));
        root.setProperty(NodeProperty.CONTAINS_TAXON_MAPPINGS.name, true);
        
        try {
	        // walk the results
	        for (Object nameResult : (JSONArray) response.get("results")) {
	
	        	JSONArray matches = (JSONArray) ((JSONObject) nameResult).get("matches");
	        	Node otuNode = graphDb.getNodeById((Long) ((JSONObject) nameResult).get("id"));
	        	
	            // if there is an exact match, update the node properties to reflect this
	        	if (matches.size() == 1) {
	        		JSONObject match = ((JSONObject) matches.get(0));
	        		if ((Double) match.get("score") == 1.0) {
	        			
	        			otuNode.setProperty(NodeProperty.OT_OTT_ID.name, match.get("matched_ott_id"));
	        			otuNode.setProperty(NodeProperty.OT_OTT_TAXON_NAME.name,  match.get("matched_name"));
	        		}
	        	} else {
	        		// create TNRS result nodes holding each match's info
	        		for (Object m : matches) {
	        			JSONObject match = (JSONObject) m;
	        			Node tnrsNode = graphDb.createNode();
	        			for (Object property : match.keySet()) {
	        				tnrsNode.setProperty((String) property, match.get(property));
	        			}
	        			tnrsNode.createRelationshipTo(otuNode, RelType.TNRSMATCHFOR);
	        		}
	        	}
	        }
	        tx.success();
        } finally {
        	tx.finish();
        }
        
        // return relevant info
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("event", "success");
        result.put("treeId", DatabaseUtils.getRootOfTreeContaining(root).getProperty(NodeProperty.TREE_ID.name));
        result.put("rootNodeId", root.getId());
        result.put("unmatched_name_ids", response.get("unmatched_name_ids"));
        result.put("matched_name_ids", response.get("matched_name_ids"));
        result.put("unambiguous_name_ids", response.get("unambiguous_name_ids"));
        result.put("context", response.get("context"));
        return OpentreeRepresentationConverter.convert(result);
        
        /*
		// save the result to a local file
        
        // TODO: the tnrs files get saved into the neo4j directory root. it would be better to save them in the
        // otu directory, but to do that we will have to do some some finagling...
        String savedResultsFilePath = "tnrs." + root.getId() + "." + System.currentTimeMillis() + ".json";
        File resultsFile = new File(savedResultsFilePath);
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter( new FileWriter(resultsFile));
            writer.write(respJSON);

        } finally {
        	if ( writer != null) {
        		writer.close( );
        	}
        }
        
        // return some JSON with the information for to use when reloading the page
        Map<String, Object> results = new HashMap<String, Object>();
        results.put("event", "success");
        results.put("treeId", DatabaseUtils.getRootOfTreeContaining(root).getProperty(NodeProperty.TREE_ID.name));
        results.put("results_file", resultsFile.getAbsolutePath());
		
        return(OpentreeRepresentationConverter.convert(results)); */
	}
}
