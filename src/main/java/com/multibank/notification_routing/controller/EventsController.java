package com.multibank.notification_routing.controller;

import com.multibank.notification_routing.dto.EventsRequestDto;
import com.multibank.notification_routing.dto.EventsResponseDto;
import com.multibank.notification_routing.service.EventsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/events")
public class EventsController {

    @Autowired
    private EventsService eventService;

    @ResponseBody
    @PostMapping(produces = "application/json")
    public ResponseEntity<EventsResponseDto> processEvents(@RequestBody EventsRequestDto event) {
        eventService.processEvents(event);
        return ResponseEntity.ok(new EventsResponseDto("SUCCESS"));
    }

    @ResponseBody
    @GetMapping(produces = "application/json", path = "/{id}")
    public ResponseEntity<EventsResponseDto> getEventStatusById(@PathVariable("id") String id) {
        return ResponseEntity.ok(eventService.getEventStatusById(id));
    }

    @ResponseBody
    @GetMapping(produces = "application/json")
    public ResponseEntity<List<String>> getFailedEventsId() {
        return ResponseEntity.ok(eventService.getFailedEvents());
    }

}
