package com.multibank.notification_routing.service.channel;

import com.multibank.notification_routing.service.channel.impl.EmailChannel;
import com.multibank.notification_routing.service.channel.impl.PushChannel;
import com.multibank.notification_routing.service.channel.impl.SmsChannel;
import com.multibank.notification_routing.utils.Channel;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ChannelFactory {
    public static NotificationChannel getChannel(Channel channel) {
        return switch (channel) {
            case EMAIL -> new EmailChannel();
            case SMS -> new SmsChannel();
            case PUSH -> new PushChannel();
            default -> throw new IllegalArgumentException("Unknown channel: " + channel);
        };
    }

    public static List<NotificationChannel> getChannels(Set<Channel> channelEnums) {
        return channelEnums.stream()
                .map(ChannelFactory::getChannel)
                .collect(Collectors.toList());
    }
}