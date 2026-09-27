package com.tastyhouse.application.review.port.out;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface ShopReviewManagementQueryPort {

    PageResult<ShopReviewManagementListItemResult> findShopReviews(ShopReviewManagementSearchCondition condition, PageQuery pageQuery);

    Optional<ShopReviewManagementDetailResult> findShopReviewDetail(ReviewId reviewId);

    List<ReviewBlindRequestHistoryResult> findBlindRequestHistory(ReviewId reviewId);
}
