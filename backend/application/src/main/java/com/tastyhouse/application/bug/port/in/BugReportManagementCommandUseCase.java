package com.tastyhouse.application.bug.port.in;

import com.tastyhouse.application.shared.marker.AdminApp;

@AdminApp
public interface BugReportManagementCommandUseCase {

    void changeStatus(BugReportStatusChangeCommand command);

    void classify(BugReportClassifyCommand command);

    void assign(BugReportAssignCommand command);
}
