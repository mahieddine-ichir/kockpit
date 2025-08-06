package com.accor.wcp.web.rest.problem;

import jakarta.validation.Valid;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
class FakeController {

    @GetMapping("/requestHeader")
    public ResponseEntity<Object> getRequestHeader(@RequestHeader(value = "testHeaderException") String param) {
        return ResponseEntity.ok("test request header");
    }

    @GetMapping("/queryParam")
    public ResponseEntity<Object> getQueryParam(@RequestParam String param) {
        return ResponseEntity.ok("test query param");
    }

    @GetMapping("/requestBody")
    public ResponseEntity<Object> getValidBody(@Valid @RequestBody FakeUser user) {
        return ResponseEntity.ok("test valid request body is not blank");
    }

    @GetMapping("/typeMismatch")
    public ResponseEntity<Object> getTypeMismatch() {
        throw new TypeMismatchException("TypeMismatch", String.class);
    }

    @GetMapping("/methodArgTypeMismatch/{id}")
    public ResponseEntity<Object> getMethodArgTypeMismatch(@PathVariable("id") Long id) {
        return ResponseEntity.ok("test MethodArgTypeMismatchException");
    }

    @GetMapping("/nullPointerException")
    public ResponseEntity<Object> getNullPointer() {
        throw new NullPointerException();
    }

    @GetMapping("/httpMediaTypeNotSupportedException")
    public ResponseEntity<Object> getMediaTypeNotSupported() throws HttpMediaTypeNotSupportedException {
        throw new HttpMediaTypeNotSupportedException("Http media type not supported");
    }
}
