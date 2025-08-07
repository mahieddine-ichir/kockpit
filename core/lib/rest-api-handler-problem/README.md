# WCP Rest Api handler problem

This library handles custom and all spring MVC raised exceptions by returning a ResponseEntity with
RFC 7807 formatted error details in the body.

inherits
from ``` org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler ```

Manage a custom body error response

# Dependency

```
    <dependency>
        <groupId>com.accor.wcp</groupId>
        <artifactId>wcp-rest-api-handler-problem</artifactId>
        <version>${wcp.bom.version}</version>
    </dependency>
```

# Parameter can be overridden

```
  wcp.web.incoming.header.traceid:X-WCP-TraceId
```

```
  wcp.web.rest.api.handler.error.codes.validation-method-argument-invalid:INPUT_VALIDATION_METHODARGINVALID
```

```
  wcp.web.rest.api.handler.error.titles.validation-method-argument-invalid:Method argument not valid
```

```
  wcp.web.rest.api.handler.error.details.validation-method-argument-invalid:Input validation error, parser raised some issues that are defined in fieldErrors array
```

```
  wcp.web.rest.api.handler.error.codes.validation-method-argument-invalid:INPUT_JSON_INVALID
```

```
  wcp.web.rest.api.handler.error.titles.validation-method-argument-invalid:Invalid json message received
```

```
  wcp.web.rest.api.handler.error.codes.unsupported-media-type:UNSUPPORTED_MEDIA_TYPE
```

# Exceptions

## MethodArgumentNotValidException

- HttpsStatus : 400

- Response :

```
    {
      "type": "about:blank",
      "title": "Method argument not valid",
      "status": 400,
      "detail": "Input validation error, parser raised some issues that are defined in fieldErrors array",
      "instance": "/requestBody",
      "fieldErrors": [
        {
          "objectName": "fakeUser",
          "field": "name",
          "message": "must not be blank"
        }
      ],
      "code": "INPUT_VALIDATION_METHODARGINVALID"
    }

```

## MethodArgumentTypeMismatchException

- HttpsStatus : 400

- Response :

```
    {
        "type": "about:blank",
        "title": "Bad Request",
        "status": 400,
        "detail": "Property 'id' with value 'exception' is not a valid Long",
        "instance": "/methodArgTypeMismatch/exception",
        "code": "INPUT_VALIDATION_METHODARGINVALID"
    }

```

## MissingRequestHeaderException

- HttpsStatus : 400

- Response :

```
    {
      "type": "about:blank",
      "title": "Method argument not valid",
      "status": 400,
      "detail": "Input validation error, parser raised some issues that are defined in fieldErrors array",
      "instance": "/requestHeader",
      "fieldErrors": [
        {
          "objectName": "headers",
          "field": "testHeaderException",
          "message": "must not be null"
        }
      ],
      "code": "INPUT_VALIDATION_METHODARGINVALID"
    }
```

## HttpMessageNotReadableException

- HttpsStatus : 400

- Response :

```
    {
        "type": "about:blank",
        "title": "Invalid json message received",
        "status": 400,
        "detail": "JSON parse error: Cannot deserialize value of type `java.lang.Integer` from String \"aaaa\": not a valid `java.lang.Integer` value",
        "instance": "/requestBody",
        "code": "INPUT_JSON_INVALID"
    }
```

## HttpMediaTypeNotSupportedException

- HttpsStatus : 415

- Response :

```
    {
      "type": "about:blank",
      "title": "Unsupported Media Type",
      "status": 415,
      "detail": "Could not parse Content-Type.",
      "instance": "/httpMediaTypeNotSupportedException",
      "code": "UNSUPPORTED_MEDIA_TYPE"
    }
```

## Exception and RuntimeException

- HttpsStatus : 415

- Response :

```
    {
      "type": "about:blank",
      "title": "Internal Server error",
      "status": 500,
      "detail": "Unable to process data. Please contact Kockpit Platform Engineering - team.",
      "instance": "/nullPointerException",
      "code": "TECHNICAL_ERROR"
    }
```

