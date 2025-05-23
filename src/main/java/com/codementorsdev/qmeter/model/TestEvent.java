package com.codementorsdev.qmeter.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class TestEvent {
    private String id;
    private long timestamp;
    private String type; // Info, Warning, Debug, Error
    private String message;

    public TestEvent() {
        this.id = UUID.randomUUID().toString();
    }

    public TestEvent(long timestamp, String type, String message) {
        this();
        this.timestamp = timestamp;
        this.type = type;
        this.message = message;
    }

    // Getters and Setters
    public String getId() { return id; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
