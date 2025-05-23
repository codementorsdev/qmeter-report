package com.codementorsdev.qmeter.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Illustrative annotation for configuring the test report.
 * This would typically be used in conjunction with a test framework's
 * extension or listener mechanism (e.g., JUnit's @RegisterExtension, TestNG's ISuiteListener).
 * It's not directly processed by the ReportGenerator class itself in this standalone library,
 * but shows how a framework could pass configuration.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface ReportConfig {
    String outputDirectory() default "target/my-test-report";
    String reportFileName() default "test_report.html";
    String environment() default "Local";
    String platform() default "Desktop";
}
