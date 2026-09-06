package com.tastyhouse.infrastructure.point.persistence;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.point.model.PointHistory;
import com.tastyhouse.domain.point.repository.PointHistoryRepository;

@Repository
public class PointHistoryRepositoryImpl implements PointHistoryRepository {
    private final PointHistoryJpaRepository pointHistoryJpaRepository;

    public PointHistoryRepositoryImpl(PointHistoryJpaRepository pointHistoryJpaRepository) {
        this.pointHistoryJpaRepository = pointHistoryJpaRepository;
    }

    @Override
    public PointHistory save(PointHistory history) {
        PointHistoryJpaEntity saved = pointHistoryJpaRepository.save(PointHistoryMapper.toEntity(history));
        return PointHistoryMapper.toDomain(saved);
    }
}
