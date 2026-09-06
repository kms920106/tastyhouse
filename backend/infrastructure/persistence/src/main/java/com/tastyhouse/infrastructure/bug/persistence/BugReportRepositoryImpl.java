package com.tastyhouse.infrastructure.bug.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.bug.model.BugReport;
import com.tastyhouse.domain.bug.repository.BugReportRepository;
import com.tastyhouse.domain.bug.vo.BugReportId;

@Repository
public class BugReportRepositoryImpl implements BugReportRepository {
    private final BugReportJpaRepository bugReportJpaRepository;

    public BugReportRepositoryImpl(BugReportJpaRepository bugReportJpaRepository) {
        this.bugReportJpaRepository = bugReportJpaRepository;
    }

    @Override
    public Optional<BugReport> findById(BugReportId bugReportId) {
        if (bugReportId == null) {
            return Optional.empty();
        }
        return bugReportJpaRepository.findById(bugReportId.value())
            .map(BugReportMapper::toDomain);
    }

    @Override
    public BugReport save(BugReport bugReport) {
        if (bugReport.getId() == null) {
            BugReportJpaEntity saved = bugReportJpaRepository.save(BugReportMapper.toEntity(bugReport));
            return BugReportMapper.toDomain(saved);
        }

        BugReportJpaEntity entity = bugReportJpaRepository.findById(bugReport.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 버그 신고입니다: " + bugReport.getId()));
        BugReportMapper.applyChanges(entity, bugReport);
        return BugReportMapper.toDomain(entity);
    }
}
