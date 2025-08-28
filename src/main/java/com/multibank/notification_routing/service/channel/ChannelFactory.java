package com.multibank.notification_routing.service.channel;

import com.multibank.notification_routing.service.enums.Channel;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ChannelFactory {
    public static NotificationChannel getChannel(Channel channel) {
        switch (channel) {
            case EMAIL: return new EmailChannel();
            case SMS: return new SmsChannel();
            case PUSH: return new PushChannel();
            default: throw new IllegalArgumentException("Unknown channel: " + channel);
        }
    }

    public static List<NotificationChannel> getChannels(Set<Channel> channelEnums) {
        return channelEnums.stream()
                .map(ChannelFactory::getChannel)
                .collect(Collectors.toList());
    }
}