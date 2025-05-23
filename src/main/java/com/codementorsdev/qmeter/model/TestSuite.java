package com.codementorsdev.qmeter.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.UUID;

public class TestSuite {
    private String id;
    private String name;
    private String status; // Pass/Fail
    private long duration; // in ms
    @JsonProperty("startTime")
    private long startTime;
    @JsonProperty("endTime")
    private long endTime;
    @JsonProperty("testCases")
    private List<TestCase> testCases;

    public TestSuite() {
        this.id = UUID.randomUUID().toString();
    }

    public TestSuite(String name, List<TestCase> testCases) {
        this();
        this.name = name;
        this.testCases = testCases;
        calculateMetrics();
    }

    private void calculateMetrics() {
        if (testCases == null || testCases.isEmpty()) {
            this.status = "No Tests";
            this.duration = 0;
            this.startTime = 0;
            this.endTime = 0;
            return;
        }

        long minStartTime = Long.MAX_VALUE;
        long maxEndTime = Long.MIN_VALUE;
        boolean failed = false;
        boolean errored = false;

        for (TestCase tc : testCases) {
            if (tc.getStartTime() < minStartTime) minStartTime = tc.getStartTime();
            if (tc.getEndTime() > maxEndTime) maxEndTime = tc.getEndTime();
            if ("Fail".equals(tc.getStatus())) failed = true;
            if ("Error".equals(tc.getStatus())) errored = true;
        }

        this.startTime = minStartTime;
        this.endTime = maxEndTime;
        this.duration = maxEndTime - minStartTime;
        this.status = failed || errored ? "Fail" : "Pass";
    }

    // Getters and Setters
    public String getId() { return id; }

    public void setId(String id) {
        this.id = id;
    }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; } // Can be set manually if needed
    public long getDuration() { return duration; }
    public void setDuration(long duration) { this.duration = duration; }
    public long getStartTime() { return startTime; }
    public void setStartTime(long startTime) { this.startTime = startTime; }
    public long getEndTime() { return endTime; }
    public void setEndTime(long endTime) { this.endTime = endTime; }
    public List<TestCase> getTestCases() { return testCases; }
    public void setTestCases(List<TestCase> testCases) {
        this.testCases = testCases;
        calculateMetrics(); // Recalculate if test cases are updated
    }
}