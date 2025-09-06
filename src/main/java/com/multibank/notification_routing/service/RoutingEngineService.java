package com.multibank.notification_routing.service;

import com.multibank.notification_routing.config.RoutingConfig;
import com.multibank.notification_routing.service.channel.ChannelFactory;
import com.multibank.notification_routing.service.channel.NotificationChannel;
import com.multibank.notification_routing.utils.Channel;
import com.multibank.notification_routing.utils.EventType;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class RoutingEngineService {
    private final RoutingConfig routingConfig;

    public RoutingEngineService(RoutingConfig routingConfig) {
        this.routingConfig = routingConfig;
    }

    public List<NotificationChannel> route(EventType eventType, String priority) {
        Set<Channel> channels = new LinkedHashSet<>();
        for (RoutingConfig.Rule rule : routingConfig.getRules()) {
            boolean matches = matchesRule(rule.getWhen(), eventType, priority);

            if (matches) {
                channels.addAll(rule.getThen().getChannels());
            }
        }

        return ChannelFactory.getChannels(channels);
    }

    private boolean matchesRule(RoutingConfig.When when, EventType eventType, String priority) {

        boolean eventTypeMatches = eventType == when.getEventType(); // Correct enum comparison
        boolean priorityMatches = (when.getPriority() == null ||
                when.getPriority().equals(priority));
        return eventTypeMatches && priorityMatches;
    }

}
