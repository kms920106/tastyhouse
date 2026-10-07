package com.tastyhouse.application.point.port.in;

import java.util.Optional;

import com.tastyhouse.application.point.port.out.PointBalanceResult;

public interface PointManagementBalanceQueryUseCase {

    Optional<PointBalanceResult> getPointBalance(Long memberId);
}
