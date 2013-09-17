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
	public String getNexsonGitDir(@Source GraphDatabaseService graphDb) {
		DatabaseManager dm = new DatabaseManager(graphDb);
		ConfigurationManager cm = new ConfigurationManager(dm);
		String dir = cm.getNexsonGitDir();
		String retstr = "{\"nexsongitdir\":\""+dir+"\"}";
		return retstr;
	}
	
	@Description( "" )
	@PluginTarget( GraphDatabaseService.class )
	public boolean setNexsonGitDir(@Source GraphDatabaseService graphDb,
			@Description( "Nexson Git Directory String")
			@Parameter(name = "nexsongitdir", optional = false) String dir) {
		DatabaseManager dm = new DatabaseManager(graphDb);
		ConfigurationManager cm = new ConfigurationManager(dm);
		boolean success = cm.setNexsonGitDir(dir);
		return success;
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
