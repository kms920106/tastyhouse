package com.tastyhouse.infrastructure.point.persistence;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.point.model.PointHistory;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class PointHistoryMapper {
    private PointHistoryMapper() {
    }

    static PointHistory toDomain(PointHistoryJpaEntity entity) {
        return PointHistory.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getMemberId(), MemberId::of),
            entity.getPointType(),
            entity.getPointAmount(),
            entity.getReason(),
            entity.getCreatedAt()
        );
    }

    static PointHistoryJpaEntity toEntity(PointHistory domain) {
        return PointHistoryJpaEntity.create(
            IdMapping.raw(domain.getMemberId(), MemberId::value),
            domain.getPointType(),
            domain.getPointAmount(),
            domain.getReason()
        );
    }
}
