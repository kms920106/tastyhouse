package com.tastyhouse.infrastructure.persistence.bug.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.bug.model.BugReport;
import com.tastyhouse.domain.bug.vo.BugReportId;
import com.tastyhouse.application.bug.port.out.write.BugReportLoadPort;
import com.tastyhouse.application.bug.port.out.write.BugReportSavePort;

@Repository
class BugReportPersistenceAdapter implements BugReportLoadPort, BugReportSavePort {

    private final BugReportJpaRepository bugReportJpaRepository;

    public BugReportPersistenceAdapter(BugReportJpaRepository bugReportJpaRepository) {
        this.bugReportJpaRepository = bugReportJpaRepository;
    }

    @Override
    public Optional<BugReport> findById(BugReportId bugReportId) {
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
