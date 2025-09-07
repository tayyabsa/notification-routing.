package com.multibank.notification_routing.service;

import com.multibank.notification_routing.dto.EventsRequestDto;
import com.multibank.notification_routing.dto.EventsResponseDto;
import com.multibank.notification_routing.exception.ApplicationException;
import com.multibank.notification_routing.lock.RedissonLockManager;
import com.multibank.notification_routing.repository.EventStatusRepo;
import com.multibank.notification_routing.repository.RecipientRepo;
import com.multibank.notification_routing.repository.model.EventStatusEntity;
import com.multibank.notification_routing.repository.model.RecipientEntity;
import com.multibank.notification_routing.service.channel.NotificationChannel;
import com.multibank.notification_routing.utils.Channel;
import com.multibank.notification_routing.utils.ChannelEventMapper;
import com.multibank.notification_routing.utils.Constants;
import com.multibank.notification_routing.utils.EventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @InjectMocks
    private EventsService eventsService;

    @Mock
    private RoutingEngineService routingEngineService;

    @Mock
    private EventStatusRepo eventStatusRepo;

    @Mock
    private RecipientRepo recipientRepo;

    @Mock
    private RetryAbleNotificationService retryAbleNotificationService;

    @Mock
    private RedissonLockManager lockManager;

    @Mock
    private NotificationChannel emailChannel;

    private EventsRequestDto dto;

    @BeforeEach
    void setUp() {
        dto = new EventsRequestDto();
        dto.setId(123L);
        dto.setEventType(EventType.USER_REGISTERED);
        dto.setPriority("HIGH");
        dto.setRecipient("user@example.com");
        dto.setPayload("{\"hello\":\"world\"}");

        lenient().when(emailChannel.channel()).thenReturn(Channel.EMAIL);
    }

    @Test
    void processEvents_LockAcquiredAndNotificationSent() throws Exception {
        when(lockManager.lock(Constants.LOCK_KEY_PREFIX + dto.getId(), Constants.DEFAULT_TTL_IN_MILLIS)).thenReturn(true);
        when(routingEngineService.route(dto.getEventType(), dto.getPriority())).thenReturn(List.of(emailChannel));
        when(eventStatusRepo.findByEventIdAndChannel(dto.getId().toString(), Channel.EMAIL.toString())).thenReturn(Optional.empty());

        RecipientEntity recipientEntity = new RecipientEntity();
        recipientEntity.setId(1L);
        recipientEntity.setRecipientId(dto.getRecipient());
        recipientEntity.setEmail("abc@gmail.com");
        recipientEntity.setPhoneNumber("123312312");
        recipientEntity.setPushToken("test");

        when(recipientRepo.findByRecipientId(anyString())).thenReturn(Optional.of(recipientEntity));

        EventStatusEntity savedEntity = new EventStatusEntity();
        savedEntity.setId(1L);
        when(eventStatusRepo.save(any(EventStatusEntity.class))).thenReturn(savedEntity);

        try (MockedStatic<ChannelEventMapper> mockedMapper = mockStatic(ChannelEventMapper.class)) {
            mockedMapper.when(() -> ChannelEventMapper.toChannelEvent(any(EventStatusEntity.class)))
                    .thenReturn(new com.multibank.notification_routing.service.channel.ChannelEvent());

            eventsService.processEvents(dto);

            verify(lockManager, times(1)).lock(Constants.LOCK_KEY_PREFIX + dto.getId(), Constants.DEFAULT_TTL_IN_MILLIS);
            verify(eventStatusRepo, times(1)).save(any(EventStatusEntity.class));
            verify(retryAbleNotificationService, times(1)).send(eq(emailChannel), any());
            verify(eventStatusRepo, times(1)).findById(savedEntity.getId());
            verify(lockManager, times(1)).unlock(Constants.LOCK_KEY_PREFIX + dto.getId());
        }
    }

    @Test
    void processEvents_ExceptionDuringNotification() throws Exception {
        RecipientEntity recipientEntity = new RecipientEntity();
        recipientEntity.setId(1L);
        recipientEntity.setRecipientId(dto.getRecipient());
        recipientEntity.setEmail("abc@gmail.com");
        recipientEntity.setPhoneNumber("123312312");
        recipientEntity.setPushToken("test");

        when(recipientRepo.findByRecipientId(anyString())).thenReturn(Optional.of(recipientEntity));
        when(lockManager.lock(Constants.LOCK_KEY_PREFIX + dto.getId(), Constants.DEFAULT_TTL_IN_MILLIS)).thenReturn(true);
        when(routingEngineService.route(dto.getEventType(), dto.getPriority())).thenReturn(List.of(emailChannel));
        when(eventStatusRepo.findByEventIdAndChannel(dto.getId().toString(), Channel.EMAIL.toString())).thenReturn(Optional.empty());

        EventStatusEntity savedEntity = new EventStatusEntity();
        savedEntity.setId(1L);
        when(eventStatusRepo.save(any(EventStatusEntity.class))).thenReturn(savedEntity);

        try (MockedStatic<ChannelEventMapper> mockedMapper = mockStatic(ChannelEventMapper.class)) {
            mockedMapper.when(() -> ChannelEventMapper.toChannelEvent(any(EventStatusEntity.class)))
                    .thenReturn(new com.multibank.notification_routing.service.channel.ChannelEvent());

            doThrow(new RuntimeException("Notification failed")).when(retryAbleNotificationService).send(eq(emailChannel), any());

            eventsService.processEvents(dto);

            verify(lockManager, times(1)).lock(Constants.LOCK_KEY_PREFIX + dto.getId(), Constants.DEFAULT_TTL_IN_MILLIS);
            verify(eventStatusRepo, times(1)).save(any(EventStatusEntity.class));
            verify(retryAbleNotificationService, times(1)).send(eq(emailChannel), any());
            verify(lockManager, times(1)).unlock(Constants.LOCK_KEY_PREFIX + dto.getId());
        }
    }


    @Test
    void getEventStatusById_ReturnsEventStatus() {
        EventStatusEntity mockEntity = new EventStatusEntity();
        mockEntity.setStatus("SENT");

        when(eventStatusRepo.findById(1L)).thenReturn(Optional.of(mockEntity));

        EventsResponseDto response = eventsService.getEventStatusById("1");

        assertThat(response.getStatus()).isEqualTo("SENT");
        verify(eventStatusRepo, times(1)).findById(1L);
    }


    @Test
    void getEventStatusById_ThrowsExceptionWhenNotFound() {
        when(eventStatusRepo.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventsService.getEventStatusById("1"))
                .isInstanceOf(ApplicationException.class)
                .hasMessage("Event not found with ID: 1");

        verify(eventStatusRepo, times(1)).findById(1L);
    }

    @Test
    void getFailedEvents_ThrowsExceptionWhenNoFailedEvents() {
        when(eventStatusRepo.findByStatus("FAILED")).thenReturn(List.of());

        assertThatThrownBy(() -> eventsService.getFailedEvents())
                .isInstanceOf(ApplicationException.class)
                .hasMessage("No failed events found");

        verify(eventStatusRepo, times(1)).findByStatus("FAILED");
    }
}