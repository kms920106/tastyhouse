package com.tastyhouse.application.point.store;

import com.tastyhouse.domain.point.model.PointHistory;

public interface PointHistoryRepository {
    PointHistory save(PointHistory history);
}
