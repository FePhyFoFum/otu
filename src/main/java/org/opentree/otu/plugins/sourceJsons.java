package org.opentree.otu.plugins;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.ws.rs.core.MediaType;

import jade.tree.*;

import org.opentree.otu.ConfigurationManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.Traversal;
import org.neo4j.server.plugins.*;
import org.neo4j.server.rest.repr.OTRepresentationConverter;
import org.neo4j.server.rest.repr.Representation;
import org.opentree.MessageLogger;
import org.opentree.graphdb.GraphDatabaseAgent;
import org.opentree.nexson.io.NexsonReader;
import org.opentree.nexson.io.NexsonSource;
import org.opentree.otu.DatabaseBrowser;
import org.opentree.otu.DatabaseManager;
import org.opentree.otu.constants.OTUConstants;
import org.opentree.otu.OTUDatabaseUtils;
import org.opentree.otu.constants.OTUGraphProperty;
import org.opentree.otu.constants.OTUNodeProperty;
import org.opentree.otu.constants.OTURelType;
import org.opentree.otu.exceptions.DuplicateSourceException;
import org.opentree.properties.OTPropertyChoices;
import org.opentree.properties.OTVocabularyPredicate;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

public class sourceJsons extends ServerPlugin {

	@Description("Return JSON containing information tree ids for all local sources")
	@PluginTarget(GraphDatabaseService.class)
	public Representation getTreeIdsForAllLocalSources(@Source GraphDatabaseService graphDb,
		@Description("Tree ids to exclude from the results") @Parameter(name="excludedTreeIds", optional=true) String[] excludedTreeIdsArr){
		Set<String> excludedTreeIds = new HashSet<String>();
		if (excludedTreeIdsArr != null) {
			for (Object tid : excludedTreeIdsArr) {
				excludedTreeIds.add((String) tid);
			}
		}
		DatabaseBrowser browser = new DatabaseBrowser(graphDb);	
		Map<String, Object> results = new HashMap<String, Object>();
		results.put("trees", browser.getTreeIdsForSourceId(browser.LOCAL_LOCATION, "*", excludedTreeIds));
		return OTRepresentationConverter.convert(results);
	}
	
	@Description("Return JSON containing information tree ids for the specified source")
	@PluginTarget(GraphDatabaseService.class)
	public Representation getTreeIdsForLocalSource (
			@Source GraphDatabaseService graphDb,
			@Description("The source id") @Parameter(name="sourceId", optional=false) String sourceId,
			@Description("Tree ids to exclude from the results") @Parameter(name="excludedTreeIds", optional=true) String[] excludedTreeIdsArr){
		Set<String> excludedTreeIds = new HashSet<String>();
		if (excludedTreeIdsArr != null) {
			for (Object tid : excludedTreeIdsArr) {
				excludedTreeIds.add((String) tid);
			}
		}
		DatabaseBrowser browser = new DatabaseBrowser(graphDb);
		Map<String, Object> results = new HashMap<String, Object>();
		results.put("trees", browser.getTreeIdsForSourceId(browser.LOCAL_LOCATION, sourceId, excludedTreeIds));
		return OTRepresentationConverter.convert(results);
	}

	@Description("Return JSON containing information about local trees")
	@PluginTarget(GraphDatabaseService.class)
	public Representation getSourceList(@Source GraphDatabaseService graphDb,
			@Description("Source ids to be excluded from the results") @Parameter(name="excludedSourceIds", optional=true) String[] excludedSourceIdsArr) {
		Set<String> excludedSourceIds = new HashSet<String>();
		if (excludedSourceIdsArr != null) {
			for (Object sid : excludedSourceIdsArr) {
				excludedSourceIds.add((String) sid);
			}
		}
		DatabaseBrowser browser = new DatabaseBrowser(graphDb);
		Map<String, Object> results = new HashMap<String, Object>();
		results.put("sources", browser.getSourceIds(browser.LOCAL_LOCATION, excludedSourceIds));
		return OTRepresentationConverter.convert(results);
	}

	/**
	 * this is a single tree version
	 * 
	 * @param nodeid
	 * @return
	 */
	@Description("Load a single newick tree into the graph") // TODO: this crashes the db (at least sometimes?)... Not sure why
	@PluginTarget(GraphDatabaseService.class)
	public Representation putSourceNewickSingle(
			@Source GraphDatabaseService graphDb,
			@Description("A string to be used as the source id for for this source. Source ids must be unique.") @Parameter(name = "sourceId", optional = false) String sourceId,
			@Description("A newick string containing the tree to be added.") @Parameter(name = "newickString", optional = false) String newickString) {

		Map<String, Object> result = new HashMap<String, Object>();
		
		GraphDatabaseAgent gdb = new GraphDatabaseAgent(graphDb);
		DatabaseManager dm = new DatabaseManager(gdb);
		NexsonSource source = new NexsonSource(sourceId);

		TreeReader tr = new TreeReader();
		source.addTree(tr.readTree(newickString));

		try {
			dm.addSource(source, DatabaseManager.LOCAL_LOCATION);
		} catch (DuplicateSourceException e) {
			result.put("event", "warning");
			result.put("message", "A local source with id " + sourceId + " already exists in the database.");
		}
	
		result.put("event", "added");
		result.put("sourceId", sourceId);
		return OTRepresentationConverter.convert(result);
	}

	/**
	 * this is single or multiple trees
	 * 
	 * @param graphDb
	 * @param sourceId
	 * @param newickString
	 * @return
	 */
	@Description("Incomplete placeholder for multi-tree upload")
	@PluginTarget(GraphDatabaseService.class)
	public String putSourceNewickMultiple(
			@Source GraphDatabaseService graphDb,
			@Description("A string to be used as the source id for for this source. Source ids must be unique.")
			@Parameter(name = "sourceId", optional = false) String sourceId,
			@Description("A newick string containing the tree to be added.")
			@Parameter(name = "newickString", optional = false) String newickString) {

		return null;
	}

	@Description("Load a nexson file into the graph database")
	@PluginTarget(GraphDatabaseService.class)
	public Representation putSourceNexsonFile(
			@Source GraphDatabaseService graphDb,
			@Description("A string to be used as the source id for for this source. Source ids must be unique.")
			@Parameter(name = "sourceId", optional = false) String sourceId,
			@Description("A nexson string to be parsed")
			@Parameter(name = "nexsonString", optional = false) String nexsonString) {

		Map<String, Object> result = new HashMap<String, Object>();

		MessageLogger msgLogger = new MessageLogger("");
		StringReader sr = new StringReader(nexsonString);
		NexsonSource source = null;
		try {
			source = NexsonReader.readNexson(sr, sourceId, false, msgLogger);
		} catch (IOException e) {
			result.put("event", "warning");
			result.put("message", e.toString());
		}

		DatabaseManager manager = new DatabaseManager(graphDb);
		try {
			manager.addSource(source, DatabaseManager.LOCAL_LOCATION);
		} catch (DuplicateSourceException ex) {
			result.put("event", "warning");
			result.put("message", "A local source with id " + sourceId + " already exists in the database.");
		}

		if (result.size() < 1) {
			result.put("event", "added");
			result.put("sourceId", sourceId);
		}

		return OTRepresentationConverter.convert(result);
	}

	@Description("Get source metadata")
	@PluginTarget(GraphDatabaseService.class)
	public Representation getSourceMetaData(@Source GraphDatabaseService graphDb,
			@Description("source Id")
			@Parameter(name = "sourceId", optional = false) String sourceId) {
		
		DatabaseBrowser browser = new DatabaseBrowser(graphDb);

		// look for local first since it's faster
		Node sourceMeta = browser.getSourceMetaNode(sourceId, DatabaseBrowser.LOCAL_LOCATION);

		// if we didn't find a local source, check remotes
		if (sourceMeta == null) {
			List<Node> sourceMetasRemote = browser.getRemoteSourceMetaNodesForSourceId(sourceId);
			if (sourceMetasRemote.size() > 0) {
				sourceMeta = sourceMetasRemote.get(0);
			}
		}
		
		return OTRepresentationConverter.convert(sourceMeta == null ? null : browser.getMetadataForSource(sourceMeta));
	}

	/**
	 * Submit properties to be stored at a node.
	 * 
	 * @param nodeid
	 * @return
	 */
	@Description("Set the properties as specified")
	@PluginTarget(Node.class)
	public Representation setBasicProperties(@Source Node node,
			@Description("The keys for the properties to be set") @Parameter(name="keys", optional=false) String[] keys,
			@Description("The values for the properties to be set") @Parameter(name="values", optional=false) String[] values,
			@Description("The types for the values to be set. These are case-insensitive and must be one of 'boolean', 'integer', 'decimal', or 'string'")
					@Parameter(name="types", optional=false) String[] types,
			@Description("Names of properties to be removed") @Parameter(name="propertiesToRemove", optional=true) String[] propertiesToRemove) {
		
		// TODO: would be better to have this all contained in a single transaction.
		
		GraphDatabaseService gds = node.getGraphDatabase();
		DatabaseManager manager = new DatabaseManager(gds);

		// first remove all indicated properties
		Transaction tx = gds.beginTx();
		try {
			if (propertiesToRemove != null) {
				for (String key : propertiesToRemove) {
					node.removeProperty(key);
				}
			}
			tx.success();
		} finally {
			tx.finish();
		}

		// now set properties we have been told to set
		manager.setProperties(node, keys, values, types);
		
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("event", "success");
		return OTRepresentationConverter.convert(result);
	}
	
	@Description("Report a list of available properties for source metadata nodes")
	@PluginTarget(GraphDatabaseService.class)
	public Representation getSourceProperties(@Source GraphDatabaseService graphDb) {
		DatabaseBrowser browser = new DatabaseBrowser(graphDb);
		return OTRepresentationConverter.convert(browser.getAvailableSourceProperties());
	}

	@Description("Report a list of available properties for trees (i.e. tree root nodes)")
	@PluginTarget(GraphDatabaseService.class)
	public Representation getTreeProperties(@Source GraphDatabaseService graphDb) {
		DatabaseBrowser browser = new DatabaseBrowser(graphDb);
		return OTRepresentationConverter.convert(browser.getAvailableTreeProperties());
	}

	@Description("Report a list of available properties for internal tree nodes")
	@PluginTarget(GraphDatabaseService.class)
	public Representation getTreeNodeProperties(@Source GraphDatabaseService graphDb) {
		DatabaseBrowser browser = new DatabaseBrowser(graphDb);
		return OTRepresentationConverter.convert(browser.getAvailableTreeNodeProperties());
	}

	@Description("Report a map containing lists of preset choices for fixed-choice properties")
	@PluginTarget(GraphDatabaseService.class)
	public Representation getChoicePresets(@Source GraphDatabaseService graphDb) {
		Map<String, Object> choices = new HashMap<String, Object>();
		for (OTPropertyChoices p : OTPropertyChoices.values()) {
			choices.put(p.propertyName(), p.choices());
		}
		return OTRepresentationConverter.convert(choices);
	}

	/**
	 * Assign a taxon to the node.
	 * 
	 * @param nodeid
	 * @return
	 */
	@Description("Set the properties as specified")
	@PluginTarget(Node.class)
	public Representation assignTaxon(@Source Node node,
			@Description("The ott id of the taxon to be set") @Parameter(name="ottId", optional=false) Long ottId,
			@Description("The name of the ott taxon corresponding to this ott id") @Parameter(name="taxonName", optional=false) String taxonName) {
		
		GraphDatabaseAgent graphDb = new GraphDatabaseAgent(node.getGraphDatabase());
		ConfigurationManager config = new ConfigurationManager(graphDb);
		DatabaseManager manager = new DatabaseManager(graphDb);
		
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(OTVocabularyPredicate.OT_OTT_ID.propertyName(), ottId);
		properties.put(OTVocabularyPredicate.OT_OTT_TAXON_NAME.propertyName(), taxonName);
		properties.put(OTUNodeProperty.NAME.propertyName(), taxonName);
		
		manager.setProperties(node, properties);

		Transaction tx = node.getGraphDatabase().beginTx();
		try {

			// remove any existing tnrs matches
			for (Node match : Traversal.description().relationships(OTURelType.TNRSMATCHFOR, Direction.INCOMING).traverse(node).nodes()) {
				if (match !=  node) {
					for (Relationship rel : match.getRelationships()) {
						rel.delete();
					}
					match.delete();
				}
			}

			// attach to taxonomy if present
			if (config.hasTaxonomy()) {
				manager.connectTreeNodeToTaxonomy(node);
			}
			
			tx.success();
		} finally {
			tx.finish();
		}
		
		
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("event", "success");
		return OTRepresentationConverter.convert(result);
	}

	/**
	 * Get the neo4j node id for the local source node for a specified source id.
	 * 
	 * @param sourceId
	 * @return
	 */
	@Description("Get the neo4j node id for the local source node for a specified source id")
	@PluginTarget(GraphDatabaseService.class)
	public Representation getNodeIdForSourceId(@Source GraphDatabaseService graphDb,
			@Description("The source id to look up") @Parameter(name="sourceId", optional=false) String sourceId) {
		
		DatabaseBrowser browser = new DatabaseBrowser(graphDb);
		return OTRepresentationConverter.convert(browser.getSourceMetaNode(sourceId, browser.LOCAL_LOCATION).getId());
	}

	
	/**
	 * Delete a tree from the local db.
	 * 
	 * @param nodeid
	 * @return
	 */
	@Description("Delete a source from the local db")
	@PluginTarget(GraphDatabaseService.class)
	public Representation deleteSourceFromSourceId(@Source GraphDatabaseService graphDb,
			@Description("source Id") @Parameter(name = "sourceId", optional = false) String sourceId) {

		DatabaseManager dm = new DatabaseManager(graphDb);
		DatabaseBrowser browser = new DatabaseBrowser(graphDb);
		Node sourceMeta = browser.getSourceMetaNode(sourceId, DatabaseBrowser.LOCAL_LOCATION);

		dm.deleteSource(sourceMeta);

		Map<String, Object> result = new HashMap<String, Object>();
		result.put("event", "added");
		result.put("sourceId", sourceId);
		return OTRepresentationConverter.convert(result);
	}
	
	
	/**
	 * Send an autocomplete query to taxomachine and pass the results back through. Just a wrapper to circumvent the single-origin policy.
	 * 
	 * @param tnrsURL
	 * @param queryString
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 */
	@Description ("Submit a query for autocompletion of taxon name.")
	@PluginTarget( GraphDatabaseService.class )
	public Representation autocompleteTaxonName(@Source GraphDatabaseService graphDb,
		@Description ("The url of the TNRS service to use. If not supplied then the public OT TNRS will be used.")
			@Parameter (name="TNRS Service URL", optional=true) String tnrsURL,
		@Description ("The query string to submit to the autocomplete service")
			@Parameter(name="queryString", optional=false) String queryString) throws IOException, ParseException {
		
		if (tnrsURL == null) {
			// TODO: get this from a graph property, which will be set to use local tnrs if it is installed
			tnrsURL = "http://dev.opentreeoflife.org/taxomachine/ext/TNRS/graphdb/autocompleteBoxQuery/";
		}
		
		// gather the data to be sent to tnrs
		Map<String, Object> query = new HashMap<String, Object>();
		query.put("queryString", queryString);

        // set up the connection
        ClientConfig cc = new DefaultClientConfig();
        Client c = Client.create(cc);
        WebResource tnrs = c.resource(tnrsURL);

        // send the query (get the response)
        String respJSON = tnrs.accept(MediaType.APPLICATION_JSON_TYPE)
        		.type(MediaType.APPLICATION_JSON_TYPE).post(String.class, new JSONObject(query).toJSONString());
        
        JSONParser parser = new JSONParser();
        JSONArray response = (JSONArray) parser.parse(respJSON);
        
        return OTRepresentationConverter.convert(response);
	}
}
