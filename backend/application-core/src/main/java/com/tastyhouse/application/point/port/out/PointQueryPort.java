package com.tastyhouse.application.point.port.out;

import java.util.List;
import java.util.Optional;

public interface PointQueryPort {

    Optional<PointBalanceResult> findBalanceByMemberId(Long memberId);

    List<PointHistoryResult> findPointHistories(Long memberId);
}
