package com.tastyhouse.application.bug.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.tastyhouse.domain.bug.model.BugReport;
import com.tastyhouse.domain.bug.model.BugReportImage;
import com.tastyhouse.domain.bug.model.BugReportPlatform;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.application.bug.port.out.write.BugReportImagePersistencePort;
import com.tastyhouse.application.bug.port.out.write.BugReportPersistencePort;

@Service
public class BugReportRegistrationService {

    private final BugReportPersistencePort bugReportPersistencePort;
    private final BugReportImagePersistencePort bugReportImagePersistencePort;

    public BugReportRegistrationService(
        BugReportPersistencePort bugReportPersistencePort,
        BugReportImagePersistencePort bugReportImagePersistencePort
    ) {
        this.bugReportPersistencePort = bugReportPersistencePort;
        this.bugReportImagePersistencePort = bugReportImagePersistencePort;
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
        BugReport saved = bugReportPersistencePort.save(bugReport);

        if (uploadedFileIds == null || uploadedFileIds.isEmpty()) {
            return saved;
        }

        for (int sort = 0; sort < uploadedFileIds.size(); sort++) {
            BugReportImage image = BugReportImage.of(
                saved.getBugReportId(), UploadedFileId.of(uploadedFileIds.get(sort)), sort
            );
            bugReportImagePersistencePort.save(image);
        }

        return saved;
    }
}
