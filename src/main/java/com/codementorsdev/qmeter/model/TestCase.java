package com.codementorsdev.qmeter.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.UUID;

public class TestCase {
    private String id;
    private String name;
    private String description;
    private String status; // Pass, Fail, Skip, Error
    private long duration; // in ms
    @JsonProperty("startTime")
    private long startTime;
    @JsonProperty("endTime")
    private long endTime;
    private String environment;
    private String platform;
    private List<TestStep> steps;
    private List<String> logs; // Simple list of log messages
    private List<TestEvent> events;

    public TestCase() {
        this.id = UUID.randomUUID().toString();
    }

    public TestCase(String name, String description, String environment, String platform) {
        this();
        this.name = name;
        this.description = description;
        this.environment = environment;
        this.platform = platform;
        this.startTime = System.currentTimeMillis();
    }

    public void end(String status) {
        this.endTime = System.currentTimeMillis();
        this.duration = this.endTime - this.startTime;
        this.status = status;
    }

    // Getters and Setters
    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; } // Should ideally be set by end()
    public long getDuration() { return duration; }
    public void setDuration(long duration) { this.duration = duration; }
    public long getStartTime() { return startTime; }
    public void setStartTime(long startTime) { this.startTime = startTime; }
    public long getEndTime() { return endTime; }
    public void setEndTime(long endTime) { this.endTime = endTime; }
    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }
    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }
    public List<TestStep> getSteps() { return steps; }
    public void setSteps(List<TestStep> steps) { this.steps = steps; }
    public List<String> getLogs() { return logs; }
    public void setLogs(List<String> logs) { this.logs = logs; }
    public List<TestEvent> getEvents() { return events; }
    public void setEvents(List<TestEvent> events) { this.events = events; }
}