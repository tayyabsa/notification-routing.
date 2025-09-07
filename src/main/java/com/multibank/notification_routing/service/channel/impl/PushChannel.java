package com.multibank.notification_routing.service.channel.impl;

import com.multibank.notification_routing.service.channel.ChannelEvent;
import com.multibank.notification_routing.service.channel.NotificationChannel;
import com.multibank.notification_routing.utils.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PushChannel implements NotificationChannel {

    @Override
    public Channel channel() {
        return Channel.PUSH;
    }

    @Override
    public void doSend(ChannelEvent event) throws Exception{
        int random = (int)(Math.random() * 100); // 0–99
        if(random % 2 == 0) {
            throw new Exception("Simulated email sending failure");
        }
        String message = formatEvent(event);
        log.info("\uD83D\uDD14 Sending PUSH to {} : {}", event.getRecipient(), message);
    }

    @Override
    public String formatEvent(ChannelEvent event) {
        return "Push Notification: Event " + event.getPayload() + " occurred.";
    }
}