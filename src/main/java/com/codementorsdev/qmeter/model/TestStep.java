package com.codementorsdev.qmeter.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class TestStep {
    private String id;
    private String description;
    private String status; // Pass, Fail, Skip, Error (could be granular)
    private long duration; // in ms

    public TestStep() {
        this.id = UUID.randomUUID().toString();
    }

    public TestStep(String description, String status, long duration) {
        this();
        this.description = description;
        this.status = status;
        this.duration = duration;
    }

    // Getters and Setters
    public String getId() { return id; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public long getDuration() { return duration; }
    public void setDuration(long duration) { this.duration = duration; }
}