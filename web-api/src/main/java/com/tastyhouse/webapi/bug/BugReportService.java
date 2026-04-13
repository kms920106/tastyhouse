package com.tastyhouse.webapi.bug;

import com.tastyhouse.core.entity.report.BugReport;
import com.tastyhouse.core.entity.report.BugReportImage;
import com.tastyhouse.core.service.BugReportCoreService;
import com.tastyhouse.webapi.bug.request.BugReportCreateRequest;
import com.tastyhouse.webapi.bug.response.BugReportResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BugReportService {

    private final BugReportCoreService bugReportCoreService;

    @Transactional
    public BugReportResponse createBugReport(Long memberId, BugReportCreateRequest request) {
        BugReport bugReport = BugReport.builder()
            .memberId(memberId)
            .device(request.device())
            .title(request.title())
            .content(request.content())
            .build();

        BugReport savedReport = bugReportCoreService.save(bugReport);

        List<Long> uploadedFileIds = request.uploadedFileIds();
        if (uploadedFileIds != null && !uploadedFileIds.isEmpty()) {
            for (int i = 0; i < uploadedFileIds.size(); i++) {
                BugReportImage image = BugReportImage.builder()
                    .bugReportId(savedReport.getId())
                    .uploadedFileId(uploadedFileIds.get(i))
                    .sort(i)
                    .build();
                bugReportCoreService.saveBugReportImage(image);
            }
        }

        return BugReportResponse.from(
            savedReport.getId(),
            savedReport.getDevice(),
            savedReport.getTitle(),
            savedReport.getContent(),
            uploadedFileIds,
            savedReport.getCreatedAt()
        );
    }
}
