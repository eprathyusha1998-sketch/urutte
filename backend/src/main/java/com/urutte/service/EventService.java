package com.urutte.service;

import com.urutte.dto.CreateEventDto;
import com.urutte.dto.EventDto;
import com.urutte.model.Event;
import com.urutte.model.EventAttendee;
import com.urutte.model.User;
import com.urutte.repository.EventAttendeeRepository;
import com.urutte.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class EventService {
    
    @Autowired
    private EventRepository eventRepository;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private EventAttendeeRepository eventAttendeeRepository;

    public EventDto createEvent(CreateEventDto createEventDto, String userId) {
        User user = userService.getUserById(userId);
        
        Event event = new Event();
        event.setTitle(createEventDto.getTitle());
        event.setDescription(createEventDto.getDescription());
        event.setEventDate(createEventDto.getEventDate());
        event.setEndDate(createEventDto.getEndDate());
        event.setLocation(createEventDto.getLocation());
        event.setCategory(createEventDto.getCategory());
        event.setImageUrl(createEventDto.getImageUrl());
        event.setPrice(createEventDto.getPrice());
        event.setMaxAttendees(createEventDto.getMaxAttendees());
        event.setOrganizer(user);
        
        Event savedEvent = eventRepository.save(event);
        return convertToDto(savedEvent, userId);
    }

    public Page<EventDto> getAllEvents(String currentUserId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return eventRepository.findByIsActiveTrue(pageable)
            .map(event -> convertToDto(event, currentUserId));
    }
    
    public Page<EventDto> getUpcomingEvents(String currentUserId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return eventRepository.findUpcomingEvents(LocalDateTime.now(), pageable)
            .map(event -> convertToDto(event, currentUserId));
    }
    
    public Page<EventDto> getEventsByCategory(String category, String currentUserId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return eventRepository.findByCategoryAndIsActiveTrue(category, pageable)
            .map(event -> convertToDto(event, currentUserId));
    }
    
    public Page<EventDto> getFreeEvents(String currentUserId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return eventRepository.findFreeEvents(pageable)
            .map(event -> convertToDto(event, currentUserId));
    }
    
    public Page<EventDto> getPaidEvents(String currentUserId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return eventRepository.findPaidEvents(pageable)
            .map(event -> convertToDto(event, currentUserId));
    }
    
    public Page<EventDto> searchEvents(String keyword, String currentUserId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return eventRepository.searchEvents(keyword, pageable)
            .map(event -> convertToDto(event, currentUserId));
    }
    
    public Page<EventDto> getTrendingEvents(String currentUserId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return eventRepository.findTrendingEvents(pageable)
            .map(event -> convertToDto(event, currentUserId));
    }
    
    public EventDto getEventById(Long eventId, String currentUserId) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new RuntimeException("Event not found"));
        return convertToDto(event, currentUserId);
    }
    
    public EventDto toggleAttendance(Long eventId, String userId) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new RuntimeException("Event not found"));
        
        User user = userService.getUserById(userId);
        
        // Check if already attending
        if (eventAttendeeRepository.existsByEventAndUser(event, user)) {
            eventAttendeeRepository.deleteByEventAndUser(event, user);
        } else {
            // Check if event has max attendees limit
            if (event.getMaxAttendees() != null) {
                long currentAttendees = eventAttendeeRepository.countByEvent(event);
                if (currentAttendees >= event.getMaxAttendees()) {
                    throw new RuntimeException("Event is full");
                }
            }
            
            EventAttendee attendee = new EventAttendee(event, user);
            eventAttendeeRepository.save(attendee);
        }
        
        return convertToDto(event, userId);
    }

    private EventDto convertToDto(Event event, String currentUserId) {
        EventDto dto = new EventDto();
        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setDescription(event.getDescription());
        dto.setEventDate(event.getEventDate());
        dto.setEndDate(event.getEndDate());
        dto.setLocation(event.getLocation());
        dto.setCategory(event.getCategory());
        dto.setImageUrl(event.getImageUrl());
        dto.setPrice(event.getPrice());
        dto.setMaxAttendees(event.getMaxAttendees());
        dto.setIsActive(event.getIsActive());
        dto.setCreatedAt(event.getCreatedAt());
        dto.setUpdatedAt(event.getUpdatedAt());
        
        // Set organizer information
        dto.setOrganizerId(event.getOrganizer().getId());
        dto.setOrganizerName(event.getOrganizer().getName());
        dto.setOrganizerAvatar(event.getOrganizer().getPicture());
        
        // Set attendee count
        dto.setAttendeeCount(eventAttendeeRepository.countByEvent(event));
        
        // Set user interaction status
        if (currentUserId != null) {
            User currentUser = userService.getUserById(currentUserId);
            dto.setIsAttending(eventAttendeeRepository.existsByEventAndUser(event, currentUser));
        }
        
        return dto;
    }
}
