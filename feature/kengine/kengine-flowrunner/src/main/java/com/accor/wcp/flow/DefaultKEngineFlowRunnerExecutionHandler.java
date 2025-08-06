package com.accor.wcp.flow;

import static com.accor.wcp.flow.errors.WcpError.TECHNICAL_ERROR;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import com.accor.kengine.RuleNodeException;
import com.accor.kengine.WarningExecutionException;
import com.accor.kengine.execution.ExecutionResult;
import com.accor.kengine.executor.KEngineFlowRunnerExecutionHandler;
import com.accor.wcp.flow.errors.ErrorCode;
import com.accor.wcp.flow.errors.FlowExecutionError;
import com.accor.wcp.flow.errors.FlowExecutionInterruptWarning;
import com.accor.wcp.flow.errors.FlowExecutionMultiWarning;
import com.accor.wcp.flow.errors.FlowExecutionWarning;
import com.accor.wcp.flow.errors.TechnicalError;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.MDC;

@Slf4j
public class DefaultKEngineFlowRunnerExecutionHandler implements KEngineFlowRunnerExecutionHandler {

  public void logExecutionId(String executionId) {
    MDC.put("executionId", executionId);
  }

  public void logAndThrowException(ExecutionResult executionResult) {
    Throwable throwable = unwrap(executionResult.getThrowable());
    if (throwable instanceof FlowExecutionError) {
      FlowExecutionError error =
          ((FlowExecutionError) throwable).withExecutionId(executionResult.getExecutionId());
      logError(error.getLog(), error.getErrorCode(), executionResult.getExecutionId());
      throw (error);
    } else if (throwable instanceof FlowExecutionInterruptWarning) {
      FlowExecutionInterruptWarning warning =
          ((FlowExecutionInterruptWarning) throwable)
              .withExecutionId(executionResult.getExecutionId());
      logWarnings(warning, executionResult.getWarnings(), executionResult);
      throw (warning);
    }
    log.error(throwable.getMessage(), throwable);
    throw new TechnicalError(TECHNICAL_ERROR)
        .withLog(throwable.getMessage())
        .withExecutionId(executionResult.getExecutionId());
  }

  @Override
  public void logWarnings(
      List<WarningExecutionException> warnings, ExecutionResult executionResult) {
    logWarnings(null, warnings, executionResult);
  }

  void logWarnings(
      FlowExecutionInterruptWarning interruptWarning,
      List<WarningExecutionException> warnings,
      ExecutionResult executionResult) {
    List<String> warningCodes = new ArrayList<>();
    List<String> warningDetails = new ArrayList<>();
    Optional.ofNullable(interruptWarning)
        .ifPresent(
            warning ->
                extractWarningCodeAndDetailsThenLogWarning(
                    executionResult,
                    warningCodes,
                    warningDetails,
                    warning.getErrorCode(),
                    warning.getErrorCode().getDetail(),
                    warning.getLog()));

    warnings.forEach(
        e -> {
          if (e instanceof FlowExecutionWarning) {
            FlowExecutionWarning warning = (FlowExecutionWarning) e;
            extractWarningCodeAndDetailsThenLogWarning(
                executionResult,
                warningCodes,
                warningDetails,
                warning.getErrorCode(),
                warning.getErrorCode().getDetail(),
                warning.getLog());
          }
          if (e instanceof FlowExecutionMultiWarning) {
            FlowExecutionMultiWarning warning = (FlowExecutionMultiWarning) e;
            warning
                .getErrorCodes()
                .forEach(
                    errorCode ->
                        extractWarningCodeAndDetailsThenLogWarning(
                            executionResult,
                            warningCodes,
                            warningDetails,
                            errorCode,
                            errorCode.getDetail(),
                            warning.getLog()));
          }
        });
    MDC.put("warningCodes", "\"" + String.join("\",\"", warningCodes) + "\"");
    MDC.put("warningDetails", "\"" + String.join("\",\"", warningDetails) + "\"");
  }

  private void extractWarningCodeAndDetailsThenLogWarning(
      ExecutionResult executionResult,
      List<String> warningCodes,
      List<String> warningDetails,
      ErrorCode errorCode,
      String errorCodeDetail,
      String log) {
    warningCodes.add(errorCode.name());
    warningDetails.add(
        isNull(errorCodeDetail)
            ? StringEscapeUtils.escapeJson(errorCode.getDetail())
            : StringEscapeUtils.escapeJson(errorCodeDetail));
    logWarning(errorCodeDetail, errorCode, executionResult.getExecutionId(), log);
  }

  private void logError(String message, ErrorCode errorCode, String executionId) {
    MDC.put("errorCode", errorCode.name());
    MDC.put("errorDetails", StringEscapeUtils.escapeJson(errorCode.getDetail()));
    log.error(
        "[{}][ERROR={}] {} {}",
        errorCode.name(),
        errorCode.getStatus(),
        executionId,
        isNull(message) ? errorCode.getDetail() : StringUtils.normalizeSpace(message));
  }

  private void logWarning(String message, ErrorCode errorCode, String executionId, String log) {
    FlowRunnerImpl.getLog()
        .warn(
            "[{}][WARNING={}] {} {} {}",
            errorCode.name(),
            errorCode.getStatus(),
            executionId,
            isNull(message) ? errorCode.getDetail() : StringUtils.normalizeSpace(message),
            nonNull(log) ? log : "");
  }

  private Throwable unwrap(RuleNodeException throwable) {
    Throwable temp = throwable;
    while (temp.getCause() != null) {
      temp = temp.getCause();
      if (temp.getCause() instanceof FlowExecutionError) {
        return temp.getCause();
      }
    }
    if (temp instanceof RuntimeException) {
      return temp;
    }
    return new RuntimeException(temp);
  }
}
