package com.tastyhouse.application.review.port.out;

import java.util.Optional;

import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface ReviewBlindRequestManagementQueryPort {

    PageResult<ReviewBlindRequestListItemResult> findBlindRequestPage(ReviewBlindRequestSearchCondition condition, PageQuery pageQuery);

    Optional<ReviewBlindRequestDetailResult> findBlindRequestDetail(Long id);
}
