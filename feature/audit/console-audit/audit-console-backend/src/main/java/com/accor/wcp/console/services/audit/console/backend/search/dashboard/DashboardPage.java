package com.accor.wcp.console.services.audit.console.backend.search.dashboard;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DashboardPage {

    private List<DashboardSearchResultDto> items;

    private Long totalSize;

    private Integer size;

    private Integer from;
}
