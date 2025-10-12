package com.urutte.controller;

import com.urutte.dto.CreateEventDto;
import com.urutte.dto.EventDto;
import com.urutte.model.User;
import com.urutte.service.EventService;
import com.urutte.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
public class EventController {

    @Autowired
    private EventService eventService;

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<EventDto> createEvent(@RequestBody CreateEventDto createEventDto,
                                             @AuthenticationPrincipal OidcUser principal) {
        User user = userService.getOrCreateUser(principal);
        EventDto event = eventService.createEvent(createEventDto, user.getId());
        return ResponseEntity.ok(event);
    }

    @GetMapping
    public ResponseEntity<Page<EventDto>> getAllEvents(@RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "20") int size,
                                                     @AuthenticationPrincipal OidcUser principal) {
        User user = userService.getOrCreateUser(principal);
        Page<EventDto> events = eventService.getAllEvents(user.getId(), page, size);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventDto> getEventById(@PathVariable Long eventId,
                                               @AuthenticationPrincipal OidcUser principal) {
        User user = userService.getOrCreateUser(principal);
        EventDto event = eventService.getEventById(eventId, user.getId());
        return ResponseEntity.ok(event);
    }

    @GetMapping("/upcoming")
    public ResponseEntity<Page<EventDto>> getUpcomingEvents(@RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "20") int size,
                                                         @AuthenticationPrincipal OidcUser principal) {
        User user = userService.getOrCreateUser(principal);
        Page<EventDto> events = eventService.getUpcomingEvents(user.getId(), page, size);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<Page<EventDto>> getEventsByCategory(@PathVariable String category,
                                                           @RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "20") int size,
                                                           @AuthenticationPrincipal OidcUser principal) {
        User user = userService.getOrCreateUser(principal);
        Page<EventDto> events = eventService.getEventsByCategory(category, user.getId(), page, size);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/free")
    public ResponseEntity<Page<EventDto>> getFreeEvents(@RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "20") int size,
                                                     @AuthenticationPrincipal OidcUser principal) {
        User user = userService.getOrCreateUser(principal);
        Page<EventDto> events = eventService.getFreeEvents(user.getId(), page, size);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/paid")
    public ResponseEntity<Page<EventDto>> getPaidEvents(@RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "20") int size,
                                                      @AuthenticationPrincipal OidcUser principal) {
        User user = userService.getOrCreateUser(principal);
        Page<EventDto> events = eventService.getPaidEvents(user.getId(), page, size);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<EventDto>> searchEvents(@RequestParam String q,
                                                     @RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "20") int size,
                                                     @AuthenticationPrincipal OidcUser principal) {
        User user = userService.getOrCreateUser(principal);
        Page<EventDto> events = eventService.searchEvents(q, user.getId(), page, size);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/trending")
    public ResponseEntity<Page<EventDto>> getTrendingEvents(@RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "20") int size,
                                                         @AuthenticationPrincipal OidcUser principal) {
        User user = userService.getOrCreateUser(principal);
        Page<EventDto> events = eventService.getTrendingEvents(user.getId(), page, size);
        return ResponseEntity.ok(events);
    }

    @PostMapping("/{eventId}/attend")
    public ResponseEntity<EventDto> toggleAttendance(@PathVariable Long eventId,
                                                   @AuthenticationPrincipal OidcUser principal) {
        User user = userService.getOrCreateUser(principal);
        EventDto event = eventService.toggleAttendance(eventId, user.getId());
        return ResponseEntity.ok(event);
    }
}
