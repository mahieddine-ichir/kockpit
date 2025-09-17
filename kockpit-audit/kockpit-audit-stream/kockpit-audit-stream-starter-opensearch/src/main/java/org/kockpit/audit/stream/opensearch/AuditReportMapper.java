//package org.kockpit.audit.stream.opensearch;
//
//import com.fasterxml.jackson.databind.DeserializationFeature;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
//import lombok.SneakyThrows;
//import org.kockpit.audit.stream.api.AuditReport;
//import org.kockpit.audit.stream.api.IndexedKeyValue;
//import org.mapstruct.Mapper;
//
//import java.time.Instant;
//import java.util.Map;
//
//@Mapper(componentModel = "spring")
//public interface AuditReportMapper {
//
//    ObjectMapper MAPPER = new ObjectMapper()
//            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
//            .registerModule(new JavaTimeModule());
//
//    SearchAuditReport map(AuditReport auditReport);
//
//    SearchIndexedKeyValue map(IndexedKeyValue indexedKeyValue);
//
//    default Long fromInstant(Instant instant) {
//        if (instant == null) {
//            return null;
//        }
//        return instant.toEpochMilli();
//    }
//
//    @SneakyThrows
//    default String toJson(Map<String, Object> map) {
//        return MAPPER.writeValueAsString(map);
//    }
//}
