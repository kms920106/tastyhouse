package com.tastyhouse.application.bug.port.in;

public interface BugReportManagementStatusChangeUseCase {

    void changeStatus(BugReportStatusChangeCommand command);
}
