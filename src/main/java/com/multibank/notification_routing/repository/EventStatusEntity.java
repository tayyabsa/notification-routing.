package com.multibank.notification_routing.repository;

import com.multibank.notification_routing.utils.EventType;
import jakarta.persistence.*;


@Entity
public class EventStatusEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventType eventType;

    @Column(nullable = false)
    private String status;

    @Column
    private String payload;

    @Column
    private String recipient;

    @Column
    private String priority;

    @Column
    private String timestamp;

    @Column
    private Integer retryCount;

    @Column
    private Boolean isDeadLetter;

    @Column
    private String channel;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public EventType getEventType() { return eventType; }
    public void setEventType(EventType eventType) { this.eventType = eventType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public Integer getRetryCount() { return retryCount; }
    public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Boolean getIsDeadLetter() {
        return isDeadLetter;
    }

    public void setIsDeadLetter(Boolean isDeadLetter) {
        this.isDeadLetter = isDeadLetter;
    }

    public String getChannel() {
        return channel;
    }
    public void setChannel(String channel) {
        this.channel = channel;
    }

}