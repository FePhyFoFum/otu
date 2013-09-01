/*
 * Read in a Nexml/JSON study file, returning a list of JadeTrees.

 * The file is assumed to be in the form produced by the
 * JSON generator in Phylografter; in particular, XML namespace
 * declarations are not processed, so particular namespace prefix
 * bindings, and a default namespace of "http://www.nexml.org/2009",
 * are assumed.
 *
 * @about is ignored, etc.
 * Ill-formed nexson files will lead to random runtime exceptions, such as bad 
 * casts and null pointer exceptions.  Should be cleaned up eventually, but not
 * high priority.
 */

package opentree.otu;

import jade.tree.JadeNode;
import jade.tree.JadeTree;
import jade.tree.NexsonReader;

import jade.MessageLogger;

import org.json.simple.JSONValue;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.neo4j.graphdb.Node;

import java.io.Reader;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class NexsonWriter {

	/* main method for testing - just dumps out Newick */
	public static void main(String argv[]) throws Exception {
		String filename = "study15.json";
		if (argv.length == 1) {
			filename = argv[0];
		}
		MessageLogger msgLogger = new MessageLogger("nexsonReader:");
		int treeIndex = 0;
		for (JadeTree tree : NexsonReader.readNexson(filename, true, msgLogger)) {
			if (tree == null) {
				msgLogger.messageInt("Null tree indicating unparseable NexSON", "index", treeIndex);
			} else {
				msgLogger.messageInt("tree", "index", treeIndex);
				msgLogger.indentMessageStr(1, "annotation", "Curator", (String)tree.getObject("ot:curatorName"));
				msgLogger.indentMessageStr(1, "annotation", "Reference", (String)tree.getObject("ot:studyPublicationReference"));
				msgLogger.indentMessageStr(1, "representation", "newick", tree.getRoot().getNewick(false));
				int i = 0;
				for (JadeNode node : tree.iterateExternalNodes()) {
					Object o = node.getObject("ot:ottolid");
					msgLogger.indentMessageStr(2, "node", "name", node.getName());
					msgLogger.indentMessageStr(2, "node", "OTT ID", o.toString());
					msgLogger.indentMessageStr(2, "node", "ID class", o.getClass().toString());
					if (++i > 10) {
						break;
					}
				}
			}
			++treeIndex;
		}
	}
	
	/* Return Nexson String  given a list of trees and a studyID*/
	//
	public static void writeNexson(Node metadatanode) throws java.io.IOException {
		StringBuffer sb = new StringBuffer();
		/*
		  The format of the file, roughly speaking (some noise omitted):
		  {"nexml": {
		  "@xmlns": ...,
		  "@nexmljson": "http:\/\/www.somewhere.org",
		  "meta": [...],
		  "otus": { "otu": [...] },
		  "trees": { "tree": [...] }}}

		  See http://www.nexml.org/manual for NexML documentation.
		*/
		String studyID = "";
		if (metadatanode.hasProperty("sourceID"))
			studyID = (String)metadatanode.getProperty("sourceID");
		
		sb.append("{\"nexml\":{\n");
		sb.append("\"@about\": \"#study\",\n"); 
		sb.append("\"@generator\": \"otu nexson writer\",\n"); 
		sb.append("\"@id\": \"study\",\n"); 
		sb.append("\"@nexmljson\": \"http://opentree.wikispaces.com/NexSON\",\n"); 
		sb.append("\"@version\": \"0.9\", \n");
		sb.append("\"@xmlns\": {\n");
		sb.append("\"$\": \"http://www.nexml.org/2009\",\n"); 
		sb.append("\"nex\": \"http://www.nexml.org/2009\",\n"); 
		sb.append("\"ot\": \"http://purl.org/opentree-terms#\", \n");
		sb.append("\"xsd\": \"http://www.w3.org/2001/XMLSchema#\", \n");
		sb.append("\"xsi\": \"http://www.w3.org/2001/XMLSchema-instance\"\n");
		sb.append("},\n");
		
		//do the metadata
		sb.append("\"meta\": [\n");
		sb.append("{\n");
		
		if(metadatanode.hasProperty("ot:studyPublicationReference")){
			sb.append("{\n");
			sb.append("\"$\": "+(String)metadatanode.getProperty("ot:studyPublicationReference")+",\n"); 
			sb.append("\"@property\": \"ot:studyPublicationReference\",\n"); 
			sb.append("\"@xsi:type\": \"nex:LiteralMeta\"\n");
			sb.append("},\n");
		}if(metadatanode.hasProperty("ot:studyPublication")){
			sb.append("{\n");
			sb.append("\"@href\": "+(String)metadatanode.getProperty("ot:studyPublication")+",\n"); 
			sb.append("\"@property\": \"ot:studyPublication\",\n"); 
			sb.append("\"@xsi:type\": \"nex:ResourceMeta\"\n");
			sb.append("},\n");
		}if(metadatanode.hasProperty("ot:curatorName")){
			sb.append("{\n");
			sb.append("\"@href\": "+(String)metadatanode.getProperty("ot:curatorName")+",\n"); 
			sb.append("\"@property\": \"ot:curatorName\",\n"); 
			sb.append("\"@xsi:type\": \"nex:LiteralMeta\"\n");
			sb.append("},\n");
		}if(metadatanode.hasProperty("ot:dataDeposit")){
			sb.append("{\n");
			sb.append("\"@href\": "+(String)metadatanode.getProperty("ot:dataDeposit")+",\n"); 
			sb.append("\"@property\": \"ot:dataDeposit\",\n"); 
			sb.append("\"@xsi:type\": \"nex:ResourceMeta\"\n");
			sb.append("},\n");
		}if(metadatanode.hasProperty("sourceID")){
			sb.append("{\n");
			sb.append("\"$\": "+(String)metadatanode.getProperty("sourceID")+",\n"); 
			sb.append("\"@property\": \"ot:studyId\",\n"); 
			sb.append("\"@xsi:type\": \"nex:LiteralMeta\"\n");
			sb.append("},\n");
		}if(metadatanode.hasProperty("ot:studyYear")){
			sb.append("{\n");
			sb.append("\"$\": "+(String)metadatanode.getProperty("ot:studyYear")+",\n"); 
			sb.append("\"@property\": \"ot:studyYear\",\n"); 
			sb.append("\"@xsi:type\": \"nex:LiteralMeta\"\n");
			sb.append("},\n");
		}if(metadatanode.hasProperty("ot:tag")){
			sb.append("{\n");
			sb.append("\"$\": "+(String)metadatanode.getProperty("ot:tag")+",\n"); 
			sb.append("\"@property\": \"ot:tag\",\n"); 
			sb.append("\"@xsi:type\": \"nex:LiteralMeta\"\n");
			sb.append("},\n");
		}
		sb.append("],\n");		
			
		//do the otus
			
		//do the trees
	}


}