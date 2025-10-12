package com.urutte.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ThreadPollDto {
    private Long id;
    private Long threadId;
    private String question;
    private Boolean isMultipleChoice;
    private LocalDateTime expiresAt;
    private Integer totalVotes;
    private LocalDateTime createdAt;
    private List<PollOptionDto> options;
    private Boolean hasUserVoted;
    private Long userVotedOptionId;
    
    // Constructors
    public ThreadPollDto() {}
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getThreadId() { return threadId; }
    public void setThreadId(Long threadId) { this.threadId = threadId; }
    
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    
    public Boolean getIsMultipleChoice() { return isMultipleChoice; }
    public void setIsMultipleChoice(Boolean isMultipleChoice) { this.isMultipleChoice = isMultipleChoice; }
    
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    
    public Integer getTotalVotes() { return totalVotes; }
    public void setTotalVotes(Integer totalVotes) { this.totalVotes = totalVotes; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public List<PollOptionDto> getOptions() { return options; }
    public void setOptions(List<PollOptionDto> options) { this.options = options; }
    
    public Boolean getHasUserVoted() { return hasUserVoted; }
    public void setHasUserVoted(Boolean hasUserVoted) { this.hasUserVoted = hasUserVoted; }
    
    public Long getUserVotedOptionId() { return userVotedOptionId; }
    public void setUserVotedOptionId(Long userVotedOptionId) { this.userVotedOptionId = userVotedOptionId; }
}
