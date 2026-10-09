package com.tastyhouse.infrastructure.jpa.point.persistence;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.point.model.PointHistory;
import com.tastyhouse.application.point.port.out.write.PointHistorySavePort;

@Repository
class PointHistoryPersistenceAdapter implements PointHistorySavePort {

    private final PointHistoryJpaRepository pointHistoryJpaRepository;

    public PointHistoryPersistenceAdapter(PointHistoryJpaRepository pointHistoryJpaRepository) {
        this.pointHistoryJpaRepository = pointHistoryJpaRepository;
    }

    @Override
    public PointHistory save(PointHistory history) {
        PointHistoryJpaEntity saved = pointHistoryJpaRepository.save(PointHistoryMapper.toEntity(history));
        return PointHistoryMapper.toDomain(saved);
    }
}
