package com.urutte.dto;

import java.time.LocalDateTime;

public class PollOptionDto {
    private Long id;
    private Long pollId;
    private String optionText;
    private Integer votesCount;
    private Integer displayOrder;
    private LocalDateTime createdAt;
    private Double percentage; // Calculated percentage of votes
    
    // Constructors
    public PollOptionDto() {}
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getPollId() { return pollId; }
    public void setPollId(Long pollId) { this.pollId = pollId; }
    
    public String getOptionText() { return optionText; }
    public void setOptionText(String optionText) { this.optionText = optionText; }
    
    public Integer getVotesCount() { return votesCount; }
    public void setVotesCount(Integer votesCount) { this.votesCount = votesCount; }
    
    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public Double getPercentage() { return percentage; }
    public void setPercentage(Double percentage) { this.percentage = percentage; }
}
