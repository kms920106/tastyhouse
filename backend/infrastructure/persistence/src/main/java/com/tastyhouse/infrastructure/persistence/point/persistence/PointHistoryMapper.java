package com.tastyhouse.infrastructure.persistence.point.persistence;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.point.model.PointHistory;
import com.tastyhouse.domain.point.model.PointType;

final class PointHistoryMapper {

    private PointHistoryMapper() {
    }

    static PointHistory toDomain(PointHistoryJpaEntity entity) {
        return PointHistory.reconstitute(
            entity.getId(),
            entity.getMemberId() == null ? null : MemberId.of(entity.getMemberId()),
            entity.getPointType() == null ? null : PointType.valueOf(entity.getPointType()),
            entity.getPointAmount(),
            entity.getReason(),
            entity.getCreatedAt()
        );
    }

    static PointHistoryJpaEntity toEntity(PointHistory history) {
        return PointHistoryJpaEntity.create(
            history.getMemberId() == null ? null : history.getMemberId().value(),
            history.getPointType() == null ? null : history.getPointType().name(),
            history.getPointAmount(),
            history.getReason()
        );
    }
}
