package org.neo4j.server.rest.repr;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class GeneralizedMappingRepresentation extends MappingRepresentation {

    public GeneralizedMappingRepresentation(RepresentationType type) {
        super(type);
    }

    public GeneralizedMappingRepresentation(String type) {
        super(type);
    }

    @Override
    protected void serialize(MappingSerializer serializer) {

    }
    
    public static MappingRepresentation getMapRepresentation(final Map<String, Object> data) {

        return new MappingRepresentation(RepresentationType.MAP.toString()) {
            @Override
            protected void serialize(final MappingSerializer serializer) {

                for (Map.Entry<String, Object> pair : data.entrySet()) {
                    
                    // TODO: extend the neo4j MappingSerializer (make a class called GeneralizedMappingSerializer) so it can use things other than strings for map keys

                    String key = pair.getKey();
                    Object value = pair.getValue();
                    
                    if (value instanceof Map) {
                        serializer.putMapping(key, (MappingRepresentation) getMapRepresentation((Map<String, Object>) value));

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
        };
    }
}
