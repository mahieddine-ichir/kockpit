package com.accor.wcp.console.services.cache;

import lombok.Data;

@Data
class CacheSettingsDto {

  private String env;

  private String name;

  private String label;
}
