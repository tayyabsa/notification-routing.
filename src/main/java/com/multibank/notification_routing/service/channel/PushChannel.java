package com.multibank.notification_routing.service.channel;

import com.multibank.notification_routing.dto.EventsRequestDto;
import com.multibank.notification_routing.service.enums.Channel;
import org.springframework.stereotype.Component;

@Component
public class PushChannel implements NotificationChannel {

    @Override
    public Channel channel() {
        return Channel.PUSH;
    }

    @Override
    public Boolean send(EventsRequestDto event) {


        String message = "Push Notification: Event " + event.getEventType() + " occurred.";
        System.out.println("🔔 Sending PUSH to " + event.getRecipient() + " : " + message);
        return true;

    }
}