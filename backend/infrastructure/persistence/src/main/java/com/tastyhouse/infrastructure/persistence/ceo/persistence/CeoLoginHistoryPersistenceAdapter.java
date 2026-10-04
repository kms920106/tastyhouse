package com.tastyhouse.infrastructure.persistence.ceo.persistence;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.ceo.model.CeoLoginHistory;
import com.tastyhouse.application.ceo.port.out.write.CeoLoginHistoryPersistencePort;

@Repository
class CeoLoginHistoryPersistenceAdapter implements CeoLoginHistoryPersistencePort {

    private final CeoLoginHistoryJpaRepository ceoLoginHistoryJpaRepository;

    public CeoLoginHistoryPersistenceAdapter(CeoLoginHistoryJpaRepository ceoLoginHistoryJpaRepository) {
        this.ceoLoginHistoryJpaRepository = ceoLoginHistoryJpaRepository;
    }

    @Override
    public CeoLoginHistory save(CeoLoginHistory ceoLoginHistory) {
        CeoLoginHistoryJpaEntity saved = ceoLoginHistoryJpaRepository
            .save(CeoLoginHistoryMapper.toEntity(ceoLoginHistory));
        return CeoLoginHistoryMapper.toDomain(saved);
    }
}
