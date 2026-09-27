package com.tastyhouse.infrastructure.point.persistence;

import com.tastyhouse.application.point.port.out.write.PointHistoryState;

final class PointHistoryMapper {
    private PointHistoryMapper() {
    }

    static PointHistoryState toState(PointHistoryJpaEntity entity) {
        return new PointHistoryState(
            entity.getId(),
            entity.getMemberId(),
            entity.getPointType(),
            entity.getPointAmount(),
            entity.getReason(),
            entity.getCreatedAt()
        );
    }

    static PointHistoryJpaEntity toEntity(PointHistoryState state) {
        return PointHistoryJpaEntity.create(
            state.memberId(),
            state.pointType(),
            state.pointAmount(),
            state.reason()
        );
    }
}
