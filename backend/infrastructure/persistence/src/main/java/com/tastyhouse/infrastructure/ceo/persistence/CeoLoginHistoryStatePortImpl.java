package com.tastyhouse.infrastructure.ceo.persistence;

import org.springframework.stereotype.Repository;

import com.tastyhouse.application.ceo.port.out.write.CeoLoginHistoryState;
import com.tastyhouse.application.ceo.port.out.write.CeoLoginHistoryStatePort;

@Repository
public class CeoLoginHistoryStatePortImpl implements CeoLoginHistoryStatePort {
    private final CeoLoginHistoryJpaRepository ceoLoginHistoryJpaRepository;

    public CeoLoginHistoryStatePortImpl(CeoLoginHistoryJpaRepository ceoLoginHistoryJpaRepository) {
        this.ceoLoginHistoryJpaRepository = ceoLoginHistoryJpaRepository;
    }

    @Override
    public CeoLoginHistoryState save(CeoLoginHistoryState state) {
        CeoLoginHistoryJpaEntity saved = ceoLoginHistoryJpaRepository
            .save(CeoLoginHistoryMapper.toEntity(state));
        return CeoLoginHistoryMapper.toState(saved);
    }
}
