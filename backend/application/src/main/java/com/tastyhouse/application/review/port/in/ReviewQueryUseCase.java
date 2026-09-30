package com.tastyhouse.application.review.port.in;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import com.tastyhouse.application.review.port.out.BestReviewListItemResult;
import com.tastyhouse.application.review.port.out.LatestReviewListItemResult;
import com.tastyhouse.application.review.port.out.MyReviewListItemResult;
import com.tastyhouse.application.review.port.out.ReviewCommentListView;
import com.tastyhouse.application.review.port.out.ReviewDetailView;
import com.tastyhouse.application.review.port.out.ReviewProductView;
import com.tastyhouse.application.review.port.out.ReviewSubmitResultView;
import com.tastyhouse.application.review.port.out.ReviewWriteInfoView;
import com.tastyhouse.application.review.port.out.ReviewsByRatingResult;
import com.tastyhouse.application.review.port.out.ShopReviewStatisticsResult;
import com.tastyhouse.application.shared.marker.WebApp;
import com.tastyhouse.application.shared.port.out.page.PageResult;

@WebApp
public interface ReviewQueryUseCase {

    PageResult<BestReviewListItemResult> searchBestReviewList(int page, int size);

    PageResult<LatestReviewListItemResult> searchLatestReviewList(int page, int size, String type, Long memberId);

    Optional<ReviewDetailView> findReviewDetail(Long reviewId, Long viewerMemberId);

    ReviewSubmitResultView getReviewSubmitResult(Long reviewId, Long authorMemberId);

    boolean isLiked(Long reviewId, Long memberId);

    ReviewCommentListView searchCommentsWithReplies(Long reviewId, Long viewerMemberId);

    Optional<ReviewProductView> findReviewProduct(Long reviewId, Long viewerMemberId);

    ReviewWriteInfoView getReviewWriteInfo(Long orderProductId, Long memberId);

    PageResult<MyReviewListItemResult> findMemberReviews(Long memberId, int page, int size);

    void requireVisibleReview(Long reviewId, Long viewerMemberId);

    ReviewsByRatingResult findShopReviewsByRating(Long shopId, int page, int size, Boolean hasImage, String sortType);

    ShopReviewStatisticsResult findShopReviewStatistics(Long shopId);

    long countVisibleReviewsByMemberId(Long memberId);

    Set<Long> findReviewedProductIds(Long orderId, Long memberId, Collection<Long> productIds);

    PageResult<MyReviewListItemResult> findMyReviews(Long memberId, int page, int size);
}
