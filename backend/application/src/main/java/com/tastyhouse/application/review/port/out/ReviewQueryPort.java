package com.tastyhouse.application.review.port.out;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface ReviewQueryPort {

    PageResult<BestReviewListItemResult> findBestReviews(PageQuery pageQuery);

    PageResult<LatestReviewListItemResult> findLatestReviews(PageQuery pageQuery);

    PageResult<LatestReviewListItemResult> findLatestReviewsByFollowing(List<Long> followingMemberIds, PageQuery pageQuery);

    PageResult<LatestReviewListItemResult> findLatestReviewsByShopId(Long shopId, Integer rating, PageQuery pageQuery, Boolean hasImage, ReviewSortSpec sort);

    PageResult<LatestReviewListItemResult> findLatestReviewsByProductId(Long productId, Integer rating, PageQuery pageQuery, Boolean hasImage, ReviewSortSpec sort);

    List<LatestReviewListItemResult> findReviewsByShopIdAndRating(Long shopId, Integer rating, int limit);

    List<LatestReviewListItemResult> findReviewsByProductIdAndRating(Long productId, Integer rating, int limit);

    Optional<ReviewDetailResult> findReviewDetail(Long reviewId, Long viewerMemberId);

    PageResult<MyReviewListItemResult> findMyReviews(Long memberId, PageQuery pageQuery);

    PageResult<MyReviewListItemResult> findReviewsByMemberId(Long memberId, PageQuery pageQuery);

    PageResult<SearchReviewItemResult> searchByKeyword(String keyword, PageQuery pageQuery);

    boolean existsByOrderIdAndProductIdAndMemberId(Long orderId, Long productId, Long memberId);

    Set<Long> findReviewedProductIds(Long orderId, Long memberId, Collection<Long> productIds);

    Optional<Long> findProductIdByReviewId(Long reviewId);

    boolean existsLike(Long reviewId, Long memberId);

    List<ReviewCommentItemResult> findComments(Long reviewId);

    List<ReviewReplyItemResult> findVisibleReplies(List<Long> commentIds);
}
