package com.tastyhouse.infrastructure.bug.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.application.bug.port.out.write.BugReportState;
import com.tastyhouse.application.bug.port.out.write.BugReportStatePort;

@Repository
public class BugReportStatePortImpl implements BugReportStatePort {
    private final BugReportJpaRepository bugReportJpaRepository;

    public BugReportStatePortImpl(BugReportJpaRepository bugReportJpaRepository) {
        this.bugReportJpaRepository = bugReportJpaRepository;
    }

    @Override
    public Optional<BugReportState> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return bugReportJpaRepository.findById(id)
            .map(BugReportMapper::toState);
    }

    @Override
    public BugReportState save(BugReportState state) {
        if (state.id() == null) {
            BugReportJpaEntity saved = bugReportJpaRepository.save(BugReportMapper.toEntity(state));
            return BugReportMapper.toState(saved);
        }

        BugReportJpaEntity entity = bugReportJpaRepository.findById(state.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 버그 신고입니다: " + state.id()));
        BugReportMapper.applyChanges(entity, state);
        return BugReportMapper.toState(entity);
    }
}
