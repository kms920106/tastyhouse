package com.tastyhouse.application.point.port.in;

import com.tastyhouse.application.shared.marker.AdminApp;
import java.util.Optional;

import com.tastyhouse.application.point.port.out.PointBalanceResult;
import com.tastyhouse.application.point.port.out.PointHistoryResult;
import com.tastyhouse.domain.shared.page.PageResult;

@AdminApp
public interface PointManagementQueryUseCase {

    Optional<PointBalanceResult> getPointBalance(Long memberId);

    PageResult<PointHistoryResult> getPointHistories(Long memberId, String type, int page, int size);
}
