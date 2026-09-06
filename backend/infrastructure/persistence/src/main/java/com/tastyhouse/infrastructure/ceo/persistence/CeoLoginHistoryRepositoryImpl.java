package com.tastyhouse.infrastructure.ceo.persistence;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.ceo.model.CeoLoginHistory;
import com.tastyhouse.domain.ceo.repository.CeoLoginHistoryRepository;

@Repository
public class CeoLoginHistoryRepositoryImpl implements CeoLoginHistoryRepository {
    private final CeoLoginHistoryJpaRepository ceoLoginHistoryJpaRepository;

    public CeoLoginHistoryRepositoryImpl(CeoLoginHistoryJpaRepository ceoLoginHistoryJpaRepository) {
        this.ceoLoginHistoryJpaRepository = ceoLoginHistoryJpaRepository;
    }

    @Override
    public CeoLoginHistory save(CeoLoginHistory ceoLoginHistory) {
        CeoLoginHistoryJpaEntity saved = ceoLoginHistoryJpaRepository
            .save(CeoLoginHistoryMapper.toEntity(ceoLoginHistory));
        return CeoLoginHistoryMapper.toDomain(saved);
    }
}
