package com.accor.wcp.console.services.core.usernotification;

import com.accor.wcp.console.sdk.notification.UserNotification;
import com.accor.wcp.console.services.core.usernotification.dto.NotificationDto;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/services/notification")
@RequiredArgsConstructor
@Slf4j
public class UserNotificationApi {

    private final WCPConsoleUserNotificationServiceImpl userNotificationService;

    @GetMapping("/all")
    public List<NotificationDto> getUserNotifications(){
        return userNotificationService.getNotifications();
    }

    @DeleteMapping("/cancel/{id}")
    public void cancelUserNotifications(@PathVariable String id){
        userNotificationService.cancel(id);
    }

    @SneakyThrows
    @PutMapping("/update/{id}")
    public void updateUserNotifications(@PathVariable String id, @RequestBody NotificationDto notification){
        userNotificationService.update(id, notification);
    }

    @PostMapping("/create/{serviceId}")
    public String createUserNotifications(@PathVariable String serviceId, @RequestBody NotificationDto notification){
        return userNotificationService.create(serviceId, notification);
    }

    @PostMapping("/create/{domain}/{env}/{serviceId}")
    public String createUserNotifications(@PathVariable String domain, @PathVariable String env, @PathVariable String serviceId, @RequestBody NotificationDto notification){
        return userNotificationService.create(domain, env, serviceId, notification);
    }

    @GetMapping("/number_new_notif/{timestamp}")
    public int getNumberOfNewNotifications(@PathVariable long timestamp){
        if(timestamp<=0){
            userNotificationService.getNumberOfNotifications();
        }
        return userNotificationService.getNumberOfNewNotifications(new Date(timestamp));
    }


}
