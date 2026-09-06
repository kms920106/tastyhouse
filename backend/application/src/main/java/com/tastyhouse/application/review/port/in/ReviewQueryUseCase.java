package com.tastyhouse.application.review.port.in;

import com.tastyhouse.application.shared.marker.WebApp;
import java.util.Optional;

import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.review.port.out.BestReviewListItemResult;
import com.tastyhouse.application.review.port.out.LatestReviewListItemResult;
import com.tastyhouse.application.review.port.out.MyReviewListItemResult;
import com.tastyhouse.application.review.port.out.ReviewCommentListView;
import com.tastyhouse.application.review.port.out.ReviewDetailView;
import com.tastyhouse.application.review.port.out.ReviewProductView;
import com.tastyhouse.application.review.port.out.ReviewSubmitResultView;
import com.tastyhouse.application.review.port.out.ReviewWriteInfoView;

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
}
