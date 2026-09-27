package com.tastyhouse.application.point.port.out;

import java.util.Optional;

import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface PointManagementQueryPort {

    Optional<PointBalanceResult> findBalanceByMemberId(Long memberId);

    PageResult<PointHistoryResult> findPointHistoryPage(PointSearchCondition condition, PageQuery pageQuery);
}
