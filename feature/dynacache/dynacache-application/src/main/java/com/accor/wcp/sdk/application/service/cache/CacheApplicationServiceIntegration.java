package com.accor.wcp.sdk.application.service.cache;

import com.accor.wcp.sdk.application.communication.App2WCPConsoleCommunicationService;
import com.accor.wcp.sdk.application.service.ServiceApplicationIntegration;
import com.accor.wcp.sdk.application.service.ServiceMessageNotification;
import com.accor.wcp.sdk.command.manager.CommandExecutor;
import com.accor.wcp.sdk.command.manager.CommandManager;
import com.accor.wcp.sdk.service.cache.communication.CacheOperationResult;
import com.accor.wcp.sdk.service.cache.communication.CacheReloadMessageRequestDto;
import com.accor.wcp.sdk.service.cache.communication.CacheReloadMessageResponseDto;
import com.accor.wcp.sdk.service.cache.communication.CacheResetStatsMessageRequestDto;
import com.accor.wcp.sdk.service.cache.communication.CacheResetStatsMessageResponseDto;
import com.accor.wcp.sdk.service.cache.communication.CacheStatisticsMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheApplicationServiceIntegration implements ServiceApplicationIntegration {

    public static final String SERVICE_CACHE_ID = "cache";

    private final App2WCPConsoleCommunicationService app2WCPConsoleCommunicationService;

    private final CacheHandler cacheHandler;

    private CommandManager<ServiceMessageNotification> commandManager;

    private ObjectMapper objectMapper;

    @PostConstruct
    void init() {
        Map<String, CommandExecutor<ServiceMessageNotification>> commandsMap = new HashMap<>();
        CommandExecutor<ServiceMessageNotification> invalidateNotification =
                this::invalidateNotification;
        CommandExecutor<ServiceMessageNotification> resetStatsNotification =
                this::resetStatsNotification;
        commandsMap.put(CacheReloadMessageRequestDto.class.getName(), invalidateNotification);
        commandsMap.put(CacheResetStatsMessageRequestDto.class.getName(), resetStatsNotification);

        commandManager =
                new CommandManager<>(
                        CacheServiceMessageNotificationCommandTypeAccessor.create(), commandsMap);

        objectMapper = new ObjectMapper();
    }

    private void resetStatsNotification(ServiceMessageNotification notification) {
        app2WCPConsoleCommunicationService.ack(getServiceId(), notification.getMessageId());
        CacheResetStatsMessageResponseDto message =
                getMessage(notification, CacheResetStatsMessageResponseDto.class);
        CacheOperationResult result = CacheOperationResult.DONE;
        String errorMessage = null;

        // Invalidate cache
        log.info("Handle reset stats cache request {}", message);
        try {
            cacheHandler.resetStats(message.getCache());
        } catch (Exception e) {
            errorMessage = e.getMessage();
            result = CacheOperationResult.ERROR;
        }

        // Send response to service
        CacheResetStatsMessageResponseDto cacheResetStatsMessageResponseDto =
                CacheResetStatsMessageResponseDto.builder()
                        .result(result)
                        .cache(message.getCache())
                        .errorMessage(errorMessage)
                        .build();
        app2WCPConsoleCommunicationService.notifyResponse(
                getServiceId(), notification.getMessageId(), cacheResetStatsMessageResponseDto);
    }

    private void invalidateNotification(ServiceMessageNotification notification) {
        app2WCPConsoleCommunicationService.ack(getServiceId(), notification.getMessageId());
        CacheReloadMessageRequestDto message =
                getMessage(notification, CacheReloadMessageRequestDto.class);
        CacheOperationResult result = CacheOperationResult.DONE;
        String errorMessage = null;

        // Invalidate cache
        log.info("Handle invalidation cache request {}", message);
        try {
            cacheHandler.reload(message.getCache());
        } catch (Exception e) {
            errorMessage = e.getMessage();
            result = CacheOperationResult.ERROR;
        }

        // Send response to service
        CacheReloadMessageResponseDto cacheInvalidationMessageResponseDto =
                CacheReloadMessageResponseDto.builder()
                        .result(result)
                        .cache(message.getCache())
                        .errorMessage(errorMessage)
                        .build();
        app2WCPConsoleCommunicationService.notifyResponse(
                getServiceId(), notification.getMessageId(), cacheInvalidationMessageResponseDto);
    }

    private <T> T getMessage(ServiceMessageNotification notification, Class<T> type) {
        return objectMapper.convertValue(notification.getMessage(), type);
    }

    @Override
    public String getServiceId() {
        return SERVICE_CACHE_ID;
    }

    @Override
    public void notification(ServiceMessageNotification notification) {
        log.info("Notification for cache: {}", notification);
        commandManager.execute(notification);
    }

    @SneakyThrows
    @Scheduled(fixedRateString = "${wcp.sdk.service.cache.schedule.rate.seconds:60}", timeUnit = TimeUnit.SECONDS)
    public void scheduleFixedRateTask() {
        List<CacheStatisticsMessage> cacheStatisticsMessages = cacheHandler.getCacheStatistics();
        cacheStatisticsMessages.parallelStream()
                .forEach(
                        cacheStatisticsMessage ->
                                app2WCPConsoleCommunicationService.notify(SERVICE_CACHE_ID, cacheStatisticsMessage));
    }
}
