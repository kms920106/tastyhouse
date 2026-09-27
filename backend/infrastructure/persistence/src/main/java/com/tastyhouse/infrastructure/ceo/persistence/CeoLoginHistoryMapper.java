package com.tastyhouse.infrastructure.ceo.persistence;

import com.tastyhouse.application.ceo.port.out.write.CeoLoginHistoryState;

final class CeoLoginHistoryMapper {
    private CeoLoginHistoryMapper() {
    }

    static CeoLoginHistoryState toState(CeoLoginHistoryJpaEntity entity) {
        return new CeoLoginHistoryState(
            entity.getId(),
            entity.getCeoId(),
            entity.getResult(),
            entity.getFailureReason(),
            entity.getIpAddress(),
            entity.getUserAgent(),
            entity.getCreatedAt()
        );
    }

    static CeoLoginHistoryJpaEntity toEntity(CeoLoginHistoryState state) {
        return CeoLoginHistoryJpaEntity.create(
            state.ceoId(),
            state.result(),
            state.failureReason(),
            state.ipAddress(),
            state.userAgent()
        );
    }
}
