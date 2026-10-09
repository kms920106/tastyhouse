package com.tastyhouse.application.bug.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.tastyhouse.domain.bug.model.BugReport;
import com.tastyhouse.domain.bug.model.BugReportImage;
import com.tastyhouse.domain.bug.model.BugReportPlatform;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.application.bug.port.out.write.BugReportImageSavePort;
import com.tastyhouse.application.bug.port.out.write.BugReportSavePort;

@Service
public class BugReportRegistrationService {

    private final BugReportSavePort bugReportSavePort;
    private final BugReportImageSavePort bugReportImageSavePort;

    public BugReportRegistrationService(
        BugReportSavePort bugReportSavePort,
        BugReportImageSavePort bugReportImageSavePort
    ) {
        this.bugReportSavePort = bugReportSavePort;
        this.bugReportImageSavePort = bugReportImageSavePort;
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
        BugReport saved = bugReportSavePort.save(bugReport);

        if (uploadedFileIds == null || uploadedFileIds.isEmpty()) {
            return saved;
        }

        for (int sort = 0; sort < uploadedFileIds.size(); sort++) {
            BugReportImage image = BugReportImage.of(
                saved.getBugReportId(), UploadedFileId.of(uploadedFileIds.get(sort)), sort
            );
            bugReportImageSavePort.save(image);
        }

        return saved;
    }
}
