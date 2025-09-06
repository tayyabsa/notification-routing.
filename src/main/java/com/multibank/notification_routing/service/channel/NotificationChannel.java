package com.multibank.notification_routing.service.channel;

import com.multibank.notification_routing.utils.Channel;

public interface NotificationChannel {
    Channel channel();
    String formatEvent(ChannelEvent event);
    void doSend(ChannelEvent event) throws Exception;
}

