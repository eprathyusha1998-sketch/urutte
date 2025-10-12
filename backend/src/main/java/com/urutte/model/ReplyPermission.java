package com.urutte.model;

public enum ReplyPermission {
    ANYONE("Anyone"),
    FOLLOWERS("Your followers"),
    FOLLOWING("Profiles you follow"),
    MENTIONED_ONLY("Mentioned only");
    
    private final String displayName;
    
    ReplyPermission(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
