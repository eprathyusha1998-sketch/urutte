package com.urutte.service;

import com.urutte.dto.MessageDto;
import com.urutte.dto.UserDto;
import com.urutte.model.Message;
import com.urutte.model.User;
import com.urutte.repository.MessageRepository;
import com.urutte.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;


    public Message saveMessage(Message message) {
        return messageRepository.save(message);
    }

    public List<MessageDto> getConversation(String userId1, String userId2, int page, int size) {
        User user1 = userRepository.findById(userId1)
                .orElseThrow(() -> new RuntimeException("User 1 not found"));
        User user2 = userRepository.findById(userId2)
                .orElseThrow(() -> new RuntimeException("User 2 not found"));
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Message> messages = messageRepository.findConversationBetweenUsersPaged(user1, user2, pageable);
        
        return messages.getContent()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<UserDto> getConversationPartners(String userId) {
        // For now, return empty list to avoid Hibernate mapping issues
        // TODO: Fix the repository queries to avoid ClassCastException
        return new ArrayList<>();
    }

    public List<MessageDto> getUnreadMessages(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return messageRepository.findUnreadMessages(user)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public long getUnreadMessageCount(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return messageRepository.countByReceiverAndIsReadFalse(user);
    }

    public void markMessagesAsRead(String userId, String senderId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        
        List<Message> unreadMessages = messageRepository.findUnreadMessages(user);
        unreadMessages.stream()
                .filter(message -> message.getSender().getId().equals(senderId))
                .forEach(message -> message.setRead(true));
        
        messageRepository.saveAll(unreadMessages);
    }

    public MessageDto sendMessage(String senderId, String receiverId, String content, String messageType, String mediaUrl) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));
        
        Message message = new Message(sender, receiver, content, messageType, mediaUrl);
        Message savedMessage = messageRepository.save(message);
        
        return convertToDto(savedMessage);
    }

    public MessageDto convertToDto(Message message) {
        MessageDto dto = new MessageDto();
        dto.setId(message.getId());
        dto.setContent(message.getContent());
        dto.setMessageType(message.getMessageType());
        dto.setMediaUrl(message.getMediaUrl());
        dto.setRead(message.isRead());
        dto.setCreatedAt(message.getCreatedAt());
        
        dto.setSenderId(message.getSender().getId());
        dto.setSenderName(message.getSender().getName());
        dto.setSenderAvatar(message.getSender().getPicture());
        
        dto.setReceiverId(message.getReceiver().getId());
        dto.setReceiverName(message.getReceiver().getName());
        dto.setReceiverAvatar(message.getReceiver().getPicture());
        
        return dto;
    }

    public UserDto convertUserToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setUsername(user.getUsername());
        dto.setPicture(user.getPicture());
        dto.setCoverPhoto(user.getCoverPhoto());
        dto.setBio(user.getBio());
        dto.setLocation(user.getLocation());
        dto.setWebsite(user.getWebsite());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setDateOfBirth(user.getDateOfBirth());
        dto.setGender(user.getGender());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        dto.setVerified(user.getIsVerified() != null ? user.getIsVerified() : false);
        dto.setPrivate(user.getIsPrivate() != null ? user.getIsPrivate() : false);
        dto.setActive(user.getIsActive() != null ? user.getIsActive() : true);
        return dto;
    }
}

