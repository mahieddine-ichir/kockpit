package com.accor.wcp.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;
import org.junit.jupiter.api.Test;

class IndexedKeyValueTest {

  @Test
  void testOfString() {
    IndexedKeyValue indexedKeyValue = IndexedKeyValue.of("key", "string value");
    assertEquals("string value", indexedKeyValue.getValue());
    assertNull(indexedKeyValue.getValueDate());
    assertNull(indexedKeyValue.getValueInteger());
    assertNull(indexedKeyValue.getValueFloat());
  }

  @Test
  void testOfObject() {
    Object str = "string value";
    IndexedKeyValue indexedKeyValue = IndexedKeyValue.of("key", str);
    assertEquals(str, indexedKeyValue.getValue());
    assertNull(indexedKeyValue.getValueDate());
    assertNull(indexedKeyValue.getValueInteger());
    assertNull(indexedKeyValue.getValueFloat());
  }

  @Test
  void testOfInteger() {
    IndexedKeyValue indexedKeyValue = IndexedKeyValue.of("key", 123456);
    assertNull(indexedKeyValue.getValue());
    assertEquals(123456, indexedKeyValue.getValueInteger());
    assertNull(indexedKeyValue.getValueDate());
    assertNull(indexedKeyValue.getValueFloat());
  }

  @Test
  void testOfDate() {
    Date value = new Date();
    IndexedKeyValue indexedKeyValue = IndexedKeyValue.of("key", value);
    assertNull(indexedKeyValue.getValue());
    assertEquals(value, indexedKeyValue.getValueDate());
    assertNull(indexedKeyValue.getValueInteger());
    assertNull(indexedKeyValue.getValueFloat());
  }

  @Test
  void testOfFloat() {
    float value = 1.435983848484f;
    IndexedKeyValue indexedKeyValue = IndexedKeyValue.of("key", value);
    assertNull(indexedKeyValue.getValue());
    assertEquals(value, indexedKeyValue.getValueFloat());
    assertNull(indexedKeyValue.getValueDate());
    assertNull(indexedKeyValue.getValueInteger());
  }
}
