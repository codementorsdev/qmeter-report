package com.codementorsdev.qmeter; // Make sure this package matches your project setup

import com.codementorsdev.qmeter.*;

import java.util.Random;

public class App {
    public static void main(String[] args) throws InterruptedException {
        // 1. Initialize the ReportGenerator with configuration
        // This should typically be done once at the very beginning of your test execution setup.
        ReportConfig config = ReportConfig.builder()
                .outputDirectory("test-reports") // Output directory for the HTML report
                .reportFileName("my_automated_report.html") // Name of the generated HTML file
                .environment("Staging") // Environment detail for the report summary
                .platform("Chrome 120 (Windows 11)") // Platform detail for the report summary
                .build();
        ReportGenerator.initialize(config);

        // Get the singleton instance of the ReportGenerator.
        // It's safe to call getInstance() after initialize().
        ReportGenerator generator = ReportGenerator.getInstance();

        // --- Data for simulating test cases ---
        Random random = new Random();
        String[] statuses = {"Pass", "Fail", "Skip", "Error"};
        String[] environments = {"QA", "Staging", "Production", "Development"};
        String[] platforms = {"Web - Chrome", "Web - Firefox", "Mobile - Android", "API - REST"};
        String[] testCaseNames = {
                "User Login Functionality", "Product Search Results", "Add to Cart Flow",
                "Checkout Process Completion", "Order History Verification", "Profile Update Details",
                "Payment Gateway Integration", "Dashboard Data Display", "Forgot Password Flow"
        };
        String[] stepDescriptions = {
                "Navigate to URL", "Enter credentials", "Click login button", "Verify element visibility",
                "Submit form data", "Wait for AJAX call to complete", "Select item from dropdown",
                "Validate API response", "Capture screenshot"
        };
        String[] logLevels = {"INFO", "DEBUG", "WARN", "ERROR"};
        String[] eventTypes = {"Info", "Warning", "Error", "Debug"};

        // Simulate multiple test suites
        for (int i = 1; i <= 3; i++) {
            // CRITICAL FIX: Capture the suiteId returned by startSuite()
            // This ensures you are using the actual ID the generator assigned to the suite.
            String currentSuiteId = generator.startSuite("Feature Suite " + i);

            // Simulate multiple test cases within each suite
            for (int j = 1; j <= 5; j++) {
                // Pass the correctly obtained currentSuiteId to startTestCase()
                String currentTestCaseId = generator.startTestCase(
                        currentSuiteId, // Pass the ID of the current suite
                        testCaseNames[random.nextInt(testCaseNames.length)] + " - Test #" + j,
                        "Verifies the functionality of " + testCaseNames[j % testCaseNames.length].toLowerCase(),
                        environments[random.nextInt(environments.length)],
                        platforms[random.nextInt(platforms.length)]
                );

                // IMPORTANT: Always check if testCaseId is not null.
                // If startTestCase couldn't find the suite (e.g., due to an incorrect suiteId),
                // it would return null, and subsequent calls would throw NullPointerException.
                if (currentTestCaseId == null) {
                    System.err.println("Skipping test case execution as it could not be started for suite " + currentSuiteId);
                    continue; // Skip the rest of this test case's operations
                }

                // Simulate some initial delay for the test case
                Thread.sleep(random.nextInt(500) + 100);

                // Simulate steps, logs, and events for the current test case
                int numSteps = random.nextInt(5) + 3; // Between 3 and 7 steps
                for (int k = 0; k < numSteps; k++) {
                    String stepStatus = "Pass";
                    // Introduce a chance for steps to fail or error
                    double failureChance = 0.15; // 15% chance for a step to fail
                    if (random.nextDouble() < failureChance) {
                        stepStatus = statuses[random.nextInt(statuses.length - 1) + 1]; // Avoid "Pass"
                    }

                    generator.addStep(
                            currentTestCaseId, // Use the valid testCaseId
                            stepDescriptions[random.nextInt(stepDescriptions.length)],
                            stepStatus,
                            random.nextInt(1000) + 50 // Duration between 50ms and 1050ms
                    );
                    Thread.sleep(random.nextInt(50) + 10); // Small delay between steps

                    generator.addLog(
                            currentTestCaseId, // Use the valid testCaseId
                            String.format("[%s] %s: Step %d executed - %s",
                                    java.time.Instant.now().toString(),
                                    logLevels[random.nextInt(logLevels.length)], k + 1, stepDescriptions[random.nextInt(stepDescriptions.length)])
                    );
                }

                // Add some random events
                if (random.nextDouble() < 0.1) {
                    generator.addEvent(currentTestCaseId, "Warning", "Application warning: Data might be inconsistent.");
                }
                if (random.nextDouble() < 0.05) {
                    generator.addEvent(currentTestCaseId, "Error", "Critical error during API call: Connection refused.");
                }

                // Determine the final status of the test case
                String finalStatus = "Pass";
                // If any step failed or errored, the test case should fail/error
                // This check accesses the test case from the map using the valid `currentTestCaseId`
                if (generator.currentTestCaseMap.get(currentTestCaseId) != null &&
                        generator.currentTestCaseMap.get(currentTestCaseId).getSteps().stream()
                                .anyMatch(s -> "Fail".equals(s.getStatus()) || "Error".equals(s.getStatus()))) {
                    finalStatus = "Fail"; // Or "Error" if an error step was present
                } else {
                    // Otherwise, randomly assign Pass, Skip, or a general Fail/Error for variety
                    double outcome = random.nextDouble();
                    if (outcome < 0.85) { // 85% chance to pass if no step failed
                        finalStatus = "Pass";
                    } else if (outcome < 0.90) { // 5% chance to skip
                        finalStatus = "Skip";
                    } else if (outcome < 0.95) { // 5% chance to fail (even if steps passed)
                        finalStatus = "Fail";
                    } else { // 5% chance to error
                        finalStatus = "Error";
                    }
                }


                generator.endTestCase(currentTestCaseId, finalStatus); // End the test case
                Thread.sleep(random.nextInt(200) + 50); // Simulate delay between test cases
            }
            generator.endSuite(currentSuiteId); // End the current suite using its correct ID
            Thread.sleep(random.nextInt(1000) + 200); // Simulate delay between suites
        }

        // 2. Generate and Flush the report at the end of all tests.
        // This writes the collected data to the HTML file.
        System.out.println("All test simulations completed. Flushing report...");
        generator.flushReport();
        System.out.println("Report generation process finished.");
    }
}