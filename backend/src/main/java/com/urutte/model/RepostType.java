package com.urutte.model;

public enum RepostType {
    REPOST("repost"),
    QUOTE("quote");
    
    private final String value;
    
    RepostType(String value) {
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
