package com.tastyhouse.application.point.port.in;

import com.tastyhouse.application.point.port.out.PointHistoryResult;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface PointManagementHistoryListQueryUseCase {

    PageResult<PointHistoryResult> getPointHistories(Long memberId, String type, int page, int size);
}
