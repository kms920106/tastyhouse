package com.tastyhouse.infrastructure.ceo.persistence;

import com.tastyhouse.domain.ceo.model.CeoLoginFailureReason;
import com.tastyhouse.domain.ceo.model.CeoLoginHistory;
import com.tastyhouse.domain.ceo.model.CeoLoginResult;
import com.tastyhouse.domain.ceo.vo.CeoId;

final class CeoLoginHistoryMapper {
    private CeoLoginHistoryMapper() {
    }

    static CeoLoginHistory toDomain(CeoLoginHistoryJpaEntity entity) {
        return CeoLoginHistory.reconstitute(
            entity.getId(),
            entity.getCeoId() == null ? null : CeoId.of(entity.getCeoId()),
            entity.getResult() == null ? null : CeoLoginResult.valueOf(entity.getResult()),
            entity.getFailureReason() == null ? null : CeoLoginFailureReason.valueOf(entity.getFailureReason()),
            entity.getIpAddress(),
            entity.getUserAgent(),
            entity.getCreatedAt()
        );
    }

    static CeoLoginHistoryJpaEntity toEntity(CeoLoginHistory history) {
        return CeoLoginHistoryJpaEntity.create(
            history.getCeoId() == null ? null : history.getCeoId().value(),
            history.getResult() == null ? null : history.getResult().name(),
            history.getFailureReason() == null ? null : history.getFailureReason().name(),
            history.getIpAddress(),
            history.getUserAgent()
        );
    }
}
