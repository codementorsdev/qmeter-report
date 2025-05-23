package com.codementorsdev.qmeter.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.UUID;

public class ReportData {
    private String id;
    private List<TestSuite> suites;
    private Summary summary;

    public ReportData() {
        this.id = UUID.randomUUID().toString();
    }

    public ReportData(List<TestSuite> suites, Summary summary) {
        this();
        this.suites = suites;
        this.summary = summary;
    }

    public String getId() {
        return id;
    }

    public List<TestSuite> getSuites() {
        return suites;
    }

    public void setSuites(List<TestSuite> suites) {
        this.suites = suites;
    }

    public Summary getSummary() {
        return summary;
    }

    public void setSummary(Summary summary) {
        this.summary = summary;
    }
}
