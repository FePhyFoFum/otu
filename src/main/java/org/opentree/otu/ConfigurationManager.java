package org.opentree.otu;

import java.io.File;
import java.io.IOException;

import org.neo4j.graphdb.GraphDatabaseService;
import org.opentree.graphdb.GraphDatabaseAgent;
import org.opentree.otu.constants.OTUGraphProperty;
import org.opentree.properties.BasicType;
import org.opentree.properties.OTProperty;

public class ConfigurationManager extends OTUDatabase {
	
	public ConfigurationManager(GraphDatabaseService gds) {
		super(gds);
	}
	
	public boolean setNexsonGitDir(String dir) {
		//TODO: make sure that the directory exists
		try {
			new File(dir).getCanonicalFile().isDirectory();
		} catch (IOException e) {
			return false; 
		}
		graphDb.setGraphProperty(OTUGraphProperty.NEXSON_GIT_DIR.propertyName(), dir);
		return true;
	}
	
	public String getNexsonGitDir() {
		String curDir = (String) graphDb.getGraphProperty(OTUGraphProperty.NEXSON_GIT_DIR.propertyName());
		if (curDir != null) {
			return curDir;
		} else {
			return null;
		}
	}
	
	public Object getGraphProperty(String propertyName) {		
		return graphDb.getGraphProperty(OTUGraphProperty.valueOf(propertyName.toUpperCase()));
	}
	
	public void setGraphProperty(String propertyName, String value, String type) {		
		BasicType basicType = BasicType.valueOf(type.toUpperCase());
		OTProperty graphProperty = OTUGraphProperty.valueOf(propertyName.toUpperCase());
		
		graphDb.setGraphProperty(graphProperty.propertyName(), basicType.convertToValue(value));
	}
}
