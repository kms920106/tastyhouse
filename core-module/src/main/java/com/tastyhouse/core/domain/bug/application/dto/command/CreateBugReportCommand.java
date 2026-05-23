package com.tastyhouse.core.domain.bug.application.dto.command;

import java.util.List;

public record CreateBugReportCommand(
    Long memberId,
    String device,
    String title,
    String content,
    List<Long> uploadedFileIds
) {
}
