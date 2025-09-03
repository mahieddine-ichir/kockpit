package com.example.insightsdashboardapi.dto;


import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class FiltersDto {
    private List<String> domains;
    private List<String> environments;
}
