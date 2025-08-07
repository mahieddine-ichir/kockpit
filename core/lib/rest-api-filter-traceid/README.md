# WCP Rest Api Filter Trace ID

This library pass in header response the given param [X-B3-TraceId] if it's valid (match pattern [^[0-9a-f]{32}$])
otherwise generate new one match the pattern

## Override param trace-Id name

Can be overridden by the parameter default value is [X-B3-TraceId]

## Retrocompatibility

To assure Retrocompatibility , the parameter [wcp.web.rest.api.header.traceid.retrocompatibilty] must set to true

Then the header-name will be [X-WCP-TraceId]
The format will be a GUID format

```
 wcp.web.incoming.header.traceid
```

## include dependency

```
 <dependency>
    <groupId>com.accor.wcp</groupId>
    <artifactId>wcp-rest-api-filter-traceid</artifactId>
    <version>${wcp.bom.version}</version>
</dependency>
```


