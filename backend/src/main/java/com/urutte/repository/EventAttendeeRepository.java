package com.urutte.repository;

import com.urutte.model.Event;
import com.urutte.model.EventAttendee;
import com.urutte.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventAttendeeRepository extends JpaRepository<EventAttendee, Long> {
    
    // Find attendees by event
    List<EventAttendee> findByEvent(Event event);
    
    // Find events by user
    List<EventAttendee> findByUserOrderByCreatedAtDesc(User user);
    
    // Find attendee by event and user
    Optional<EventAttendee> findByEventAndUser(Event event, User user);
    
    // Check if user is attending an event
    boolean existsByEventAndUser(Event event, User user);
    
    // Count attendees for an event
    long countByEvent(Event event);
    
    // Delete attendee by event and user
    void deleteByEventAndUser(Event event, User user);
    
    // Find upcoming events for a user
    @Query("SELECT ea FROM EventAttendee ea WHERE ea.user = :user AND ea.event.eventDate > CURRENT_TIMESTAMP ORDER BY ea.event.eventDate ASC")
    List<EventAttendee> findUpcomingEventsByUser(@Param("user") User user);
}
