package com.tastyhouse.application.bug.port.in;

public interface BugReportCreateUseCase {

    Long createBugReport(BugReportCreateCommand command);
}
