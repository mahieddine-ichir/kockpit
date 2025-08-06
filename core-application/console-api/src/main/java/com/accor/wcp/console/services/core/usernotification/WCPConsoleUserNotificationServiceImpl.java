package com.accor.wcp.console.services.core.usernotification;

import com.accor.wcp.console.sdk.notification.UserNotification;
import com.accor.wcp.console.sdk.notification.WCPConsoleUserNotificationService;
import com.accor.wcp.console.services.core.usernotification.dto.NotificationDto;
import org.springframework.stereotype.Service;

import java.util.*;

import static java.util.Objects.isNull;

@Service
class WCPConsoleUserNotificationServiceImpl implements WCPConsoleUserNotificationService {

    private Map<String, NotificationDto> notifications = new HashMap();

    @Override
    public String create(String serviceId, UserNotification notification) {
        String notificationId = UUID.randomUUID().toString();

        NotificationDto newNotification = NotificationDto.builder()
                .id(notificationId)
                .level(notification.getLevel())
                .applicationId(notification.getApplicationId())
                .serviceId(serviceId)
                .description(notification.getDescription())
                .domain("")
                .env("")
                .build();

        if(isNull(notification.getDate())){
            newNotification.setDate(new Date());
        }
        else{
            newNotification.setDate(notification.getDate());
        }

        this.notifications.put(notificationId, newNotification);


        return notificationId;
    }

    @Override
    public String create(String domain, String env, String serviceId, UserNotification notification) {
        String notificationId = UUID.randomUUID().toString();

        NotificationDto newNotification = NotificationDto.builder()
                .id(notificationId)
                .level(notification.getLevel())
                .applicationId(notification.getApplicationId())
                .serviceId(serviceId)
                .description(notification.getDescription())
                .domain(domain)
                .env(env)
                .build();

        if(isNull(notification.getDate())){
            newNotification.setDate(new Date());
        }else{
            newNotification.setDate(notification.getDate());
        }
        this.notifications.put(notificationId, newNotification);

        return notificationId;
    }

    @Override
    public void cancel(String notificationId) {
        notifications.remove(notificationId);
    }

    @Override
    public void update(String notificationId, UserNotification notification) {

        NotificationDto toUpdate = notifications.get(notificationId);

        toUpdate.setApplicationId(notification.getApplicationId());
        toUpdate.setDescription(notification.getDescription());
        toUpdate.setLevel(notification.getLevel());
        toUpdate.setDate(notification.getDate());

        notifications.put(notificationId, toUpdate);
    }

    public List<NotificationDto> getNotifications(){
        List<NotificationDto> notificationDtos = new ArrayList<NotificationDto>(notifications.values());
        Collections.sort(notificationDtos, new SortByDate());
        Collections.reverse(notificationDtos);
        return notificationDtos;
    }

    public int getNumberOfNewNotifications(Date date){
        List<NotificationDto> newNotif = this.notifications.values().stream().filter(notif -> notif.getDate().after(date)).toList();
        return newNotif.size();
    }

    public int getNumberOfNotifications(){
        return this.notifications.size();
    }
}

class SortByDate implements Comparator<NotificationDto> {
    @Override
    public int compare(NotificationDto a, NotificationDto b) {
        return a.getDate().compareTo(b.getDate());
    }
}
