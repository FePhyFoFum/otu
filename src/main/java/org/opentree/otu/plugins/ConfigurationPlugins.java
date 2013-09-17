package org.opentree.otu.plugins;

import java.util.HashMap;
import java.util.Map;

import opentree.taxonomy.TaxonomyLoaderOTT;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.server.plugins.Description;
import org.neo4j.server.plugins.Parameter;
import org.neo4j.server.plugins.PluginTarget;
import org.neo4j.server.plugins.ServerPlugin;
import org.neo4j.server.plugins.Source;
import org.neo4j.server.rest.repr.OTRepresentationConverter;
import org.neo4j.server.rest.repr.Representation;
import org.opentree.graphdb.GraphDatabaseAgent;
import org.opentree.otu.ConfigurationManager;
import org.opentree.otu.DatabaseManager;
import org.opentree.otu.constants.OTUGraphProperty;

public class ConfigurationPlugins extends ServerPlugin {

	@Description( "" )
	@PluginTarget( GraphDatabaseService.class )
	public Representation getNexsonGitDir(@Source GraphDatabaseService graphDb) {
		ConfigurationManager config = new ConfigurationManager(graphDb);
		
		String dir = config.getNexsonGitDir();
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("nexsongitdir", dir ==  null ? "not specified" : dir);
	
		return OTRepresentationConverter.convert(result);
	}
		
	@Description( "" )
	@PluginTarget( GraphDatabaseService.class )
	public Representation setNexsonGitDir(@Source GraphDatabaseService graphDb,
			@Description( "Nexson Git Directory String")
			@Parameter(name = "nexsongitdir", optional = false) String dir) {

		ConfigurationManager config = new ConfigurationManager(graphDb);

		boolean success = config.setNexsonGitDir(dir);

		Map<String, Object> result = new HashMap<String, Object>();
		if (success) {
			result.put("event", "success");
		} else {
			result.put("event", "failure");
		}
		
		return OTRepresentationConverter.convert(result);
	}
	
	@Description("Get a property of the graph root node")
	@PluginTarget( GraphDatabaseService.class )
	public Representation getGraphProperty(@Source GraphDatabaseService graphDb,
			@Description("The name of the graph property to retrieve") @Parameter(name="propertyName", optional=false) String propertyName) {
		
		ConfigurationManager config = new ConfigurationManager(graphDb);
		Object value = config.getGraphProperty(propertyName);
		
		Map<String, Object> result = new HashMap<String, Object>();
		result.put(propertyName, value);
		return OTRepresentationConverter.convert(result);
	}

	@Description("Set a property of the graph root node")
	@PluginTarget( GraphDatabaseService.class )
	public Representation setGraphProperty(@Source GraphDatabaseService graphDb,
			@Description("The name of the graph property to retrieve")
				@Parameter(name="propertyName", optional=false) String propertyName,
			@Description("The value to be set")
				@Parameter(name="value", optional=false) String value,
			@Description("The datatype of the value. This is case-insensitive and must be one of 'boolean', 'integer', 'decimal', or 'string'.")
				@Parameter(name="type", optional=false) String type) {
		
		ConfigurationManager config = new ConfigurationManager(graphDb);
		config.setGraphProperty(propertyName, value, type);
		
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("event", "success");
		return OTRepresentationConverter.convert(result);
	}

	@Description( "Install the OTT taxonomy" )
	@PluginTarget( GraphDatabaseService.class )
	public Representation installOTT(@Source GraphDatabaseService graphDb,
			@Description( "Taxonomy file")
			@Parameter(name = "taxonomyFile", optional = false) String taxonomyFile) {

		TaxonomyLoaderOTT loader = new TaxonomyLoaderOTT(graphDb);

		// turn off unnecessary features
		loader.setAddSynonyms(false);
		loader.setAddBarrierNodes(false);
		loader.setbuildPreferredIndexes(false);
		loader.setbuildPreferredRels(false);

		// make sure we build the optional ott id index
		loader.setCreateOTTIdIndexes(true);
		
		loader.loadOTTIntoGraph("ott", taxonomyFile, "");
		
		GraphDatabaseAgent gdb = new GraphDatabaseAgent(graphDb);
		gdb.setGraphProperty(OTUGraphProperty.HAS_TAXONOMY.propertyName(), true);
		
		Map<String, Object> results = new HashMap<String, Object>();
		results.put("event", "success");
		
		return OTRepresentationConverter.convert(results);
	}
}
