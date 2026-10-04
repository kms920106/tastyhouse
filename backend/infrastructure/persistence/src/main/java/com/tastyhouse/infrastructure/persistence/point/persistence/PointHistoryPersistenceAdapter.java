package com.tastyhouse.infrastructure.persistence.point.persistence;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.point.model.PointHistory;
import com.tastyhouse.application.point.port.out.write.PointHistoryPersistencePort;

@Repository
public class PointHistoryPersistenceAdapter implements PointHistoryPersistencePort {

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
