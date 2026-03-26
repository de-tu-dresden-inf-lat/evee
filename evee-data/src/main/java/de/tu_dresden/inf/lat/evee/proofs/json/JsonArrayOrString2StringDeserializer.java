package de.tu_dresden.inf.lat.evee.proofs.json;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class JsonArrayOrString2StringDeserializer extends JsonDeserializer<String> {

    @Override
    public String deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException, MismatchedInputException {
        JsonNode node = (JsonNode) parser.readValueAsTree();

        if (node.isValueNode() && node.isTextual()) {
                return node.asText();
        } 
        else if (node.isArray()) {
            JsonNode valueNode = ((ArrayNode)node).get(0);
            if (valueNode == null)
                return "";

            return valueNode.asText();   
        }

        throw MismatchedInputException.from(parser, String.class,
                "Expected input to be of type String or String array, but got " + node.getNodeType().toString());
        
    }
    
}