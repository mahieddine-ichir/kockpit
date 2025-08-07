# WCP Rest Api config local resolver

This library configure a custom LocaleResolver witch base on a custom LocalesReferential

## LocalesReferential

The custom resolver use a custom LocalesReferential

To use it, you need to Implements interface
``` com.accor.wcp.web.rest.config.localresolver.LocalesReferential ``` otherwhise a default one
``` com.accor.wcp.web.rest.config.localresolver.DefaultLocalesReferential ``` will be used.

## Include Dependency

```
    <dependency>
        <groupId>com.accor.wcp</groupId>
        <artifactId>wcp-rest-api-config-localeresolver</artifactId>
        <version>${wcp.bom.version}</version>
    </dependency>
```

- Can be used with library [wcp-rest-api-filter-contentlanguage]