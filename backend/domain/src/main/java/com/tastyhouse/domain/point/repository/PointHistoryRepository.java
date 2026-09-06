package com.tastyhouse.domain.point.repository;

import com.tastyhouse.domain.point.model.PointHistory;

public interface PointHistoryRepository {
    PointHistory save(PointHistory history);
}
