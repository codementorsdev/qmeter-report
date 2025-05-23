package com.codementorsdev.qmeter;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ReportConfig {
    private Path outputDirectory;
    private String reportFileName;
    private String environment;
    private String platform;

    private ReportConfig(Builder builder) {
        this.outputDirectory = builder.outputDirectory;
        this.reportFileName = builder.reportFileName;
        this.environment = builder.environment;
        this.platform = builder.platform;
    }

    public Path getOutputDirectory() {
        return outputDirectory;
    }

    public String getReportFileName() {
        return reportFileName;
    }

    public String getEnvironment() {
        return environment;
    }

    public String getPlatform() {
        return platform;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Path outputDirectory = Paths.get("target", "test-automation-report"); // Default output directory
        private String reportFileName = "report.html"; // Default report file name
        private String environment = "Unknown";
        private String platform = "Unknown";

        public Builder outputDirectory(String path) {
            this.outputDirectory = Paths.get(path);
            return this;
        }

        public Builder outputDirectory(Path path) {
            this.outputDirectory = path;
            return this;
        }

        public Builder reportFileName(String fileName) {
            this.reportFileName = fileName;
            return this;
        }

        public Builder environment(String environment) {
            this.environment = environment;
            return this;
        }

        public Builder platform(String platform) {
            this.platform = platform;
            return this;
        }

        public ReportConfig build() {
            return new ReportConfig(this);
        }
    }
}