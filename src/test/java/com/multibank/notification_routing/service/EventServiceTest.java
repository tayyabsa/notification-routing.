package com.multibank.notification_routing.service;


import com.multibank.notification_routing.dto.EventsRequestDto;
import com.multibank.notification_routing.repository.EventStatusEntity;
import com.multibank.notification_routing.repository.EventStatusRepo;
import com.multibank.notification_routing.service.channel.ChannelEvent;
import com.multibank.notification_routing.service.channel.NotificationChannel;
import com.multibank.notification_routing.utils.Channel;
import com.multibank.notification_routing.utils.ChannelEventMapper;
import com.multibank.notification_routing.utils.EventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
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
    private RetryAbleNotificationService retryAbleNotificationService;

    // channel doubles
    @Mock private NotificationChannel emailChannel;
    @Mock private NotificationChannel smsChannel;

    private EventsRequestDto dto;

    @BeforeEach
    void setUp() {
        dto = new EventsRequestDto();
        dto.setEventType(EventType.USER_REGISTERED);
        dto.setPriority("HIGH");
        dto.setRecipient("user@example.com");
        dto.setPayload("{\"hello\":\"world\"}");
        when(emailChannel.channel()).thenReturn(Channel.EMAIL);

    }

    @Test
    void processEventsMultipleChannelsSuccessUpdatesToSent() throws Exception {
        when(smsChannel.channel()).thenReturn(Channel.SMS);


        // route to two channels
        when(routingEngineService.route(dto.getEventType(), dto.getPriority()))
                .thenReturn(List.of(emailChannel, smsChannel));

        // mock ChannelEventMapper static
        ChannelEvent mapped = new ChannelEvent();
        mapped.setEventType(dto.getEventType());
        mapped.setPriority(dto.getPriority());
        mapped.setRecipient(dto.getRecipient());
        mapped.setPayload(dto.getPayload());

        // simulate DB ids on first save for each channel
        AtomicLong idSeq = new AtomicLong(100);
        Answer<EventStatusEntity> assignId = inv -> {
            EventStatusEntity e = inv.getArgument(0);
            if (e.getId() == null) e.setId(idSeq.getAndIncrement());
            return e;
        };
        when(eventStatusRepo.save(any(EventStatusEntity.class))).thenAnswer(assignId);

        // findById returns the same entity we saved (so the lambda can update to SENT)
        when(eventStatusRepo.findById(anyLong())).thenAnswer(inv -> {
            Long id = inv.getArgument(0);
            EventStatusEntity e = new EventStatusEntity();
            e.setId(id);
            e.setEventType(dto.getEventType());
            e.setRecipient(dto.getRecipient());
            e.setPriority(dto.getPriority());
            e.setPayload(dto.getPayload());
            e.setStatus("SENT");
            e.setRetryCount(0);
            e.setChannel("EMAIL"); // value not important for assertion here
            e.setIsDeadLetter(false);
            return Optional.of(e);
        });

        try (MockedStatic<ChannelEventMapper> mocked = mockStatic(ChannelEventMapper.class)) {
            mocked.when(() -> ChannelEventMapper.toChannelEvent(any(EventStatusEntity.class)))
                    .thenReturn(mapped);

            // Act
            eventsService.processEvents(dto);

            // Assert send invoked for each channel with mapped event
            verify(retryAbleNotificationService, times(1)).send(eq(emailChannel), eq(mapped));
            verify(retryAbleNotificationService, times(1)).send(eq(smsChannel), eq(mapped));

            // First save (PENDING) + second save (SENT) for each channel
            ArgumentCaptor<EventStatusEntity> saveCaptor = ArgumentCaptor.forClass(EventStatusEntity.class);
            verify(eventStatusRepo, atLeast(2)).save(saveCaptor.capture());

            // verify we did create PENDING rows
            assertThat(saveCaptor.getAllValues().stream().filter(e -> "PENDING".equals(e.getStatus())).count())
                    .isGreaterThanOrEqualTo(2);

            // verify we later updated to SENT
            assertThat(saveCaptor.getAllValues().stream().anyMatch(e -> "SENT".equals(e.getStatus()))).isTrue();

            // optional: ensure initial fields set
            EventStatusEntity firstSaved = saveCaptor.getAllValues().get(0);
            assertThat(firstSaved.getEventType()).isEqualTo(dto.getEventType());
            assertThat(firstSaved.getRecipient()).isEqualTo(dto.getRecipient());
            assertThat(firstSaved.getPriority()).isEqualTo(dto.getPriority());
            assertThat(firstSaved.getStatus()).isEqualTo("PENDING");
            assertThat(firstSaved.getIsDeadLetter()).isFalse();
            assertThat(firstSaved.getRetryCount()).isZero();
        }
    }

    @Test
    void processEventsWhenSendThrowsDoesNotMarkSent() throws Exception {
        when(routingEngineService.route(dto.getEventType(), dto.getPriority()))
                .thenReturn(List.of(emailChannel));

        // assign id on save
        when(eventStatusRepo.save(any(EventStatusEntity.class))).thenAnswer(inv -> {
            EventStatusEntity e = inv.getArgument(0);
            if (e.getId() == null) e.setId(200L);
            return e;
        });

        // mapper
        ChannelEvent mapped = new ChannelEvent();
        try (MockedStatic<ChannelEventMapper> mocked = mockStatic(ChannelEventMapper.class)) {
            mocked.when(() -> ChannelEventMapper.toChannelEvent(any(EventStatusEntity.class)))
                    .thenReturn(mapped);

            // make send throw (simulate channel failure after retries exhausted bubbling up or immediate throw)
            doThrow(new RuntimeException("send failed")).when(retryAbleNotificationService).send(eq(emailChannel), eq(mapped));

            // Act (no exception escapes because your code catches it)
            eventsService.processEvents(dto);

            // Assert: first save(PENDING) happened, but NO later update to SENT
            ArgumentCaptor<EventStatusEntity> saveCaptor = ArgumentCaptor.forClass(EventStatusEntity.class);
            verify(eventStatusRepo, atLeastOnce()).save(saveCaptor.capture());

            // ensure no entity got status "SENT"
            boolean anySent = saveCaptor.getAllValues().stream().anyMatch(e -> "SENT".equals(e.getStatus()));
            assertThat(anySent).isFalse();
        }
    }
}