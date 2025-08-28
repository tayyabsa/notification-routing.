package com.multibank.notification_routing.service;

import com.multibank.notification_routing.config.RoutingProperties;
import com.multibank.notification_routing.dto.EventsRequestDto;
import com.multibank.notification_routing.service.channel.ChannelFactory;
import com.multibank.notification_routing.service.channel.NotificationChannel;
import com.multibank.notification_routing.service.enums.Channel;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class RoutingEngineService {
    private final RoutingProperties routingProperties;

    public RoutingEngineService(RoutingProperties routingProperties) {
        this.routingProperties = routingProperties;
    }

    public List<NotificationChannel> route(EventsRequestDto event) {
        Set<Channel> channels = new LinkedHashSet<>();

        // Loop through all configured rules
        for (RoutingProperties.Rule rule : routingProperties.getRules()) {
            boolean matches = matchesRule(rule.getWhen(), event);

            if (matches) {
                channels.addAll(rule.getThen().getChannels());
            }
        }

        return ChannelFactory.getChannels(channels);
    }

    private boolean matchesRule(RoutingProperties.When when, EventsRequestDto event) {
        // null means "don't care"
        boolean eventTypeMatches = (when.getEventType() == null ||
                when.getEventType().equalsIgnoreCase(event.getEventType().toString()));
        boolean priorityMatches = (when.getPriority() == null ||
                when.getPriority().equals(event.getPriority()));
        return eventTypeMatches && priorityMatches;
    }
}
