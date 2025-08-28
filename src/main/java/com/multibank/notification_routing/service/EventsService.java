package com.multibank.notification_routing.service;

import com.multibank.notification_routing.dto.EventsRequestDto;
import com.multibank.notification_routing.dto.EventsResponseDto;
import com.multibank.notification_routing.repository.EventStatusEntity;
import com.multibank.notification_routing.repository.EventStatusRepo;
import com.multibank.notification_routing.service.channel.NotificationChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class EventsService {

    @Value("${retry.maxAttempts}")
    private Integer maxAttempts;

    @Autowired
    private RoutingEngineService routingEngineService;

    @Autowired
    private EventStatusRepo eventStatusRepo;

    public void processFailures() {
        List<EventStatusEntity> failedEvents =
                eventStatusRepo.findByStatusAndIsDeadLetter("FAILED", false);

        failedEvents.stream()
                .collect(Collectors.partitioningBy(e ->
                        Optional.ofNullable(e.getRetryCount()).orElse(0) < maxAttempts
                ))
                .forEach((shouldRetry, events) -> {
                    if (shouldRetry) {
                        // map to DTOs and process
                        events.stream()
                                .map(this::toDto)
                                .forEach(this::processEvents);
                    } else {
                        // mark as dead letter in bulk
                        events.forEach(e -> e.setIsDeadLetter(true));
                        eventStatusRepo.saveAll(events);
                        events.forEach(e ->
                                System.out.println("Max retry attempts reached for event ID: " + e.getId())
                        );
                    }
                });
    }

    @Async
    public void processEvents(EventsRequestDto event) {

        List<NotificationChannel> channels = routingEngineService.route(event);
        for (NotificationChannel channel : channels) {
            try {
                Boolean status = channel.send(event);
                Optional<EventStatusEntity> eventStatus = eventStatusRepo.findById(event.getId());
                if (eventStatus.isPresent()) {
                    EventStatusEntity existingEntity = eventStatus.get();
                    existingEntity.setRetryCount(existingEntity.getRetryCount() + 1);
                    existingEntity.setStatus(status ? "SENT" : "FAILED");
                    eventStatusRepo.save(existingEntity);
                } else {
                    EventStatusEntity eventStatusEntity = new EventStatusEntity();
                    eventStatusEntity.setEventType(event.getEventType());
                    eventStatusEntity.setRetryCount(0);
                    eventStatusEntity.setStatus(status ? "SENT" : "FAILED");
                    eventStatusEntity.setRecipient(event.getRecipient());
                    eventStatusEntity.setPriority(event.getPriority());
                    eventStatusEntity.setPayload(event.getPayload());
                    eventStatusRepo.save(eventStatusEntity);
                }
            } catch (Exception e) {
                System.err.println("Failed to send notification via " + channel.channel() + " for " + event.getRecipient());
                e.printStackTrace();
            }
        }
        System.out.println("Event processed: " + event.getEventType() + " for " + event.getRecipient());
    }

    private EventsRequestDto toDto(EventStatusEntity e) {
        EventsRequestDto dto = new EventsRequestDto();
        dto.setId(e.getId());
        dto.setEventType(e.getEventType());
        dto.setPriority(e.getPriority());
        dto.setRecipient(e.getRecipient());
        dto.setPayload(e.getPayload());
        return dto;
    }

    public EventsResponseDto getEventStatusById(String id) {
        Optional<EventStatusEntity> eventStatus = eventStatusRepo.findById(Long.valueOf(id));
        if (eventStatus.isPresent()) {
            return new EventsResponseDto(eventStatus.get().getStatus());
        } else {
            throw new RuntimeException("Event not found with ID: " + id);
        }
    }

    public List<String> getFailedEvents() {

        List<EventStatusEntity> events = eventStatusRepo.findByStatus("FAILED");
        if (!events.isEmpty()) {
            return events.stream()
                    .map(e -> e.getId().toString())
                    .collect(Collectors.toList());
        } else {
            throw new RuntimeException("No failed events found");
        }
    }
}
