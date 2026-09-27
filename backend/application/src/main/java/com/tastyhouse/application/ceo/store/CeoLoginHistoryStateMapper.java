package com.tastyhouse.application.ceo.store;

import com.tastyhouse.application.ceo.port.out.write.CeoLoginHistoryState;
import com.tastyhouse.domain.ceo.model.CeoLoginFailureReason;
import com.tastyhouse.domain.ceo.model.CeoLoginHistory;
import com.tastyhouse.domain.ceo.model.CeoLoginResult;
import com.tastyhouse.domain.ceo.vo.CeoId;

final class CeoLoginHistoryStateMapper {
    private CeoLoginHistoryStateMapper() {
    }

    static CeoLoginHistory toDomain(CeoLoginHistoryState state) {
        return CeoLoginHistory.reconstitute(
            state.id(),
            state.ceoId() == null ? null : CeoId.of(state.ceoId()),
            state.result() == null ? null : CeoLoginResult.valueOf(state.result()),
            state.failureReason() == null ? null : CeoLoginFailureReason.valueOf(state.failureReason()),
            state.ipAddress(),
            state.userAgent(),
            state.createdAt()
        );
    }

    static CeoLoginHistoryState toState(CeoLoginHistory history) {
        return new CeoLoginHistoryState(
            history.getId(),
            history.getCeoId() == null ? null : history.getCeoId().value(),
            history.getResult() == null ? null : history.getResult().name(),
            history.getFailureReason() == null ? null : history.getFailureReason().name(),
            history.getIpAddress(),
            history.getUserAgent(),
            history.getCreatedAt()
        );
    }
}
