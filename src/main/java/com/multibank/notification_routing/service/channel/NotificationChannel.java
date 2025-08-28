package com.multibank.notification_routing.service.channel;

import com.multibank.notification_routing.dto.EventsRequestDto;
import com.multibank.notification_routing.service.enums.Channel;

public interface NotificationChannel {
    Channel channel();
    Boolean send(EventsRequestDto event) throws Exception;
}

