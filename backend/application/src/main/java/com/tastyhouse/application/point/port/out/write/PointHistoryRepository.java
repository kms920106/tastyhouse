package com.tastyhouse.application.point.port.out.write;

import com.tastyhouse.domain.point.model.PointHistory;

public interface PointHistoryRepository {
    PointHistory save(PointHistory history);
}
