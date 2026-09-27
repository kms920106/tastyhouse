package com.tastyhouse.infrastructure.point.persistence;

import org.springframework.stereotype.Repository;

import com.tastyhouse.application.point.port.out.write.PointHistoryState;
import com.tastyhouse.application.point.port.out.write.PointHistoryStatePort;

@Repository
public class PointHistoryStatePortImpl implements PointHistoryStatePort {
    private final PointHistoryJpaRepository pointHistoryJpaRepository;

    public PointHistoryStatePortImpl(PointHistoryJpaRepository pointHistoryJpaRepository) {
        this.pointHistoryJpaRepository = pointHistoryJpaRepository;
    }

    @Override
    public PointHistoryState save(PointHistoryState state) {
        PointHistoryJpaEntity saved = pointHistoryJpaRepository.save(PointHistoryMapper.toEntity(state));
        return PointHistoryMapper.toState(saved);
    }
}
