package com.accor.wcp.web.rest.problem;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.indexOf;
import static org.apache.commons.lang3.StringUtils.isBlank;

import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.MDC;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.validation.BindingResult;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import java.util.List;
import java.util.Optional;

@RestControllerAdvice
public class GlobalApiExceptionHandler extends ResponseEntityExceptionHandler {

  static final String WCP_TRACE_ID = "X-WCP-TraceId";
  private static final String FIELD_ERRORS_KEY = "fieldErrors";
  private static final String KEY_NAME_CODE = "code";
  public static final String KEY_LOG_NAME_ERROR_CODE = "errorCode";
  public static final String KEY_LOG_NAME_ERROR_DETAILS = "errorDetails";

  @Value("${wcp.web.incoming.header.traceid:X-WCP-TraceId}")
  private String traceIdHeaderName;

  @Value(
      "${wcp.web.rest.api.handler.error.codes.validation-method-argument-invalid:INPUT_VALIDATION_METHODARGINVALID}")
  private String validationMethodArgumentInvalidCode;

  @Value(
      "${wcp.web.rest.api.handler.error.titles.validation-method-argument-invalid:Method argument not valid}")
  private String validationMethodArgumentInvalidTitle;

  @Value(
      "${wcp.web.rest.api.handler.error.details.validation-method-argument-invalid:Input validation error, parser raised some issues that are defined in fieldErrors array}")
  private String validationMethodArgumentInvalidDetails;

  @Value(
      "${wcp.web.rest.api.handler.error.codes.validation-method-argument-invalid:INPUT_JSON_INVALID}")
  private String invalidJsonBodyCode;

  @Value(
      "${wcp.web.rest.api.handler.error.titles.validation-method-argument-invalid:Invalid json message received}")
  private String invalidJsonBodyTitle;

  @Value("${wcp.web.rest.api.handler.error.codes.unsupported-media-type:UNSUPPORTED_MEDIA_TYPE}")
  private String unsupportedMediaTypeCode;

  @Value("${wcp.web.rest.api.handler.error.codes.missing-query-param:MISSING_QUERY_PARAMETER}")
  private String missingQueryParam;

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    List<FieldError> fieldErrors = computeFieldErrors(ex);
    return validationMethodArgumentInvalidCodeResponseEntity(
        ex, headers, status, request, fieldErrors);
  }

  private ResponseEntity<Object> validationMethodArgumentInvalidCodeResponseEntity(
      MethodArgumentNotValidException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request,
      List<FieldError> fieldErrors) {
    headers = addTraceIdInResponseHeader(headers, request);
    ProblemDetail problemDetail =
        createProblemDetail(
            ex,
            status,
            validationMethodArgumentInvalidDetails,
            validationMethodArgumentInvalidCode,
            null,
            request);
    problemDetail.setTitle(validationMethodArgumentInvalidTitle);
    problemDetail.setProperty(FIELD_ERRORS_KEY, fieldErrors);
    problemDetail.setProperty(KEY_NAME_CODE, validationMethodArgumentInvalidCode);

    return createResponseEntity(problemDetail, headers, status, request);
  }

  @Override
  protected ResponseEntity<Object> handleMissingServletRequestParameter(
      MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
    headers = addTraceIdInResponseHeader(headers, request);
    String detail = String.format("Required query parameter '%s' is not present.",
        ex.getParameterName());
    ProblemDetail problemDetail =
        createProblemDetail(
            ex,
            status,
            detail,
            missingQueryParam,
            null,
            request);
    problemDetail.setProperty(KEY_NAME_CODE, missingQueryParam);
    return createResponseEntity(problemDetail, headers, status, request);
  }

  @Override
  protected ResponseEntity<Object> handleTypeMismatch(
      TypeMismatchException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
    if (ex instanceof MethodArgumentTypeMismatchException methodArgumentTypeMismatchException) {
      return handleMethodArgumentTypeMismatch(methodArgumentTypeMismatchException, request);
    }
    return super.handleTypeMismatch(ex, headers, status, request);
  }

  @ExceptionHandler(MissingRequestHeaderException.class)
  public ResponseEntity<ProblemDetail> handleMissingRequestHeader(
      MissingRequestHeaderException ex, WebRequest request) {
    List<FieldError> fieldErrors =
        List.of(new FieldError("headers", ex.getHeaderName(), "must not be null"));
    ProblemDetail problemDetail =
        createProblemDetail(
            ex,
            HttpStatus.BAD_REQUEST,
            validationMethodArgumentInvalidDetails,
            validationMethodArgumentInvalidCode,
            null,
            request);
    problemDetail.setTitle(validationMethodArgumentInvalidTitle);
    problemDetail.setProperty(FIELD_ERRORS_KEY, fieldErrors);
    problemDetail.setProperty(KEY_NAME_CODE, validationMethodArgumentInvalidCode);
    return ResponseEntity.badRequest().body(problemDetail);
  }

  private ResponseEntity<Object> handleMethodArgumentTypeMismatch(
      MethodArgumentTypeMismatchException ex, WebRequest request) {
    String detail =
        String.format(
            "Property '%s' with value '%s' is not a valid %s",
            ex.getName().replaceFirst("DTO$", ""),
            nonNull(ex.getValue()) ? ex.getValue().toString() : "",
            nonNull(ex.getRequiredType()) ? ex.getRequiredType().getSimpleName() : "");

    ProblemDetail problemDetail =
        createProblemDetail(
            ex, HttpStatus.BAD_REQUEST, detail, validationMethodArgumentInvalidCode, null, request);
    problemDetail.setProperty(KEY_NAME_CODE, validationMethodArgumentInvalidCode);
    HttpHeaders headers = addTraceIdInResponseHeader(new HttpHeaders(), request);
    return createResponseEntity(problemDetail, headers, HttpStatus.BAD_REQUEST, request);
  }

  @Override
  protected ResponseEntity<Object> handleHttpMessageNotReadable(
      @NonNull HttpMessageNotReadableException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {

    String localizedMessage = getHttpMessageNotReadableLocalizedMessage(ex);

    ProblemDetail problemDetail =
        createProblemDetail(
            ex,
            HttpStatus.BAD_REQUEST,
            localizedMessage,
            validationMethodArgumentInvalidCode,
            null,
            request);
    problemDetail.setProperty(KEY_NAME_CODE, invalidJsonBodyCode);
    problemDetail.setTitle(invalidJsonBodyTitle);
    return createResponseEntity(problemDetail, headers, HttpStatus.BAD_REQUEST, request);
  }

  private static List<FieldError> computeFieldErrors(MethodArgumentNotValidException ex) {
    BindingResult result = ex.getBindingResult();
    return result.getFieldErrors().stream().map(FieldError::new).toList();
  }

  private HttpHeaders addTraceIdInResponseHeader(HttpHeaders originalHeaders, WebRequest request) {
    String traceId = request.getHeader(traceIdHeaderName);
    if (isBlank(traceId)) {
      return originalHeaders;
    }
    HttpHeaders headers = new HttpHeaders();
    headers.addAll(originalHeaders);
    headers.set(traceIdHeaderName, traceId);
    // Retro compatibility
    headers.set(WCP_TRACE_ID, traceId);
    return headers;
  }

  @Override
  protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
      HttpMediaTypeNotSupportedException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    headers = addTraceIdInResponseHeader(headers, request);
    ProblemDetail problemDetail = ex.getBody();
    problemDetail.setProperty(KEY_NAME_CODE, unsupportedMediaTypeCode);
    return createResponseEntity(problemDetail, headers, status, request);
  }

  private String getHttpMessageNotReadableLocalizedMessage(HttpMessageNotReadableException ex) {
    if (!nonNull(ex.getLocalizedMessage())) {
      return "";
    }
    if (ex.getLocalizedMessage().contains(";")) {
      return ex.getLocalizedMessage().substring(0, indexOf(ex.getLocalizedMessage(), ";"));
    }
    return ex.getLocalizedMessage();
  }

  @Override
  protected ResponseEntity<Object> createResponseEntity(
      @Nullable Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {

    if (body instanceof ProblemDetail problemDetail) {
      // For log outputs
      MDC.put(
          KEY_LOG_NAME_ERROR_CODE,
          Optional.ofNullable(problemDetail.getProperties())
              .map(prop -> prop.get(KEY_NAME_CODE))
              .map(Object::toString)
              .orElse(""));
      MDC.put(KEY_LOG_NAME_ERROR_DETAILS, StringEscapeUtils.escapeJson(problemDetail.getDetail()));
    }
    return super.createResponseEntity(body, headers, statusCode, request);
  }
}
