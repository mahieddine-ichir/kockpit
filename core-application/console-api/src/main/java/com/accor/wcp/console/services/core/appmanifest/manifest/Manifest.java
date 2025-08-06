package com.accor.wcp.console.services.core.appmanifest.manifest;

import java.time.Instant;
import lombok.Getter;
import org.json.JSONObject;

@Getter
public class Manifest {
  private String name;
  private Instant lastModificationDate;
  private JSONObject content;
  private String source;

  public Manifest(String name, Instant lastModificationDate, JSONObject content, String source) {
    this.name = name;
    this.lastModificationDate = lastModificationDate;
    this.content = content;
    this.source = source;
  }
}
