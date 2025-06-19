package org.kockpit.audit.backend.DataModel;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

public class AuditDeserializer  extends JsonDeserializer<Audit> {
    @Override
    public Audit deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);
        JsonNode typeNode = node.get("type");
        if (typeNode == null) {
            throw new JsonParseException(jp, "Missing 'type' field");
        }
        String type = typeNode.asText();
        if (HttpExchangeAudit.TYPE.equals(type)) {
            return jp.getCodec().treeToValue(node, HttpExchangeAudit.class);
        } else {
            throw new JsonParseException(jp, "Unknown 'type' field: " + type);
        }
    }
}
