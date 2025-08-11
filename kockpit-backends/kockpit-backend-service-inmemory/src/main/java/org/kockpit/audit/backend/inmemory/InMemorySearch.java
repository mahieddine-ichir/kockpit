package org.kockpit.audit.backend.inmemory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.audit.backend.DomainApiDelegate;
import org.kockpit.audit.backend.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ReflectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.lang.Math.max;
import static java.lang.Math.min;

@RequiredArgsConstructor
@Slf4j
public class InMemorySearch implements DomainApiDelegate {

    private final ObjectMapper objectMapper;

    private List<Map<?, ?>> audits;

    @SneakyThrows
    @PostConstruct
    void init() {
        TypeReference<List<Map<?,?>>> typeReference = new TypeReference<>() {};
        audits = objectMapper.readValue(this.getClass().getResourceAsStream("/audits.json"), typeReference);
    }

    @Override
    public ResponseEntity<Object> auditsByIdForDomainAndEnv(String id, String domain, String env) {
        Optional<Map<?, ?>> auditById = audits.stream()
                .filter(audit -> {
                    Optional<Object> oId = read(audit, "id");
                    return oId.isPresent() && oId.get().equals(id);
                })
                .findFirst();
        return ResponseEntity.ok(auditById);
    }

    @Override
    public ResponseEntity<Page> searchAudits(String query, String domain, String env, Integer start, Integer size) {
        List<Map<?, ?>> list = audits.stream().filter(audit ->
                        audit.values().stream()
                                .anyMatch(v -> search(query, v)))
                .toList();

        List<Map<?, ?>> subList = list.subList(min(start, list.size()), min(start + size, list.size()));

        return ResponseEntity.ok(Page.builder()
                        .size((long) subList.size())
                        .totalCount((long) list.size())
                        .items(new ArrayList<>(list))
                .build());
    }

    private boolean search(String query, Object object) {
        if (object == null) {
            return false;
        }
        if (object instanceof String s) {
            return s.toLowerCase().contains(query.toLowerCase());
        } else if  (object instanceof Map map) {
            return map.values().stream().anyMatch(v -> search(query, v));
        } else if  (object instanceof List<?> list) {
            return list.stream().anyMatch(v -> search(query, v));
        } else {
            log.warn("Unknown type {}", object.getClass());
            return false;
        }
    }

    private Optional<Object> read(Object object, String key) {
        if (object instanceof Map<?,?> map) {
            return Optional.ofNullable(map.get(key));
        } else {
            return Stream.of(object.getClass().getDeclaredFields())
                    .filter(field -> field.getName().equals(key))
                    .findFirst()
                    .map(field -> {
                        ReflectionUtils.makeAccessible(field);
                        return ReflectionUtils.getField(field, object);
                    });
        }
    }

    @Override
    public ResponseEntity<Page> listAudits(String domain, String env, Integer start, Integer size) {
        List<Map<?, ?>> subList = audits.subList(min(audits.size(), max(0, start)), min(audits.size(), start + size));
        return ResponseEntity.ok(Page.builder()
                        .items(new ArrayList<>(subList))
                        .totalCount((long) audits.size())
                        .size((long) subList.size())
                .build());
    }
}
