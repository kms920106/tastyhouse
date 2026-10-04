package com.tastyhouse.application.bug.port.in;

public interface BugReportManagementCommandUseCase {

    void changeStatus(BugReportStatusChangeCommand command);

    void classify(BugReportClassifyCommand command);

    void assign(BugReportAssignCommand command);
}
