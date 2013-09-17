package org.opentree.otu;

import java.io.File;
import java.io.IOException;

import org.opentree.graphdb.GraphDatabaseAgent;
import org.opentree.otu.constants.OTUGraphProperty;

public class ConfigurationManager  {

	private DatabaseManager dm = null;
	
	public ConfigurationManager(DatabaseManager dm){
		this.dm = dm;
	}
	
	public boolean setNexsonGitDir(String dir){
		//TODO: make sure that the directory exists
		try {
			new File(dir).getCanonicalFile().isDirectory();
		} catch (IOException e) {
			return false; 
		}
		GraphDatabaseAgent gda = dm.getGraphDatabase();
		gda.setGraphProperty(OTUGraphProperty.NEXSON_GIT_DIR.propertyName(), dir);
		return true;
	}
	
	public String getNexsonGitDir( ){
		GraphDatabaseAgent gda = dm.getGraphDatabase();
		String curDir = (String) gda.getGraphProperty(OTUGraphProperty.NEXSON_GIT_DIR.propertyName());
		if  (curDir != null){
			return curDir;
		}else{
			return null;
		}
	}
}
