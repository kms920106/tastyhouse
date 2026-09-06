package com.tastyhouse.application.shop.port.out;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

public interface ShopRequestQueryPort {

    PageResult<ShopRequestListItemResult> findRequestPage(ShopRequestSearchCondition condition, PageQuery pageQuery);

    Optional<ShopRequestDetailResult> findRequestDetail(Long requestId);

    Optional<ShopRequestImageChangeDetailResult> findImageChangeDetail(Long sourceRequestId);

    Optional<ShopRequestAdjustmentDetailResult> findAdjustmentDetail(Long sourceRequestId);

    Optional<ShopRequestReviewBlindDetailResult> findReviewBlindDetail(Long sourceRequestId);

    List<ShopRequestCommentResult> findComments(Long requestId);
}
