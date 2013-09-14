package org.neo4j.server.rest.repr;

import java.util.Map;
import java.util.Iterator;

/**
 * Just added a static method for converting a string to object map. This class is currently unused.
 */
import org.neo4j.helpers.collection.PrefetchingIterator;

public class GeneralizedListRepresentation extends ListRepresentation {

	
	public GeneralizedListRepresentation(String type, Iterable<? extends Representation> content) {
		super(type, content);
	}

	public GeneralizedListRepresentation(RepresentationType type, Iterable<? extends Representation> content) {
		super(type, content);
	}
	
    public static ListRepresentation maps (final Iterable<Map<String, Object>> mapIterable) {
        return new ListRepresentation( RepresentationType.MAP, new Iterable<MappingRepresentation>() {
            
        	@Override
            public Iterator<MappingRepresentation> iterator() {
            	
                return new PrefetchingIterator<MappingRepresentation>() {
                    Iterator<Map<String, Object>> mapIter = mapIterable.iterator();

                    @Override
                    protected MappingRepresentation fetchNextOrNull() {
                    	if (mapIter.hasNext()) {
                    		return GeneralizedMappingRepresentation.getMapRepresentation(mapIter.next());
                    	} else {
                    		return null;
                    	}
                    }
                };
            }
        });
    }
}
