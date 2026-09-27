package com.tastyhouse.application.point.store;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.point.model.Point;
import com.tastyhouse.application.point.port.out.write.PointState;

final class PointStateMapper {
    private PointStateMapper() {
    }

    static Point toDomain(PointState state) {
        return Point.reconstitute(
            state.id(),
            state.memberId() == null ? null : MemberId.of(state.memberId()),
            state.availablePoints(),
            state.expiredThisMonth()
        );
    }

    static PointState toState(Point point) {
        return new PointState(
            point.getId(),
            point.getMemberId() == null ? null : point.getMemberId().value(),
            point.getAvailablePoints(),
            point.getExpiredThisMonth()
        );
    }
}
