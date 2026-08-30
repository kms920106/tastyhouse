package com.tastyhouse.application.review.port.out;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.tastyhouse.domain.review.model.ReviewSortType;
import com.tastyhouse.domain.review.vo.ReviewCommentId;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

/**
 * 리뷰 조회 포트(CQRS query 측 아웃바운드 포트) — 회원 화면용.
 *
 * <p>회원에게 노출되는 리뷰 목록·상세·댓글과 작성 권한 판정을 담당한다. 관리 화면 조회는
 * {@link ReviewManagementQueryPort}가 소유한다.
 *
 * <p>회원 화면과 관리 화면이 같은 형태로 보는 태그 조회는 어느 쪽에도 두지 않고
 * {@link ReviewTagQueryPort}로 떼어 두 앱이 함께 의존한다.
 */
public interface ReviewQueryPort {

    PageResult<BestReviewListItemResult> findBestReviews(PageQuery pageQuery);

    PageResult<LatestReviewListItemResult> findLatestReviews(PageQuery pageQuery);

    PageResult<LatestReviewListItemResult> findLatestReviewsByFollowing(List<Long> followingMemberIds, PageQuery pageQuery);

    PageResult<LatestReviewListItemResult> findLatestReviewsByShopId(Long shopId, Integer rating, PageQuery pageQuery, Boolean hasImage, ReviewSortType sortType);

    PageResult<LatestReviewListItemResult> findLatestReviewsByProductId(Long productId, Integer rating, PageQuery pageQuery, Boolean hasImage, ReviewSortType sortType);

    List<LatestReviewListItemResult> findReviewsByShopIdAndRating(Long shopId, Integer rating, int limit);

    List<LatestReviewListItemResult> findReviewsByProductIdAndRating(Long productId, Integer rating, int limit);

    Optional<ReviewDetailResult> findReviewDetail(ReviewId reviewId, Long viewerMemberId);

    PageResult<MyReviewListItemResult> findMyReviews(Long memberId, PageQuery pageQuery);

    PageResult<MyReviewListItemResult> findReviewsByMemberId(Long memberId, PageQuery pageQuery);

    PageResult<SearchReviewItemResult> searchByKeyword(String keyword, PageQuery pageQuery);

    boolean existsByOrderIdAndProductIdAndMemberId(Long orderId, Long productId, Long memberId);

    Set<Long> findReviewedProductIds(Long orderId, Long memberId, Collection<Long> productIds);

    Optional<Long> findProductIdByReviewId(Long reviewId);

    boolean existsLike(ReviewId reviewId, Long memberId);

    List<ReviewCommentItemResult> findComments(ReviewId reviewId);

    List<ReviewReplyItemResult> findVisibleReplies(List<ReviewCommentId> commentIds);
}
