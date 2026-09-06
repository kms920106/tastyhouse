package com.tastyhouse.application.bug.port.in;

import com.tastyhouse.application.shared.marker.WebApp;

@WebApp
public interface BugReportCommandUseCase {

    Long createBugReport(BugReportCreateCommand command);
}
