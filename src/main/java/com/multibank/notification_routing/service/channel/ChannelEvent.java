package com.multibank.notification_routing.service.channel;

import com.multibank.notification_routing.utils.EventType;

import java.time.LocalDateTime;
import java.util.UUID;

public class ChannelEvent {

    private Long id;
    private EventType eventType;
    private String payload;
    private String recipient;
    private LocalDateTime timestamp;
    private String priority;

    public ChannelEvent(Long id,EventType eventType, String payload, String recipient, LocalDateTime timestamp, String priority) {
        this.id = id;
        this.eventType = eventType;
        this.payload = payload;
        this.recipient = recipient;
        this.timestamp = timestamp;
        this.priority = priority;
    }

    public ChannelEvent() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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
}
