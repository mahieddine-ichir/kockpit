**Mapping V2** :
- Remove all text field since we not use full text search
- "type" : "object" and "enabled" : false
   * The enabled setting, which can be applied only to the top-level mapping definition and to object fields, causes Elasticsearch to skip parsing of the contents of the field entirely. The JSON can still be retrieved from the _source field, but it is not searchable or stored in any other way
- "index" : false 
  * Should the field be searchable? Accepts true (default) or false
- "doc_values" : false
  * Should the field be stored on disk in a column-stride fashion, so that it can later be used for sorting, aggregations, or scripting? Accepts true (default) or false. 

**Mapping V3** :
- Remove "doc_values" : false on field date to fix error on kibana when discover -> Can't load fielddata on [indexedExtensions.indexedKeyValues.valueDate] because fielddata is unsupported on fields of type [date]. Use doc values instead.
  (seems to be a bug)

**Mapping V4** :
- Set Number shard to 1 and replica to 0

-------------------------------------------------
**Policy V1** :
- Add rollover min_index_age = 1d
- Add delete after 30d by default (nb days overloadable)

**Policy V2** :
- Add rollover min_size = 30gb
