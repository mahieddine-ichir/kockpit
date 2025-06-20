package org.kockpit.audit.backend.model;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.util.*;

public class AuditMapDeserializer extends StdDeserializer<List<Audit>>{
    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);

    public AuditMapDeserializer() {
        this(null);
    }

    protected AuditMapDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public List<Audit> deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JacksonException {
        List<Audit> audits = new ArrayList<>();
        JsonNode node = jp.getCodec().readTree(jp);

        if (node.isArray()) {
            for (JsonNode auditNode : node) {
                if (auditNode.has("httpAuditedRequest")) {
                    Audit audit = mapper.treeToValue(auditNode, HttpExchangeAudit.class);
                    audits.add(audit);
                }
            }
        }
        return audits;
    }
}
