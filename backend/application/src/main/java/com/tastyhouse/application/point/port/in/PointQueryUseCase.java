package com.tastyhouse.application.point.port.in;

import com.tastyhouse.application.point.port.out.PointBalanceResult;
import com.tastyhouse.application.point.port.out.PointHistoryViewResult;
import com.tastyhouse.application.shared.marker.WebApp;

@WebApp
public interface PointQueryUseCase {

    PointBalanceResult getMemberPoint(Long memberId);

    PointHistoryViewResult getPointHistory(Long memberId);

    Integer getUsablePoint(Long memberId);
}
