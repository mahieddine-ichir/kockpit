package com.accor.wcp.sdk.service.cache.communication;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CacheResetStatsMessageRequestDto implements Serializable {
  @Builder.Default private String __type__ = CacheResetStatsMessageRequestDto.class.getName();
  private String cache;
}
