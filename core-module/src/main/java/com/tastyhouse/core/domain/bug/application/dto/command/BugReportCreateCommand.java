package com.tastyhouse.core.domain.bug.application.dto.command;

import java.util.List;

import com.tastyhouse.core.domain.bug.domain.model.BugReportPlatform;

public record BugReportCreateCommand(
    Long memberId,
    String device,
    String title,
    String content,
    String appVersion,
    BugReportPlatform platform,
    String osVersion,
    List<Long> uploadedFileIds
) {

    public static BugReportCreateCommand of(
        Long memberId,
        String device,
        String title,
        String content,
        String appVersion,
        BugReportPlatform platform,
        String osVersion,
        List<Long> uploadedFileIds
    ) {
        return new BugReportCreateCommand(memberId, device, title, content, appVersion, platform, osVersion, uploadedFileIds);
    }
}
