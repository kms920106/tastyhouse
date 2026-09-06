package com.tastyhouse.application.point.port.out;

import java.util.List;

public record PointHistoryViewResult(
    Integer availablePoints,
    Integer expiredThisMonth,
    List<PointHistoryItemViewResult> histories
) {
}
