package com.urutte.model;

public enum MediaType {
    IMAGE("image"),
    VIDEO("video"),
    GIF("gif"),
    AUDIO("audio"),
    DOCUMENT("document");
    
    private final String value;
    
    MediaType(String value) {
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
