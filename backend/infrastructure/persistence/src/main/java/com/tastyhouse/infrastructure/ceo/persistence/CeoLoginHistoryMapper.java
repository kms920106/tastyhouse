package com.tastyhouse.infrastructure.ceo.persistence;

import com.tastyhouse.domain.ceo.model.CeoLoginHistory;
import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class CeoLoginHistoryMapper {
    private CeoLoginHistoryMapper() {
    }

    static CeoLoginHistory toDomain(CeoLoginHistoryJpaEntity entity) {
        return CeoLoginHistory.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getCeoId(), CeoId::of),
            entity.getResult(),
            entity.getFailureReason(),
            entity.getIpAddress(),
            entity.getUserAgent(),
            entity.getCreatedAt()
        );
    }

    static CeoLoginHistoryJpaEntity toEntity(CeoLoginHistory domain) {
        return CeoLoginHistoryJpaEntity.create(
            IdMapping.raw(domain.getCeoId(), CeoId::value),
            domain.getResult(),
            domain.getFailureReason(),
            domain.getIpAddress(),
            domain.getUserAgent()
        );
    }
}
