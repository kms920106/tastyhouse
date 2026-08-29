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
 * review 읽기 포트(CQRS query 측 아웃바운드 포트).
 *
 * <p>완전 매핑 전환으로 <b>응용 계층이 읽기 계약을 소유</b>하고 infrastructure-module의
 * {@code ReviewQueryDao}가 이를 구현한다. 소비 모듈은 이 인터페이스와 같은 패키지의 반환 DTO
 * ({@code *Result})·검색 조건({@code *SearchCondition})만 알며, QueryDSL도 어댑터의 존재도 알지 않는다.
 *
 * <p>메서드명·시그니처는 DAO의 기존 공개 표면을 그대로 전사한 것이다(챕터 04는 순수 소유권 이동이라
 * 조회 동작·wire 계약을 바꾸지 않는다).
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

    List<Long> findTagIdsByReviewId(Long reviewId);

    List<String> findTagNamesByIds(List<Long> tagIds);
}
