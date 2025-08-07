# WCP Rest Api Validation for X-Accept-Version

This library validate the header-paramters [X-Accept-Version]

A custom header (X-Accept-Version) allows you to preserve your URIs between versions though it is
effectively a duplicate of the content negotiation behavior implemented by the existing Accept
header

## Activate validation in application

- Property accept-version is mandatory and must contains the current version of the application :

```
 wcp.web.rest.api.validation.accept-version.version
```

- Example in yaml property file

 ```
wcp:
  web:
    rest:
      api:
        validation:
          accept-version:
            version: 1
```

- include dependency

```
 <dependency>
    <groupId>com.accor.wcp</groupId>
    <artifactId>wcp-rest-api-validation-acceptversion</artifactId>
    <version>${wcp.bom.version}</version>
</dependency>
```

## Overide X-Accept-Version name

You can override the X-Accept-Version version name by overriding this property, default is '
X-Accept-Version'

```
 wcp.web.rest.api.validation.accept-version.header-name
```

- Example in yaml property file

 ```
wcp:
  web:
    rest:
      api:
        validation:
          accept-version:
            header-name: New-Accept-Version-Name
```

### Response missing X-Accept-Version

Thrown a custom AcceptVersionMissingRequestHeaderException inherits from
MissingRequestHeaderException (from spring-web dependency)
when X-Accept-Version is null , empty or blank

- Return Http status

 ```
400 Bad Request
```

- Return in error message

 ```
Required header 'X-Accept-Version' is not present.
```

you can use additional library [wcp-rest-api-handler-problem] to manage global
MissingRequestHeaderException

### Response invalid x-Accept-Version

Expected error when x-Accept-Version invalid

- Return Http status

 ```
406 Not Acceptable
```

- Return message

 ```
{
  "title": "Not Acceptable",
  "status": 406,
  "detail": "Invalid major version passed in header X-Accept-Version (=invalid), accepted version is 1",
  "code": "WRONG_VERSION"
}
```


