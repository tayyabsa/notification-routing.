package com.multibank.notification_routing.utils;

import com.multibank.notification_routing.dto.EventsRequestDto;
import com.multibank.notification_routing.dto.EventsResponseDto;
import com.multibank.notification_routing.repository.EventStatusEntity;
import com.multibank.notification_routing.service.channel.ChannelEvent;

/**
 * Utility class for converting between Event-related DTOs/entities and ChannelEvent.
 * This is a stateless mapper with only static methods.
 */
public final class ChannelEventMapper {

    // Private constructor to prevent instantiation
    private ChannelEventMapper() {}

    /**
     * Convert an EventsRequestDto into a ChannelEvent.
     *
     * @param request the incoming event request DTO
     * @return a ChannelEvent ready to be dispatched
     */
    public static ChannelEvent toChannelEvent(EventsRequestDto request) {
        if (request == null) {
            return null;
        }
        ChannelEvent channelEvent = new ChannelEvent();
        channelEvent.setEventType(request.getEventType());
        channelEvent.setPriority(request.getPriority());
        channelEvent.setRecipient(request.getRecipient());
        channelEvent.setPayload(request.getPayload()); // add if ChannelEvent has payload
        return channelEvent;
    }

    /**
     * Convert an EventStatusEntity into a ChannelEvent.
     *
     * @param entity the event status entity from DB
     * @return a ChannelEvent ready to be dispatched
     */
    public static ChannelEvent toChannelEvent(EventStatusEntity entity) {
        if (entity == null) {
            return null;
        }
        ChannelEvent channelEvent = new ChannelEvent();
        channelEvent.setId(entity.getId());
        channelEvent.setEventType(entity.getEventType());
        channelEvent.setPriority(entity.getPriority());
        channelEvent.setRecipient(entity.getRecipient());
        channelEvent.setPayload(entity.getPayload()); // add if ChannelEvent has payload
        return channelEvent;
    }

    public static EventsResponseDto toEventsResponseDto(EventStatusEntity entity) {
        if (entity == null) {
            return null;
        }
        EventsResponseDto responseDto = new EventsResponseDto();
        responseDto.setId(entity.getId().toString());
        responseDto.setStatus(entity.getStatus());
        responseDto.setEventType(entity.getEventType());
        responseDto.setPayload(entity.getPayload());
        responseDto.setRecipient(entity.getRecipient());
        responseDto.setChannel(entity.getChannel());
        return responseDto;
    }
}