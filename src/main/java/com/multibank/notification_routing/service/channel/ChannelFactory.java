package com.multibank.notification_routing.service.channel;

import com.multibank.notification_routing.service.channel.impl.EmailChannel;
import com.multibank.notification_routing.service.channel.impl.PushChannel;
import com.multibank.notification_routing.service.channel.impl.SmsChannel;
import com.multibank.notification_routing.utils.Channel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ChannelFactory {

    @Autowired private EmailChannel emailChannel;
    @Autowired private SmsChannel smsChannel;
    @Autowired private PushChannel pushChannel;

    public NotificationChannel getChannel(Channel channel) {
        return switch (channel) {
            case EMAIL -> emailChannel;
            case SMS -> smsChannel;
            case PUSH -> pushChannel;
            default -> throw new IllegalArgumentException("Unknown channel: " + channel);
        };
    }

    public List<NotificationChannel> getChannels(Set<Channel> channelEnums) {
        return channelEnums.stream()
                .map(this::getChannel)
                .collect(Collectors.toList());
    }
}