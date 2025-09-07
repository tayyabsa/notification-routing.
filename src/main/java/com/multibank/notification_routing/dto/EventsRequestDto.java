package com.multibank.notification_routing.dto;

import com.multibank.notification_routing.service.channel.ChannelEvent;
import com.multibank.notification_routing.utils.EventType;

import java.time.LocalDateTime;

public class EventsRequestDto {
    private Long id;
    private EventType eventType;
    private String payload;
    private String recipient;
    private LocalDateTime timestamp;
    private String priority;
    private String idempotencyKey;

    public EventsRequestDto() {
    }

    public EventsRequestDto(Long id,EventType eventType, String recipient, String priority, String payload) {
        this.eventType = eventType;
        this.payload = payload;
        this.recipient = recipient;
        this.timestamp = LocalDateTime.now();
        this.priority = priority;
        this.id = id;
    }

    // Getters and setters
    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
