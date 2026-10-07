package com.tastyhouse.application.point.port.in;

import com.tastyhouse.application.point.port.out.PointBalanceResult;

public interface PointBalanceQueryUseCase {

    PointBalanceResult getMemberPoint(Long memberId);
}
