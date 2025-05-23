package com.codementorsdev.qmeter.model;

import java.util.UUID;

public class TestLog {
    private String id;
    private long timestamp;
    private String level; // INFO, DEBUG, WARN, ERROR
    private String message;

    public TestLog() { this.id = UUID.randomUUID().toString(); }

    public TestLog(long timestamp, String level, String message) {
        this();
        this.timestamp = timestamp;
        this.level = level;
        this.message = message;
    }

    // Getters and Setters
    public String getId() { return id; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
