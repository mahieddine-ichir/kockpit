package org.kockpit.backend.services.search;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Page
 */
@Data
@Builder
public class Page {

    @JsonProperty("total_count")
  private Long totalCount;

  private Long size;

  private List<Object> items;
}

