package com.multibank.notification_routing.repository.model;

import com.multibank.notification_routing.utils.EventType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
@Entity
public class EventStatusEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventType eventType;

    @Column
    private String eventId;

    @Column(nullable = false)
    private String status;

    @Column
    private String payload;

    @Column
    private String recipient;

    @Column
    private String priority;

    @Column
    private Integer retryCount;

    @Column
    private Boolean isDeadLetter;

    @Column
    private String channel;

}