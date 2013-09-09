package org.neo4j.server.rest.repr;

import jade.IterableArray;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.neo4j.helpers.collection.FirstItemIterable;
import org.neo4j.helpers.collection.IterableWrapper;
import org.neo4j.helpers.collection.IteratorWrapper;
import org.neo4j.server.rest.repr.ListRepresentation;
import org.neo4j.server.rest.repr.MappingRepresentation;
import org.neo4j.server.rest.repr.Representation;
import org.neo4j.server.rest.repr.RepresentationType;
import org.neo4j.server.rest.repr.ValueRepresentation;

/**
 * This class was supposed to serialize a JSON object as JSON. This would facilitate returning results from other web services in
 * order to circumvent the single origin protocol. Unfortunately I haven't been able to figure out how to do this yet, and the
 * errors I'm getting from the server are uninformative. So I am abandoning it for now. It would be easier to debug using a direct
 * command-line interation so the stack traces refer to this class rather than the neo4j serialization classes themselves. Will require
 * some craziness to get there though.
 * 
 * @author cody
 *
 */
@Deprecated
public class JSONToRepresentationConverter {

	/**
	 * Serialization methods for JSONObjects
	 * 
	 * @param JSONObject json
	 * @return serializable Representation of json 
	 */
    public static Representation convert(final JSONObject json)
    {

    	// determine the approach to conversion by the type of the data to be converted
    	// start with specific object classes that might be observed, and move to more general
    	// types of containers if the data doesn't match a specific type
    	
    	if (json instanceof Iterable) {
        	return getListRepresentation( (Iterable) json);
        
        } else if (json instanceof Iterator) {
        	Iterator iterator = (Iterator) json;
            return getIteratorRepresentation(iterator);
        
        } else if (json instanceof Map) {
            return getMapRepresentation( (Map) json );
            
        } else {
        	return getSingleRepresentation(json);

        }

    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  public conversion methods for specific data types below here
    //
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static MappingRepresentation getMapRepresentation(final Map<String, Object> data) {

        return new MappingRepresentation(RepresentationType.MAP.toString()) {
            @Override
            protected void serialize(final MappingSerializer serializer) {

                for (Map.Entry<String, Object> pair : data.entrySet()) {
                    
                    // TODO: extend MappingSerializer so it can use things other than strings for map keys

                    String key = pair.getKey();
                    Object value = pair.getValue();

                    if (value instanceof String) {

                    	// attempt to parse strings as json
                    	try {
                    		
                    		JSONParser parser = new JSONParser();
                    		JSONObject json = (JSONObject) parser.parse((String) value);

                    		value = json;
 
                    	// horrible... better to sniff arrays and maps so we don't have to deal with exceptions and try to parse every single string
                    	} catch (ParseException ex) {
	                        value = "attempt to parse this string (in map serializer) failed: '" + ((String)value).substring(0,50) + "'";

                    	}
                    	
                    	/*
                    	 
                    	 // this isn't working but this is probably how we should be checking for json....
                    	  
                    	boolean isJSON = false;
                    	for (int i = 0; i < ((String) value).length(); i++) {
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
                		*/
                    	
                }
                
                    if (value instanceof Map) {
                        serializer.putMapping(key, (MappingRepresentation) getMapRepresentation((Map) value));

                    } else if (value instanceof Iterable) {
                        serializer.putList(key, (ListRepresentation) getListRepresentation((Iterable) value));

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
        };
    }
    
    /**
     * Return a serialization of a general Iterable type
     * @param data
     * @return
     */
    public static ListRepresentation getListRepresentation(Iterable data) {
        final FirstItemIterable<Representation> results = convertValuesToRepresentations(data);
        return new ListRepresentation(getType(results), results);
    }
    
    public static ListRepresentation getArrayRepresentation(Object[] data) {
    	
    	// if this doesn't work then just copy all the values from the array into a linkedlist and call the convert function on that
        final FirstItemIterable<Representation> results = convertValuesToRepresentations(new IterableArray(data));
        return new ListRepresentation(getType(results), results);
    }


    
    ///////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  internal conversion methods below here
    //
    ///////////////////////////////////////////////////////////////////////////////////////////////
    
    static Representation getIteratorRepresentation(Iterator data) {
        final FirstItemIterable<Representation> results = new FirstItemIterable<Representation>(
                new IteratorWrapper<Representation, Object>(data) {
                    @Override
                    protected Representation underlyingObjectToObject(Object value) {
                        if (value instanceof Iterable)
                        {
                            FirstItemIterable<Representation> nested = convertValuesToRepresentations((Iterable) value);
                            return new ListRepresentation(getType(nested), nested);
                        } else {
                            return getSingleRepresentation(value);
                        }
                    }
                }
                );
        return new ListRepresentation(getType(results), results);
    }

    static FirstItemIterable<Representation> convertValuesToRepresentations(Iterable data) {
        /*
         * if ( data instanceof Table ) { return new FirstItemIterable<Representation>(Collections.<Representation>singleton(new GremlinTableRepresentation(
         * (Table) data ))); }
         */
        return new FirstItemIterable<Representation> (
                new IterableWrapper<Representation, Object>(data) {

                    @Override
                    protected Representation underlyingObjectToObject(Object value) {
                    	
                    	if (value instanceof Iterable) {
                            final FirstItemIterable<Representation> nested = convertValuesToRepresentations((Iterable) value);
                            return new ListRepresentation(getType(nested), nested);

                    	} else if (value instanceof String) {

                    		JSONObject json = null;
                        	try {
                        		JSONParser parser = new JSONParser();
                        		json = (JSONObject) parser.parse((String) value);
     
                        	// horrible... better to sniff arrays and maps so we don't try to parse every single string... see getSingleRepresentation for example
                        	} catch (ParseException ex) {
    	                        return getSingleRepresentation("attempt to parse this string (in iterable serializer) failed: '" + ((String)value).substring(0,50) + "'");

                        	}
                            	
                        	if (json instanceof Iterable) {
                                final FirstItemIterable<Representation> nested = convertValuesToRepresentations((Iterable) json);
                                return new ListRepresentation(getType(nested), nested);
                            	
                            /* } else if (json instanceof Map) {
                                final FirstItemIterable<Representation> nested = convertValuesToRepresentations((Iterable) json);
                                return new ListRepresentation(getType(nested), nested); */

	                    	} else {
	                            return getSingleRepresentation(value);
	                    	}
                    	
                    	} else {
                            return getSingleRepresentation(value);
                            
                        }
                    }
                });
    }

    /**
     * Infer the type of the objects contained by the Iterable `representationIter`, or if `representationIter` has no elements in its iterator,
     * then return the type of `representationIter` itself. Used by other converter methods to sniff the datatypes of elements in containers so that the appropriate
     * converter methods can be called.
     * @param representationIter
     * @return type of elements in 
     */
    static RepresentationType getType(FirstItemIterable<Representation> representationIter)
    {
        Representation representation = representationIter.getFirst();
        
        if (representation == null) {
            return RepresentationType.STRING;
        }
        
        return representation.getRepresentationType();
    }
    
    /**
     * Return a Representation object that represents `data`. Representation objects are required by the RepresentationConverter serialization methods, so all objects to
     * be serialized (including primitives) must be converted to a Representation; this method provides that functionality.
     * @param data
     * @return Representation object for data
     */
    static Representation getSingleRepresentation(Object value)
    {
        if (value == null) {
            return ValueRepresentation.string("null");        
            
        } else if (value instanceof String) {

        	// attempt to parse strings as json
        	try {
        		JSONParser parser = new JSONParser();
        		JSONObject json = (JSONObject) parser.parse((String) value);

        		value = json;

        	// horrible... better to sniff arrays and maps so we don't have to catch exceptions and try to parse every single string
        	} catch (ParseException ex) {
                value = "attempt to parse this string (in map serializer) failed: '" + ((String)value).substring(0,50) + "'";

        	}

        	/* this is not working but this is probably how we should be doing this.

 			for (int i = 0; i < ((String) data).length(); i++) {
        		if (Character.isWhitespace(((String)data).charAt(i)) || ((String) data).charAt(i) == '"') {
        			continue;
        		} else if (((String) data).charAt(i) == '{') {
                    return OpentreeRepresentationConverter.getMapRepresentation((JSONObject) data);
            	} else if (((String) data).charAt(i) == '[') {
                    return OpentreeRepresentationConverter.getListRepresentation((JSONArray) data);
        		} else {
        			break;
        		}
        	}
        	return ValueRepresentation.string((String) data); */

        }
        
        // now actually convert the value
        
        if (value instanceof Map) {
            return (MappingRepresentation) getMapRepresentation((Map) value);

        } else if (value instanceof Iterable) {
            return (ListRepresentation) getListRepresentation((Iterable) value);

        } else if (value instanceof Boolean) {
            return ValueRepresentation.bool((Boolean) value);

        } else if (value instanceof Double || value instanceof Float) {
            return ValueRepresentation.number(((Number) value).doubleValue());
        
        } else if (value instanceof Long) {
            return ValueRepresentation.number(((Long) value).longValue());

        } else if (value instanceof Integer) {
            return ValueRepresentation.number(((Integer) value).intValue());
        
        } else if (value.getClass().isArray()) {
        	return (ListRepresentation) getArrayRepresentation((Object[]) value);

        } else {
        	return ValueRepresentation.string(value.toString());
        }
    }
    

}