package com.accor.wcp.web.rest.config;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonMappingException.Reference;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Order
@ControllerAdvice
@Slf4j
public class JsonParseErrorHandler {
  private static final String ERROR_TITLE_INVALID_JSON = "Invalid json message received";
  private static final String ERROR_DETAIL_INVALID_JSON =
      "JSON parse error : you did not provide a valid JSON file";

  private static final String ERROR_TITLE_METHOD_ARGUMENT_INVALID = "Method argument not valid";

  private static final String ERROR_DETAIL_METHOD_ARGUMENT_INVALID =
      "Input validation error, parser raised some issues that are defined in fieldErrors array";

  private static final int ERROR_STATUS = 400;

  @Value(
      "${wcp.web.rest.api.handler.error.codes.validation-method-argument-invalid:INPUT_JSON_INVALID}")
  private String invalidJsonBodyCode;

  @Value(
      "${wcp.web.rest.api.handler.error.codes.validation-method-argument-invalid:INPUT_VALIDATION_METHODARGINVALID}")
  private String validationMethodArgumentInvalidCode;

  @Value(
      "${wcp.web.rest.api.handler.error.codes.validation-invalid-type-id:INPUT_VALIDATION_INVALIDTYPEID}")
  private String validationInvalidTypeId;

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorMessage> handleJsonParseExceptions(
      HttpMessageNotReadableException ex) {
    ErrorMessage errorMessage =
        ErrorMessage.builder()
            .title(ERROR_TITLE_INVALID_JSON)
            .status(ERROR_STATUS)
            .detail(ERROR_DETAIL_INVALID_JSON)
            .code(invalidJsonBodyCode)
            .build();

    String msg;
    Throwable cause = ex.getCause();

    if (cause instanceof JsonParseException jpe) {
      errorMessage = handleJsonParseException(jpe);
    } else if (cause instanceof InvalidFormatException ife) {
      errorMessage = handleInvalidFormatException(ife);
    } else if (cause instanceof InvalidTypeIdException ite) {
      errorMessage = handleInvalidTypeIdException(ite);
    }

    // special case of JsonMappingException below, too much class detail in error messages
    else if (cause instanceof MismatchedInputException mie) {
      if (nonNull(mie.getPath()) && !mie.getPath().isEmpty()) {
        msg = "Invalid request field: " + mie.getPath().get(0).getFieldName();
      }

      // just in case, haven't seen this condition
      else {
        msg = "Invalid request message";
      }

      errorMessage.setDetail(msg);
    } else if (cause instanceof JsonMappingException jme) {
      msg = jme.getOriginalMessage();
      if (nonNull(jme.getPath()) && !jme.getPath().isEmpty()) {
        msg = "Invalid request field: " + jme.getPath().get(0).getFieldName() + ": " + msg;
      }

      errorMessage.setDetail(msg);
    }

    // For log outputs
    MDC.put("errorCode", errorMessage.getCode());
    MDC.put("errorDetails", StringEscapeUtils.escapeJson(errorMessage.getDetail()));

    return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
  }

  private ErrorMessage handleInvalidTypeIdException(InvalidTypeIdException ite) {
    String msg = ite.getOriginalMessage();
    if (nonNull(ite.getPath()) && !ite.getPath().isEmpty()) {
      msg =
          "Invalid type id: "
              + ite.getTypeId()
              + " in field: "
              + ite.getPath().get(0).getFieldName();
    }
    return ErrorMessage.builder()
        .code(validationInvalidTypeId)
        .title(ERROR_TITLE_INVALID_JSON)
        .status(ERROR_STATUS)
        .detail(msg)
        .build();
  }

  private ErrorMessage handleInvalidFormatException(InvalidFormatException ife) {
    String msg = ife.getOriginalMessage();
    return ErrorMessage.builder()
        .code(validationMethodArgumentInvalidCode)
        .title(ERROR_TITLE_METHOD_ARGUMENT_INVALID)
        .status(ERROR_STATUS)
        .detail(ERROR_DETAIL_METHOD_ARGUMENT_INVALID)
        .fieldErrors(computeFieldErrors(msg, ife).orElse(null))
        .build();
  }

  private ErrorMessage handleJsonParseException(JsonParseException jpe) {
    log.warn("Error parsing input json. Error message: {}", jpe.getOriginalMessage());
    return ErrorMessage.builder()
        .code(invalidJsonBodyCode)
        .title(ERROR_TITLE_INVALID_JSON)
        .status(ERROR_STATUS)
        .detail(ERROR_DETAIL_INVALID_JSON)
        .build();
  }

  private static Optional<List<FieldError>> computeFieldErrors(
      String msg, MismatchedInputException ife) {
    if (isNull(ife.getPath()) || ife.getPath().isEmpty()) {
      return Optional.empty();
    }

    List<FieldError> fieldErrors;
    Reference reference = ife.getPath().get(ife.getPath().size() - 1);
    String description = reference.getDescription();
    String[] splitStrings = description.split("\\.");
    fieldErrors =
        List.of(
            FieldError.builder()
                .field(reference.getFieldName())
                .message(msg)
                .objectName(splitStrings[splitStrings.length - 1])
                .build());
    return Optional.of(fieldErrors);
  }
}
