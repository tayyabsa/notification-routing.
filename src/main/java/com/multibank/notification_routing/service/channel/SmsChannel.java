package com.multibank.notification_routing.service.channel;

import com.multibank.notification_routing.dto.EventsRequestDto;
import com.multibank.notification_routing.service.enums.Channel;
import org.springframework.stereotype.Component;

@Component
public class SmsChannel implements NotificationChannel {

    @Override
    public Channel channel() {
        return Channel.SMS;
    }

    @Override
    public Boolean send(EventsRequestDto event) {
        String message = "Hello " + event.getRecipient() + ", thanks for registering!";
        System.out.println("📱 Sending SMS to " + event.getRecipient() + " : " + message);
        return true;
    }
}