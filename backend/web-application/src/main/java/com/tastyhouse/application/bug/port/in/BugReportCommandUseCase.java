package com.tastyhouse.application.bug.port.in;

public interface BugReportCommandUseCase {

    Long createBugReport(BugReportCreateCommand command);
}
