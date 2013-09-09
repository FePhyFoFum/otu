package org.neo4j.server.rest.repr;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
public class GeneralizedMappingRepresentation extends MappingRepresentation {

    public GeneralizedMappingRepresentation(RepresentationType type) {
        super(type);
        // TODO Auto-generated constructor stub
    }

    public GeneralizedMappingRepresentation(String type) {
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

    public static MappingRepresentation getMapRepresentation(final Map<String, Object> data) {

        return new MappingRepresentation(RepresentationType.MAP.toString()) {
            @Override
            protected void serialize(final MappingSerializer serializer) {

                for (Map.Entry<String, Object> pair : data.entrySet()) {
                    
                    // TODO: extend serializer so it can use things other than strings for map keys

                    String key = pair.getKey();
                    Object value = pair.getValue();
//                    Object valueConverted = ;
                    
//                    JSONObject parsedValue = null;
/*                    if (value instanceof String) {
                        serializer.putString(key, (String) value);

/*                    	try {
                    		JSONParser parser = new JSONParser();
                    		Map json = (Map) parser.parse((String) value);
                    		serializer.putMapping(key, (MappingRepresentation) OpentreeRepresentationConverter.convert((JSONObject) json));
 
                    	} catch (ParseException ex) {
                    		
                    		// horrible...
	                        serializer.putString(key, (String) value);
                    	}
/*                    	boolean isJSON = false;
                    	for (int i = 0; i < ((String) value).length(); i++) { // this isn't working but this is how we should do it....
                    		if (Character.isWhitespace(((String)value).charAt(i))) {
                    			continue;
                    		} else if (((String) value).charAt(i) == '{') {
    	                        serializer.putMapping(key, (MappingRepresentation) OpentreeRepresentationConverter.convert((JSONObject) value));
                            	isJSON = true;
                    			break;
                        	} else if (((String) value).charAt(i) == '[') {
    	                        serializer.putList(key, (ListRepresentation) OpentreeRepresentationConverter.convert((JSONObject) value));
                            	isJSON = true;
                    			break;
                    		} else {
                    			break;
                    		}
                    	}                    		
                		if (!isJSON) {
	                        serializer.putString(key, (String) value);
                		}
                    } else { */
                    
	                    if (value instanceof Map) {
	                        serializer.putMapping(key, (MappingRepresentation) OpentreeRepresentationConverter.convert(value));
	
	                    } else if (value instanceof List) {
	                        serializer.putList(key, (ListRepresentation) OpentreeRepresentationConverter.convert(value));
	
	                    } else if (value instanceof Boolean) {
	                        serializer.putBoolean(key, (Boolean) value);
	
	                    } else if (value instanceof Float || value instanceof Double || value instanceof Long || value instanceof Integer) {
	                        serializer.putNumber(key, (Number) value);

	                    } else if (value instanceof String) {
	                    	serializer.putString(key, (String) value);
	
	                    } else if (value.getClass().isArray()) {
                        	serializer.putString(key, Arrays.toString((Object[]) value));

	                    } else {
	                    	serializer.putString(key, value.toString());
	                    }
                    }
                }
//            }
        };
    }
}
