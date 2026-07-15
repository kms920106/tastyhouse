package com.tastyhouse.core.domain.bug.application.dto.command;

import java.util.List;

import com.tastyhouse.core.domain.bug.domain.model.BugReportPlatform;
import com.tastyhouse.core.domain.member.domain.vo.MemberId;

public record BugReportCreateCommand(
    MemberId memberId,
    String device,
    String title,
    String content,
    String appVersion,
    BugReportPlatform platform,
    String osVersion,
    List<Long> uploadedFileIds
) {

    public static BugReportCreateCommand of(
        MemberId memberId,
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
