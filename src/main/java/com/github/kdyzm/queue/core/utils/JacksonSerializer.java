package com.github.kdyzm.queue.core.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author kdyzm
 */
public class JacksonSerializer {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public String serialize(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T deserialize(String rawStr, Class<T> klass) {
        if (null == rawStr || rawStr.length() == 0) {
            return null;
        }

        try {
            T tobj = objectMapper.readValue(rawStr, klass);
            return tobj;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public String addObjToJson(String json, String key, Object obj){
    	try {
	    	ObjectNode node = objectMapper.createObjectNode();
	    	JsonNode rootNode = objectMapper.readTree(json);
	    	node.setAll((ObjectNode)rootNode);
	        node.putPOJO(key, obj);
	        return objectMapper.writeValueAsString(node);
    	} catch (Exception e) {
              throw new RuntimeException(e);
        }
    }
}
