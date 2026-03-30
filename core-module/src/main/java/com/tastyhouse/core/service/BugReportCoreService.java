package com.tastyhouse.core.service;

import com.tastyhouse.core.entity.report.BugReport;
import com.tastyhouse.core.entity.report.BugReportImage;
import com.tastyhouse.core.repository.report.BugReportImageJpaRepository;
import com.tastyhouse.core.repository.report.BugReportJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BugReportCoreService {

    private final BugReportJpaRepository bugReportJpaRepository;
    private final BugReportImageJpaRepository bugReportImageJpaRepository;

    @Transactional
    public BugReport save(BugReport bugReport) {
        return bugReportJpaRepository.save(bugReport);
    }

    @Transactional
    public void saveBugReportImage(BugReportImage image) {
        bugReportImageJpaRepository.save(image);
    }
}
