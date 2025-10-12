package com.urutte.service;

import com.urutte.dto.NotificationDto;
import com.urutte.model.Notification;
import com.urutte.model.User;
import com.urutte.repository.NotificationRepository;
import com.urutte.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;


    public NotificationDto createNotification(String userId, String fromUserId, String type, 
                                            String title, String message, String relatedEntityType, Long relatedEntityId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        User fromUser = null;
        if (fromUserId != null) {
            fromUser = userRepository.findById(fromUserId)
                    .orElse(null);
        }

        Notification notification = new Notification(user, fromUser, type, title, message, relatedEntityType, relatedEntityId);
        Notification savedNotification = notificationRepository.save(notification);
        
        NotificationDto dto = convertToDto(savedNotification);
        
        // Note: Real-time notifications removed with WebSocket support
        // Notifications will be available via REST API polling
        
        return dto;
    }

    public Page<NotificationDto> getUserNotifications(String userId, int page, int size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Pageable pageable = PageRequest.of(page, size);
        return notificationRepository.findByUserOrderByCreatedAtDesc(user, pageable)
                .map(this::convertToDto);
    }

    public List<NotificationDto> getUnreadNotifications(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return notificationRepository.findUnreadNotifications(user)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public long getUnreadNotificationCount(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    public NotificationDto markAsRead(Long notificationId, String userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        
        if (!notification.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        
        notification.setRead(true);
        Notification savedNotification = notificationRepository.save(notification);
        
        return convertToDto(savedNotification);
    }

    public void markAllAsRead(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Notification> unreadNotifications = notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
        unreadNotifications.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(unreadNotifications);
    }

    private NotificationDto convertToDto(Notification notification) {
        NotificationDto dto = new NotificationDto();
        dto.setId(notification.getId());
        dto.setType(notification.getType());
        dto.setTitle(notification.getTitle());
        dto.setMessage(notification.getMessage());
        dto.setRelatedEntityType(notification.getRelatedEntityType());
        dto.setRelatedEntityId(notification.getRelatedEntityId());
        dto.setRead(notification.isRead());
        dto.setCreatedAt(notification.getCreatedAt());
        
        if (notification.getFromUser() != null) {
            dto.setFromUserId(notification.getFromUser().getId());
            dto.setFromUserName(notification.getFromUser().getName());
            dto.setFromUserAvatar(notification.getFromUser().getPicture());
        }
        
        return dto;
    }
}
