package com.tastyhouse.core.domain.bug.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.bug.domain.model.BugReport;
import com.tastyhouse.core.domain.bug.domain.repository.BugReportRepository;

@Repository
@RequiredArgsConstructor
public class BugReportRepositoryImpl implements BugReportRepository {

    private final BugReportJpaRepository bugReportJpaRepository;

    @Override
    public BugReport save(BugReport bugReport) {
        return bugReportJpaRepository.save(bugReport);
    }
}
