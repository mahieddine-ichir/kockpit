package com.accor.wcp.console.services.audit.console.backend.search.dashboard;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import org.mapstruct.Mapper;
import org.opensearch.search.SearchHit;

import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring")
public abstract class DashboardDtoMapper {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public abstract List<DashboardSearchResultDto> ofList(List<SearchHit> searchHits);

    public DashboardSearchResultDto toDto(SearchHit searchHit) {
        DashboardSearchResultDto dto = this.toDto(searchHit.getSourceAsMap());
        dto.setIndex(searchHit.getIndex());
        dto.setId(searchHit.getId());

        Object indexedExtensions = searchHit.getSourceAsMap().get("indexedExtensions");
        if (indexedExtensions != null) {
            Object indexedKeyValuesList = ((List<Map>) indexedExtensions).get(0);
            if (indexedKeyValuesList != null) {
                Object indexedKeyValues = ((Map) indexedKeyValuesList).get("indexedKeyValues");
                dto.setIndexedKeyValues((List<Map<String, Object>>) indexedKeyValues);
            }
        }
        return dto;
    }

    @SneakyThrows
    private DashboardSearchResultDto toDto(Map<String, Object> input) {
        return objectMapper.convertValue(input, DashboardSearchResultDto.class);
    }
}
