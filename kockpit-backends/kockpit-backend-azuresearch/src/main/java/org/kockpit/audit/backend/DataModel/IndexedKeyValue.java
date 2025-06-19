package org.kockpit.audit.backend.DataModel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.Date;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public final class IndexedKeyValue {

  private  String key;

  private String value;

  private Integer valueInteger;

  private Float valueFloat;

  private Date valueDate;

  public static IndexedKeyValue of(String key, String value) {
    return new IndexedKeyValue(key, value);
  }

  public static IndexedKeyValue of(String key, Integer value) {
    return new IndexedKeyValue(key, value);
  }

  public static IndexedKeyValue of(String key, Float value) {
    return new IndexedKeyValue(key, value);
  }

  public static IndexedKeyValue of(String key, Date value) {
    return new IndexedKeyValue(key, value);
  }

  public static IndexedKeyValue of(String key, Object value) {
    return new IndexedKeyValue(key, value);
  }

  @Deprecated(forRemoval = true, since = "2.3.1")
  public IndexedKeyValue(String key, String value) {
    this.key = key;
    this.value = value;
  }

  private IndexedKeyValue(String key, Integer integer) {
    this.key = key;
    this.valueInteger = integer;
  }

  private IndexedKeyValue(String key, Float f) {
    this.key = key;
    this.valueFloat = f;
  }

  private IndexedKeyValue(String key, Date date) {
    this.key = key;
    this.valueDate = date;
  }

  @Deprecated(forRemoval = true, since = "2.3.1")
  public IndexedKeyValue(String key, Object value) {
    this.key = key;
    // For temp retro-compatibility
    this.value = "" + value;
    if (value instanceof Integer) {
      valueInteger = (Integer) value;
    } else if (value instanceof Long) {
      valueInteger = ((Long) value).intValue();
    } else if (value instanceof Float) {
      valueFloat = (Float) value;
    } else if (value instanceof Double) {
      valueFloat = ((Double) value).floatValue();
    } else if (value instanceof Date) {
      valueDate = (Date) value;
    }
  }
}
