package com.tastyhouse.application.point.port.out.write;

import com.tastyhouse.domain.point.model.PointHistory;

public interface PointHistoryPersistencePort {
    PointHistory save(PointHistory history);
}
