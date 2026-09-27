package com.tastyhouse.application.bug.port.out.write;

public record BugReportImageState(
    Long id,
    Long bugReportId,
    Long imageFileId,
    Integer sort
) {
}
