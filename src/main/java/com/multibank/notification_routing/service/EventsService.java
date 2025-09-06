package com.multibank.notification_routing.service;

import com.multibank.notification_routing.dto.EventsRequestDto;
import com.multibank.notification_routing.dto.EventsResponseDto;
import com.multibank.notification_routing.repository.EventStatusEntity;
import com.multibank.notification_routing.repository.EventStatusRepo;
import com.multibank.notification_routing.service.channel.NotificationChannel;
import com.multibank.notification_routing.utils.ChannelEventMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EventsService {

    @Autowired
    private RoutingEngineService routingEngineService;

    @Autowired
    private EventStatusRepo eventStatusRepo;

    @Autowired
    private RetryAbleNotificationService retryAbleNotificationService;

    @Async
    public void processEvents(EventsRequestDto event) {

        List<NotificationChannel> channels = routingEngineService.route(event.getEventType(), event.getPriority());
        for (NotificationChannel channel : channels) {
            EventStatusEntity eventStatusEntity = new EventStatusEntity();
            eventStatusEntity.setEventType(event.getEventType());
            eventStatusEntity.setRetryCount(0);
            eventStatusEntity.setRecipient(event.getRecipient());
            eventStatusEntity.setPriority(event.getPriority());
            eventStatusEntity.setPayload(event.getPayload());
            eventStatusEntity.setStatus("PENDING");
            eventStatusEntity.setChannel(channel.channel().toString());
            eventStatusEntity.setIsDeadLetter(false);
            eventStatusEntity = eventStatusRepo.save(eventStatusEntity);
            try {
                retryAbleNotificationService.send(channel, ChannelEventMapper.toChannelEvent(eventStatusEntity));

                eventStatusRepo.findById(eventStatusEntity.getId()
                ).ifPresent(e -> {
                    e.setStatus("SENT");
                    eventStatusRepo.save(e);
                });
            } catch (Exception e) {
                System.err.println("Failed to send notification for " + event.getRecipient());
            }
        }
        System.out.println("Event processed: " + event.getEventType() + " for " + event.getRecipient());
    }

    public EventsResponseDto getEventStatusById(String id) {
        Optional<EventStatusEntity> eventStatus = eventStatusRepo.findById(Long.valueOf(id));
        if (eventStatus.isPresent()) {
            return new EventsResponseDto(eventStatus.get().getStatus());
        } else {
            throw new RuntimeException("Event not found with ID: " + id);
        }
    }

    public List<EventsResponseDto> getFailedEvents() {

        List<EventStatusEntity> events = eventStatusRepo.findByStatus("FAILED");
        if (!events.isEmpty()) {
            return events.stream()
                    .map(ChannelEventMapper::toEventsResponseDto)
                    .collect(Collectors.toList());
        } else {
            throw new RuntimeException("No failed events found");
        }
    }
}
