package com.tastyhouse.domain.bug.service;

import java.util.List;

import com.tastyhouse.domain.bug.model.BugReport;
import com.tastyhouse.domain.bug.model.BugReportImage;
import com.tastyhouse.domain.bug.model.BugReportPlatform;
import com.tastyhouse.domain.bug.repository.BugReportImageRepository;
import com.tastyhouse.domain.bug.repository.BugReportRepository;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.member.vo.MemberId;

public class BugReportRegistrationService {
    private final BugReportRepository bugReportRepository;
    private final BugReportImageRepository bugReportImageRepository;

    public BugReportRegistrationService(
        BugReportRepository bugReportRepository,
        BugReportImageRepository bugReportImageRepository
    ) {
        this.bugReportRepository = bugReportRepository;
        this.bugReportImageRepository = bugReportImageRepository;
    }

    public BugReport register(
        MemberId memberId,
        String device,
        String title,
        String content,
        String appVersion,
        BugReportPlatform platform,
        String osVersion,
        List<Long> uploadedFileIds
    ) {
        BugReport bugReport = BugReport.of(memberId, device, title, content, appVersion, platform, osVersion);
        BugReport saved = bugReportRepository.save(bugReport);

        if (uploadedFileIds == null || uploadedFileIds.isEmpty()) {
            return saved;
        }

        for (int sort = 0; sort < uploadedFileIds.size(); sort++) {
            BugReportImage image = BugReportImage.of(
                saved.getBugReportId(), UploadedFileId.of(uploadedFileIds.get(sort)), sort
            );
            bugReportImageRepository.save(image);
        }

        return saved;
    }
}
