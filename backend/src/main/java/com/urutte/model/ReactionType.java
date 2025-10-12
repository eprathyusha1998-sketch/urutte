package com.urutte.model;

public enum ReactionType {
    LIKE("like"),
    LOVE("love"),
    LAUGH("laugh"),
    ANGRY("angry"),
    SAD("sad"),
    WOW("wow");
    
    private final String value;
    
    ReactionType(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public String toString() {
        return value;
    }
}
