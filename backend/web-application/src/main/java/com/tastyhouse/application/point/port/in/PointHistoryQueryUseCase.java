package com.tastyhouse.application.point.port.in;

import com.tastyhouse.application.point.port.out.PointHistoryViewResult;

public interface PointHistoryQueryUseCase {

    PointHistoryViewResult getPointHistory(Long memberId);
}
