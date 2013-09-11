package org.neo4j.server.rest.repr;

import jade.IterableArray;
import jade.tree.JadeNode;

import java.net.URI;
import java.util.ArrayList;
import java.util.Map;
import java.util.Stack;
import java.util.Iterator;

import opentree.otu.constants.NodeProperty;
import opentree.otu.constants.OTUConstants;

public class JadeNodeRepresentation extends MappingRepresentation {

    public JadeNodeRepresentation(RepresentationType type) {
        super(type);
    }

    public JadeNodeRepresentation(String type) {
        super(type);
    }

    @Override
    String serialize(RepresentationFormat format, URI baseUri, ExtensionInjector extensions) {
        MappingWriter writer = format.serializeMapping(type);
        Serializer.injectExtensions(writer, this, baseUri, extensions);
        serialize(new MappingSerializer(writer, baseUri, extensions));
        writer.done();
        return format.complete(writer);
    }

    @Override
    void putTo(MappingSerializer serializer, String key) {
        serializer.putMapping(key, this);
    }

    @Override
    protected void serialize(MappingSerializer serializer) {

    }

    public static MappingRepresentation getJadeNodeRepresentation(final JadeNode jadeNode) {

        return new MappingRepresentation(RepresentationType.MAP.toString()) {
            @Override
            protected void serialize(final MappingSerializer serializer) {
	
//	public static String convert(JadeNode jadeNode, boolean bl) {

//		StringBuffer ret = new StringBuffer("{");
					if (jadeNode.getName() != null) {
						serializer.putString("name", jadeNode.getName());
//						ret.append(" \"name\": \"" + this.getName() + "\"");
					} else {
						serializer.putString("name", "");
//						ret.append(" \"name\": \"\"");
					}
					
					
/*					if (jadeNode.getObject(NodeProperty.PROCESSED_BY_TNRS.name) != null) {
						serializer.putBoolean(NodeProperty.PROCESSED_BY_TNRS.name, true);
					} */
					
					// add properties suitable for the JSON
					for (NodeProperty property : OTUConstants.VISIBLE_JSON_TREE_PROPERTIES) {
						if (jadeNode.getObject(property.name) != null) {
							
							if (property.type == int.class || property.type == Integer.class) {
								serializer.putNumber(property.name, (Integer) jadeNode.getObject(property.name));

							} else if (property.type == double.class || property.type == Double.class) {
								serializer.putNumber(property.name, (Double) jadeNode.getObject(property.name));

							} else if (property.type == Boolean.class || property.type == boolean.class) {
								serializer.putBoolean(property.name, (Boolean) jadeNode.getObject(property.name));
								
							} else if (property.type == String.class) {
								serializer.putString(property.name, (String) jadeNode.getObject(property.name));

							} else if (property.type.isArray()) {
								serializer.putList(property.name, OpentreeRepresentationConverter.getListRepresentation(new IterableArray(jadeNode.getObject(property.name))));

							} else {
								serializer.putString(property.name, jadeNode.getObject(property.name).toString());
							}
						}
					}
					
					
					/*
					if (jadeNode.getObject("nodeid") != null) {
						serializer.putString("nodeid", "");
//						ret.append("\n, \"nodeid\": \"" + this.getObject("nodeid") + "\""); // note: what is the difference between this version and the capitalized one below?
					} */

					serializer.putList("children", OpentreeRepresentationConverter.getListRepresentation(jadeNode.getChildren()));
/*					for (int i = 0; i < jadeNode.getChildCount(); i++) {
						if (i == 0) {
							ret.append("\n, \"children\": [\n");
						}
						ret.append(getJadeNodeRepresentation(jadeNode.getChild(i), bl));
						if (i == this.getChildCount() - 1) {
							ret.append("]\n");
						} else {
							ret.append(",\n");
						}
					} */
					Object hc = jadeNode.getObject("haschild");
					if (hc != null) {
						int nc = (Integer)jadeNode.getObject("numchild");
						if(nc > jadeNode.getChildCount()){
							serializer.putBoolean("notcomplete", true);
//							ret.append(", \"notcomplete\": 1");
						}
					}

					/* not supported because of annoyance of trying to pass the boolean to the getListRepresentation method
					
					if (useBranchLengths) {
						serializer.putNumber("bl", jadeNode.getBL());
					}
					
					*/

					/*					Object sup = this.getObject("supporting_sources");
					if (sup != null) {
			//			ret.append(", \"supportedBy\": ");
			//			JSONExporter.writeStringArrayAsJSON(ret, (String []) sup);
						int sz = 0;
						for (int i=0;i<((String []) sup).length;i++){
							if ((((String []) sup)[i]).equals("taxonomy"))
								continue;
							else
								sz++;
						}
						ret.append(", \"size\": "+sz);
					} */
//					if(this.getObject("nodeId") != null){
//						ret.append(", \"id\":"+this.getObject("nodeId"));
//					}
					serializer.putNumber("id", (Long) jadeNode.getObject("nodeId"));
					Object ig = jadeNode.getObject("ingroup");
					if(ig != null){
//						ret.append(", \"ingroup\": 1");
						serializer.putBoolean("ingroup", true);
					}
//					ret.append("}");
//					return ret.toString();
			}
        };
    }
}
