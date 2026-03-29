package com.tastyhouse.core.service;

import com.tastyhouse.core.entity.report.BugReport;
import com.tastyhouse.core.entity.report.BugReportImage;
import com.tastyhouse.core.repository.report.BugReportImageJpaRepository;
import com.tastyhouse.core.repository.report.BugReportJpaRepository;
import com.tastyhouse.core.repository.report.ReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BugReportCoreService {

    private final ReportRepository reportRepository;
    private final BugReportJpaRepository bugReportJpaRepository;
    private final BugReportImageJpaRepository bugReportImageJpaRepository;

    @Transactional(readOnly = true)
    public List<BugReport> findBugReportsByMemberId(Long memberId) {
        return reportRepository.findBugReportsByMemberIdOrderByCreatedAtDesc(memberId);
    }

    @Transactional(readOnly = true)
    public List<BugReportImage> findBugReportImages(Long bugReportId) {
        return reportRepository.findBugReportImagesByBugReportId(bugReportId);
    }

    @Transactional
    public BugReport save(BugReport bugReport) {
        return bugReportJpaRepository.save(bugReport);
    }

    @Transactional
    public void saveBugReportImages(List<BugReportImage> images) {
        bugReportImageJpaRepository.saveAll(images);
    }
}
