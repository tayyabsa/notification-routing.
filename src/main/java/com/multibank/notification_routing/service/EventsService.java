package com.multibank.notification_routing.service;

import com.multibank.notification_routing.dto.EventsRequestDto;
import com.multibank.notification_routing.dto.EventsResponseDto;
import com.multibank.notification_routing.exception.ApplicationException;
import com.multibank.notification_routing.lock.RedissonLockManager;
import com.multibank.notification_routing.repository.EventStatusEntity;
import com.multibank.notification_routing.repository.EventStatusRepo;
import com.multibank.notification_routing.service.channel.NotificationChannel;
import com.multibank.notification_routing.utils.ChannelEventMapper;
import com.multibank.notification_routing.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EventsService {

    @Autowired
    private RoutingEngineService routingEngineService;

    @Autowired
    private EventStatusRepo eventStatusRepo;

    @Autowired
    private RetryAbleNotificationService retryAbleNotificationService;

    @Autowired
    private RedissonLockManager lockManager;

    @Async
    public void processEvents(EventsRequestDto event) {
        List<NotificationChannel> channels = routingEngineService.route(event.getEventType(), event.getPriority());
        for (NotificationChannel channel : channels) {
            boolean lockAcquired = lockManager.lock(Constants.LOCK_KEY_PREFIX + event.getId(), Constants.DEFAULT_TTL_IN_MILLIS);
            if (lockAcquired) {
                try {
                    Optional<EventStatusEntity> eventStatus = eventStatusRepo.findByEventIdAndChannel(event.getId().toString(), channel.channel().toString());
                    if (eventStatus.isEmpty()) {
                        EventStatusEntity eventStatusEntity = new EventStatusEntity();
                        eventStatusEntity.setEventType(event.getEventType());
                        eventStatusEntity.setRetryCount(0);
                        eventStatusEntity.setRecipient(event.getRecipient());
                        eventStatusEntity.setPriority(event.getPriority());
                        eventStatusEntity.setPayload(event.getPayload());
                        eventStatusEntity.setStatus("PENDING");
                        eventStatusEntity.setChannel(channel.channel().toString());
                        eventStatusEntity.setIsDeadLetter(false);
                        eventStatusEntity.setEventId(event.getId().toString());
                        eventStatusEntity = eventStatusRepo.save(eventStatusEntity);
                        try {
                            retryAbleNotificationService.send(channel, ChannelEventMapper.toChannelEvent(eventStatusEntity));
                            eventStatusRepo.findById(eventStatusEntity.getId()
                            ).ifPresent(e -> {
                                e.setStatus("SENT");
                                eventStatusRepo.save(e);
                            });
                        } catch (Exception e) {
                            log.error("Failed to send notification for {}", event.getRecipient());
                        }
                    }
                } finally {
                    lockManager.unlock(Constants.LOCK_KEY_PREFIX + event.getId());
                }
            }
        }
        log.info("Event processed: {} for {}", event.getEventType(), event.getRecipient());
    }

    public EventsResponseDto getEventStatusById(String id) {
        Optional<EventStatusEntity> eventStatus = eventStatusRepo.findById(Long.valueOf(id));
        if (eventStatus.isPresent()) {
            return new EventsResponseDto(eventStatus.get().getStatus());
        } else {
            throw new ApplicationException(Constants.ERROR_CODE_NOT_FOUND, "Event not found with ID: " + id);
        }
    }

    public List<EventsResponseDto> getFailedEvents() {

        List<EventStatusEntity> events = eventStatusRepo.findByStatus("FAILED");
        if (!events.isEmpty()) {
            return events.stream()
                    .map(ChannelEventMapper::toEventsResponseDto)
                    .collect(Collectors.toList());
        } else {
            throw new ApplicationException(Constants.ERROR_CODE_NOT_FOUND, "No failed events found");
        }
    }
}
