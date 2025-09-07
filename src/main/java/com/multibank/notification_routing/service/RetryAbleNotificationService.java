package com.multibank.notification_routing.service;


import com.multibank.notification_routing.exception.ApplicationException;
import com.multibank.notification_routing.repository.EventStatusEntity;
import com.multibank.notification_routing.repository.EventStatusRepo;
import com.multibank.notification_routing.service.channel.ChannelEvent;
import com.multibank.notification_routing.service.channel.NotificationChannel;
import com.multibank.notification_routing.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class RetryAbleNotificationService {

    @Autowired
    private EventStatusRepo eventStatusRepo;

    @Retryable(
            value = {Exception.class},
            maxAttemptsExpression = "#{@retryConfig.maxAttempts}",
            backoff = @Backoff(
                    delayExpression = "#{@retryConfig.initialBackoffMs}",
                    multiplierExpression = "#{@retryConfig.multiplier}"
            )
    )
    public void send(NotificationChannel channel, ChannelEvent event) throws Exception {
        System.out.println("Attempting to send event via " + event.getPayload());
        try {
            channel.doSend(event);
        } catch (Exception e) {
            System.err.println("Send failed: " + e.getMessage());
            Optional<EventStatusEntity> eventStatusEntity = eventStatusRepo.findById(event.getId());
            if(eventStatusEntity.isPresent()) {
                EventStatusEntity entity = eventStatusEntity.get();
                entity.setRetryCount(entity.getRetryCount() + 1);
                eventStatusRepo.saveAndFlush(entity);
            }
            throw e;
        }
    }

    @Recover
    public void recover(Exception ex,NotificationChannel channel, ChannelEvent event) {
        Optional<EventStatusEntity> eventStatusEntity = eventStatusRepo.findById(event.getId());
        if(eventStatusEntity.isPresent()) {
            EventStatusEntity entity = eventStatusEntity.get();
            entity.setStatus("FAILED");
            entity.setIsDeadLetter(true);
            eventStatusRepo.saveAndFlush(entity);
        }
        System.err.println("Retries exhausted. Event moved to dead-letter. Reason: " + ex.getMessage());
        throw new ApplicationException(Constants.ERROR_CODE_BAD_REQUEST,ex.toString());
    }
}