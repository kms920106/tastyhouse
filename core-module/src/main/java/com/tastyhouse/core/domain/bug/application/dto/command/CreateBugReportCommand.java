package com.tastyhouse.core.domain.bug.application.dto.command;

import java.util.List;

public record CreateBugReportCommand(
    Long memberId,
    String device,
    String title,
    String content,
    List<Long> uploadedFileIds
) {

    public static CreateBugReportCommand of(
        Long memberId,
        String device,
        String title,
        String content,
        List<Long> uploadedFileIds
    ) {
        return new CreateBugReportCommand(memberId, device, title, content, uploadedFileIds);
    }
}
