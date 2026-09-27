package com.tastyhouse.infrastructure.bug.persistence;

import org.springframework.stereotype.Repository;

import com.tastyhouse.application.bug.port.out.write.BugReportImageState;
import com.tastyhouse.application.bug.port.out.write.BugReportImageStatePort;

@Repository
public class BugReportImageStatePortImpl implements BugReportImageStatePort {
    private final BugReportImageJpaRepository bugReportImageJpaRepository;

    public BugReportImageStatePortImpl(BugReportImageJpaRepository bugReportImageJpaRepository) {
        this.bugReportImageJpaRepository = bugReportImageJpaRepository;
    }

    @Override
    public BugReportImageState save(BugReportImageState state) {
        BugReportImageJpaEntity saved = bugReportImageJpaRepository.save(BugReportImageMapper.toEntity(state));
        return BugReportImageMapper.toState(saved);
    }
}
