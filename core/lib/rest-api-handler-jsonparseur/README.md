# WCP Rest Api handler json parser

This library provide error json parser errors handling

# Dependency

```
    <dependency>
        <groupId>com.accor.wcp</groupId>
        <artifactId>wcp-rest-api-handler-jsonparseur</artifactId>
        <version>${wcp.bom.version}</version>
    </dependency>
```

# Parameter can be overridden

```
  wcp.web.rest.api.handler.error.codes.validation-method-argument-invalid:INPUT_JSON_INVALID
```

```
  wcp.web.rest.api.handler.error.codes.validation-method-argument-invalid:INPUT_VALIDATION_METHODARGINVALID
```

```
  wcp.web.rest.api.handler.error.codes.validation-invalid-type-id:INPUT_VALIDATION_INVALIDTYPEID
```

# Handling Exception ``` org.springframework.http.converter.HttpMessageNotReadableException ```

## JsonParseException

- HttpsStatus : 400

- Response :

```
    {
      "title": "Invalid json message received",
      "status": 400,
      "detail": "JSON parse error : you did not provide a valid JSON file",
      "code": "INPUT_JSON_INVALID",
      "fieldErrors": null
    }

```

## InvalidFormatException

- HttpsStatus : 400

- Response :

```
    {
      "title": "Method argument not valid",
      "status": 400,
      "detail": "Input validation error, parser raised some issues that are defined in fieldErrors array",
      "code": "INPUT_VALIDATION_METHODARGINVALID",
      "fieldErrors": [
        {
          "objectName": "FakeUser[\"age\"]",
          "field": "age",
          "message": "Cannot deserialize value of type `java.lang.Integer` from String \"aaaaa\": not a valid `java.lang.Integer` value"
        }
      ]
    }
```

## InvalidTypeIdException

- HttpsStatus : 400

- Response :

```
    {
      "title": "Invalid json message received",
      "status": 400,
      "detail": "Invalid type id: null in field: origin",
      "code": "INPUT_VALIDATION_INVALIDTYPEID",
      "fieldErrors": null
    }
```
