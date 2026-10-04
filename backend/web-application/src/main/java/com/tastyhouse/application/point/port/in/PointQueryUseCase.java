package com.tastyhouse.application.point.port.in;

import com.tastyhouse.application.point.port.out.PointBalanceResult;
import com.tastyhouse.application.point.port.out.PointHistoryViewResult;

public interface PointQueryUseCase {

    PointBalanceResult getMemberPoint(Long memberId);

    PointHistoryViewResult getPointHistory(Long memberId);

    Integer getUsablePoint(Long memberId);
}
