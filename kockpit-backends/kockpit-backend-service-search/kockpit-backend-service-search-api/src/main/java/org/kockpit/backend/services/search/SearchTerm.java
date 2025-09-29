package org.kockpit.backend.services.search;

import lombok.Data;

/**
 * SearchTerm
 */
@Data
public class SearchTerm {

  private String name;

  private String path;

  private Object value;
}

