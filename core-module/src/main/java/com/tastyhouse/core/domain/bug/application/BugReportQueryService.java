package com.tastyhouse.core.domain.bug.application;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.bug.domain.model.BugReport;
import com.tastyhouse.core.domain.bug.domain.model.BugReportImage;
import com.tastyhouse.core.domain.bug.domain.repository.BugReportImageRepository;
import com.tastyhouse.core.domain.bug.domain.repository.BugReportRepository;
import com.tastyhouse.core.domain.bug.domain.vo.BugReportId;
import com.tastyhouse.core.domain.bug.application.dto.BugReportAdminListItemDto;
import com.tastyhouse.core.domain.bug.application.dto.BugReportAdminSearchCondition;
import com.tastyhouse.core.domain.bug.application.dto.BugReportDetailDto;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BugReportQueryService {

    private final BugReportRepository bugReportRepository;
    private final BugReportImageRepository bugReportImageRepository;

    public PageResult<BugReportAdminListItemDto> findAllBugReports(BugReportAdminSearchCondition condition, int page, int size) {
        PageQuery pageQuery = PageQuery.of(page, size);
        return bugReportRepository.findAllBugReports(condition, pageQuery);
    }

    public BugReportDetailDto findDetailById(BugReportId bugReportId) {
        BugReport bugReport = bugReportRepository.findById(bugReportId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.BUG_REPORT_NOT_FOUND));

        List<Long> imageFileIds = bugReportImageRepository.findByBugReportId(bugReport.getId()).stream()
            .map(BugReportImage::getImageFileId)
            .toList();

        return BugReportDetailDto.from(
            bugReport.getBugReportId(),
            bugReport.getMemberId(),
            bugReport.getDevice(),
            bugReport.getTitle(),
            bugReport.getContent(),
            bugReport.getStatus(),
            bugReport.getCategory(),
            bugReport.getPriority(),
            bugReport.getAssigneeAdminId(),
            bugReport.getAdminAnswer(),
            bugReport.getResolvedAt(),
            bugReport.getAppVersion(),
            bugReport.getPlatform(),
            bugReport.getOsVersion(),
            imageFileIds,
            bugReport.getCreatedAt(),
            bugReport.getUpdatedAt()
        );
    }
}
