package de.tu_dresden.inf.lat.evee.proofs.json;

import java.io.IOException;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class JsonArrayOrString2StringDeserializer extends JsonDeserializer<String> {

    @Override
    public String deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException, JacksonException {
        JsonNode node = (JsonNode) parser.readValueAsTree();

        if (node.isValueNode() && node.isTextual()) {
                return node.asText();
        } 
        else if (node.isArray()) {
            ArrayNode arrayNode = (ArrayNode) node;
            JsonNode valueNode = arrayNode.get(0);
            if (!arrayNode.isEmpty() && valueNode.isTextual()) {
                return valueNode.asText();
            }
        }

        throw MismatchedInputException.from(parser, String.class,
                "Expected input to be of type String or String array, but got " + node.getNodeType().toString());
        
    }
    
}