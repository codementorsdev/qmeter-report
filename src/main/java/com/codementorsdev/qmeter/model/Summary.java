package com.codementorsdev.qmeter.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Summary {
    @JsonProperty("totalSuites")
    private int totalSuites;
    @JsonProperty("totalTestCases")
    private int totalTestCases;
    @JsonProperty("pass")
    private int pass;
    @JsonProperty("fail")
    private int fail;
    @JsonProperty("skip")
    private int skip;
    @JsonProperty("error")
    private int error;
    @JsonProperty("totalExecutionTime")
    private long totalExecutionTime;
    @JsonProperty("startTime")
    private long startTime; // Unix timestamp in milliseconds
    @JsonProperty("endTime")
    private long endTime;   // Unix timestamp in milliseconds
    @JsonProperty("environment")
    private String environment;
    @JsonProperty("platform")
    private String platform;

    public Summary() {}

    public Summary(int totalSuites, int totalTestCases, int pass, int fail, int skip, int error, long totalExecutionTime, long startTime, long endTime, String environment, String platform) {
        this.totalSuites = totalSuites;
        this.totalTestCases = totalTestCases;
        this.pass = pass;
        this.fail = fail;
        this.skip = skip;
        this.error = error;
        this.totalExecutionTime = totalExecutionTime;
        this.startTime = startTime;
        this.endTime = endTime;
        this.environment = environment;
        this.platform = platform;
    }

    // Getters and Setters
    public int getTotalSuites() { return totalSuites; }
    public void setTotalSuites(int totalSuites) { this.totalSuites = totalSuites; }
    public int getTotalTestCases() { return totalTestCases; }
    public void setTotalTestCases(int totalTestCases) { this.totalTestCases = totalTestCases; }
    public int getPass() { return pass; }
    public void setPass(int pass) { this.pass = pass; }
    public int getFail() { return fail; }
    public void setFail(int fail) { this.fail = fail; }
    public int getSkip() { return skip; }
    public void setSkip(int skip) { this.skip = skip; }
    public int getError() { return error; }
    public void setError(int error) { this.error = error; }
    public long getTotalExecutionTime() { return totalExecutionTime; }
    public void setTotalExecutionTime(long totalExecutionTime) { this.totalExecutionTime = totalExecutionTime; }
    public long getStartTime() { return startTime; }
    public void setStartTime(long startTime) { this.startTime = startTime; }
    public long getEndTime() { return endTime; }
    public void setEndTime(long endTime) { this.endTime = endTime; }
    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }
    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }
}
