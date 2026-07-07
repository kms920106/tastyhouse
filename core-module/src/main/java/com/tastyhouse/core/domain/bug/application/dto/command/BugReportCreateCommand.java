package com.tastyhouse.core.domain.bug.application.dto.command;

import java.util.List;

public record BugReportCreateCommand(
    Long memberId,
    String device,
    String title,
    String content,
    List<Long> uploadedFileIds
) {

    public static BugReportCreateCommand of(
        Long memberId,
        String device,
        String title,
        String content,
        List<Long> uploadedFileIds
    ) {
        return new BugReportCreateCommand(memberId, device, title, content, uploadedFileIds);
    }
}
