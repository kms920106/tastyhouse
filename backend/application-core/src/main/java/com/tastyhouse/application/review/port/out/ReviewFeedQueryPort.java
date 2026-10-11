package com.tastyhouse.application.review.port.out;

import java.util.List;

import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface ReviewFeedQueryPort {

    PageResult<BestReviewListItemResult> findBestReviews(PageQuery pageQuery);

    PageResult<LatestReviewListItemResult> findLatestReviews(PageQuery pageQuery);

    PageResult<LatestReviewListItemResult> findLatestReviewsByFollowing(List<Long> followingMemberIds, PageQuery pageQuery);

    PageResult<LatestReviewListItemResult> findLatestReviewsByShopId(Long shopId, Integer rating, PageQuery pageQuery, Boolean hasImage, ReviewSortSpec sort);

    PageResult<LatestReviewListItemResult> findLatestReviewsByProductId(Long productId, Integer rating, PageQuery pageQuery, Boolean hasImage, ReviewSortSpec sort);

    List<LatestReviewListItemResult> findReviewsByShopIdAndRating(Long shopId, Integer rating, int limit);

    List<LatestReviewListItemResult> findReviewsByProductIdAndRating(Long productId, Integer rating, int limit);
}
