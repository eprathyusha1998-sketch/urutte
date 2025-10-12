package com.urutte.repository;

import com.urutte.model.Event;
import com.urutte.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    
    // Find events by category
    Page<Event> findByCategoryAndIsActiveTrue(String category, Pageable pageable);
    
    // Find events by organizer
    Page<Event> findByOrganizerAndIsActiveTrue(User organizer, Pageable pageable);
    
    // Find upcoming events
    @Query("SELECT e FROM Event e WHERE e.isActive = true AND e.eventDate > :now ORDER BY e.eventDate ASC")
    Page<Event> findUpcomingEvents(@Param("now") LocalDateTime now, Pageable pageable);
    
    // Find events by date range
    @Query("SELECT e FROM Event e WHERE e.isActive = true AND e.eventDate BETWEEN :startDate AND :endDate ORDER BY e.eventDate ASC")
    Page<Event> findEventsByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);
    
    // Find events by location
    @Query("SELECT e FROM Event e WHERE e.isActive = true AND LOWER(e.location) LIKE LOWER(CONCAT('%', :location, '%'))")
    Page<Event> findByLocation(@Param("location") String location, Pageable pageable);
    
    // Find free events
    @Query("SELECT e FROM Event e WHERE e.isActive = true AND (e.price IS NULL OR e.price = 0)")
    Page<Event> findFreeEvents(Pageable pageable);
    
    // Find paid events
    @Query("SELECT e FROM Event e WHERE e.isActive = true AND e.price > 0")
    Page<Event> findPaidEvents(Pageable pageable);
    
    // Search events by title or description
    @Query("SELECT e FROM Event e WHERE e.isActive = true AND " +
           "(LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Event> searchEvents(@Param("keyword") String keyword, Pageable pageable);
    
    // Find active events
    Page<Event> findByIsActiveTrue(Pageable pageable);
    
    // Find events by multiple categories
    @Query("SELECT e FROM Event e WHERE e.isActive = true AND e.category IN :categories")
    Page<Event> findByCategories(@Param("categories") List<String> categories, Pageable pageable);
    
    // Find trending events (most attendees)
    @Query("SELECT e FROM Event e WHERE e.isActive = true ORDER BY SIZE(e.attendees) DESC")
    Page<Event> findTrendingEvents(Pageable pageable);
    
    // Find recently created events
    @Query("SELECT e FROM Event e WHERE e.isActive = true ORDER BY e.createdAt DESC")
    Page<Event> findRecentEvents(Pageable pageable);
}
