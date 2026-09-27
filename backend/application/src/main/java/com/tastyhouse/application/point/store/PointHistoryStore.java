package com.tastyhouse.application.point.store;

import com.tastyhouse.application.point.port.out.write.PointHistoryStatePort;
import com.tastyhouse.domain.point.model.PointHistory;

public class PointHistoryStore implements PointHistoryRepository {
    private final PointHistoryStatePort pointHistoryStatePort;

    public PointHistoryStore(PointHistoryStatePort pointHistoryStatePort) {
        this.pointHistoryStatePort = pointHistoryStatePort;
    }

    @Override
    public PointHistory save(PointHistory history) {
        return PointHistoryStateMapper.toDomain(pointHistoryStatePort.save(PointHistoryStateMapper.toState(history)));
    }
}
