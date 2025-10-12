package com.urutte.model;

public enum ThreadType {
    ORIGINAL("original"),
    REPLY("reply"),
    QUOTE("quote"),
    RETWEET("retweet");
    
    private final String value;
    
    ThreadType(String value) {
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
