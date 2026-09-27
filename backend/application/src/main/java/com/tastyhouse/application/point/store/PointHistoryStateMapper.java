package com.tastyhouse.application.point.store;

import com.tastyhouse.application.point.port.out.write.PointHistoryState;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.point.model.PointHistory;
import com.tastyhouse.domain.point.model.PointType;

final class PointHistoryStateMapper {
    private PointHistoryStateMapper() {
    }

    static PointHistory toDomain(PointHistoryState state) {
        return PointHistory.reconstitute(
            state.id(),
            state.memberId() == null ? null : MemberId.of(state.memberId()),
            state.pointType() == null ? null : PointType.valueOf(state.pointType()),
            state.pointAmount(),
            state.reason(),
            state.createdAt()
        );
    }

    static PointHistoryState toState(PointHistory history) {
        return new PointHistoryState(
            history.getId(),
            history.getMemberId() == null ? null : history.getMemberId().value(),
            history.getPointType() == null ? null : history.getPointType().name(),
            history.getPointAmount(),
            history.getReason(),
            history.getCreatedAt()
        );
    }
}
