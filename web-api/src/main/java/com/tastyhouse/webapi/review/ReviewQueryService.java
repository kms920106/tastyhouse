package com.tastyhouse.webapi.review;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.domain.vo.MemberId;
import com.tastyhouse.domain.member.follow.domain.repository.MemberFollowRepository;
import com.tastyhouse.domain.order.domain.model.OrderProduct;
import com.tastyhouse.domain.order.domain.repository.OrderProductRepository;
import com.tastyhouse.domain.order.domain.vo.OrderProductId;
import com.tastyhouse.domain.review.domain.model.ReviewSortType;
import com.tastyhouse.domain.review.domain.vo.ReviewCommentId;
import com.tastyhouse.domain.review.domain.vo.ReviewId;
import com.tastyhouse.domain.exception.EntityNotFoundException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.infrastructure.product.query.ProductDetailResult;
import com.tastyhouse.infrastructure.product.query.ProductQueryDao;
import com.tastyhouse.infrastructure.review.query.BestReviewListItemResult;
import com.tastyhouse.infrastructure.review.query.LatestReviewListItemResult;
import com.tastyhouse.infrastructure.review.query.MyReviewListItemResult;
import com.tastyhouse.infrastructure.review.query.ReviewCommentItemResult;
import com.tastyhouse.infrastructure.review.query.ReviewDetailResult;
import com.tastyhouse.infrastructure.review.query.ReviewQueryDao;
import com.tastyhouse.infrastructure.review.query.ReviewReplyItemResult;
import com.tastyhouse.infrastructure.review.query.ReviewStatisticsQueryDao;
import com.tastyhouse.infrastructure.review.query.ReviewsByRatingResult;
import com.tastyhouse.infrastructure.review.query.ShopReviewStatisticsResult;
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.webapi.review.response.ReviewBestListItemResponse;
import com.tastyhouse.webapi.review.response.ReviewCommentListResponse;
import com.tastyhouse.webapi.review.response.ReviewCommentResponse;
import com.tastyhouse.webapi.review.response.ReviewDetailResponse;
import com.tastyhouse.webapi.review.response.ReviewLatestListItemResponse;
import com.tastyhouse.webapi.review.response.ReviewLikeStatusResponse;
import com.tastyhouse.webapi.review.response.ReviewMemberListItemResponse;
import com.tastyhouse.webapi.review.response.ReviewProductResponse;
import com.tastyhouse.webapi.review.response.ReviewReplyResponse;
import com.tastyhouse.webapi.review.response.ReviewResponse;
import com.tastyhouse.webapi.review.response.ReviewWriteInfoResponse;

/**
 * 리뷰 조회 서비스(web).
 *
 * <p>infra read 어댑터({@link ReviewQueryDao}·{@link ReviewStatisticsQueryDao})만 주입해 조회하고
 * Response를 조립한다(private 매퍼).
 * 파일 경로 → 표시용 URL 변환은 DAO({@code FileUrlResolver})가 담당하므로 이 계층은 이미 URL이 된
 * 필드를 그대로 조립한다(응답에 파일 식별자·경로를 노출하지 않는다는 규칙).
 *
 * <p>과거 core 조회 서비스가 여러 조회를 조합해 만들던 값(리뷰 상세의 태그명)도 이 계층이 조합한다 —
 * DAO는 단일 조회 단위만 제공한다.
 *
 * <p>리뷰 화면이 곁들여 보여주는 상품 정보(상품명·가격·대표 이미지)는 다른 도메인의 QueryService를 경유하지
 * 않고 {@link ProductQueryDao}를 직접 주입해 조회한다 — 서비스를 경유하면 상품 쪽이 리뷰 통계를 얻기 위해
 * 이 서비스를 다시 주입해야 해서 빈 순환 참조가 생긴다. 표현 목적 조회는 DAO 계층에서 교차하는 것이 옳다.
 *
 * <p>명령 동작은 {@link ReviewCommandService}로 분리했다(CQRS).
 */
@Service
@Transactional(readOnly = true)
public class ReviewQueryService {

    private final ReviewQueryDao reviewQueryDao;
    private final ReviewStatisticsQueryDao reviewStatisticsQueryDao;
    private final MemberFollowRepository memberFollowRepository;
    private final ProductQueryDao productQueryDao;
    private final OrderProductRepository orderProductRepository;

    public ReviewQueryService(
        ReviewQueryDao reviewQueryDao,
        ReviewStatisticsQueryDao reviewStatisticsQueryDao,
        MemberFollowRepository memberFollowRepository,
        ProductQueryDao productQueryDao,
        OrderProductRepository orderProductRepository
    ) {
        this.reviewQueryDao = reviewQueryDao;
        this.reviewStatisticsQueryDao = reviewStatisticsQueryDao;
        this.memberFollowRepository = memberFollowRepository;
        this.productQueryDao = productQueryDao;
        this.orderProductRepository = orderProductRepository;
    }

    /**
     * 베스트 리뷰 목록.
     */
    public PaginationResponse<ReviewBestListItemResponse> searchBestReviewList(int page, int size) {
        PageResult<ReviewBestListItemResponse> pageResult = reviewQueryDao.findBestReviews(PageQuery.of(page, size))
            .map(this::toBestReviewListItemResponse);
        return PaginationResponse.from(pageResult);
    }

    /**
     * 최신 리뷰 목록 — FOLLOWING이면 로그인 회원이 팔로우한 회원들의 리뷰만 조회한다.
     */
    public PaginationResponse<ReviewLatestListItemResponse> searchLatestReviewList(
        int page,
        int size,
        String type,
        Long memberId
    ) {
        PageResult<ReviewLatestListItemResponse> pageResult;
        if (ReviewListType.from(type) == ReviewListType.FOLLOWING && memberId != null) {
            pageResult = findLatestReviewsByFollowing(MemberId.of(memberId), page, size)
                .map(this::toLatestReviewListItemResponse);
        } else {
            pageResult = reviewQueryDao.findLatestReviews(PageQuery.of(page, size))
                .map(this::toLatestReviewListItemResponse);
        }
        return PaginationResponse.from(pageResult);
    }

    /**
     * 리뷰 상세 — 상세 본문 조회와 태그명 조회를 조합한다. 태그가 없으면 조합하지 않는다.
     */
    public Optional<ReviewDetailResponse> findReviewDetail(Long reviewId) {
        return findReviewDetailResult(ReviewId.of(reviewId))
            .map(this::toReviewDetailResponse);
    }

    /**
     * 리뷰 등록·수정 응답 — 명령이 돌려준 식별자로 커밋 이후 재조회해 조립한다.
     *
     * <p>{@link ReviewCommandService}가 식별자만 반환하므로(CQRS 교차 주입 금지) 등록·수정 API의 응답은
     * 이 메서드가 만든다. 응답 계약을 바꾸지 않기 위해 {@code ReviewResponse}의 필드 구성은 그대로 두고,
     * 값의 출처만 "명령이 들고 있던 도메인 모델"에서 "커밋된 행의 투영"으로 옮겼다 — 리뷰 상세 투영이
     * 숨김 리뷰를 제외하므로, 방금 등록된(숨김이 아닌) 리뷰는 항상 조회된다.
     */
    public ReviewResponse getReviewResponse(Long reviewId) {
        ReviewDetailResult detail = findReviewDetailResult(ReviewId.of(reviewId))
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.REVIEW_NOT_FOUND));

        return ReviewResponse.from(
            detail.id(),
            findProductIdOfReview(reviewId),
            detail.tasteRating(),
            detail.amountRating(),
            detail.priceRating(),
            detail.totalRating(),
            detail.content(),
            detail.imageUrls(),
            detail.tagNames(),
            detail.createdAt()
        );
    }

    /**
     * 리뷰 좋아요 여부.
     */
    public ReviewLikeStatusResponse isLiked(Long reviewId, Long memberId) {
        boolean liked = reviewQueryDao.existsLike(ReviewId.of(reviewId), MemberId.of(memberId));
        return ReviewLikeStatusResponse.from(liked);
    }

    /**
     * 리뷰의 댓글·답글 목록. 댓글은 숨김 포함, 답글은 숨김 제외다(기존 동작 유지).
     */
    public ReviewCommentListResponse searchCommentsWithReplies(Long reviewId) {
        List<ReviewCommentItemResult> comments = reviewQueryDao.findComments(ReviewId.of(reviewId));

        if (comments.isEmpty()) {
            return ReviewCommentListResponse.from(List.of(), 0);
        }

        List<ReviewCommentId> commentIds = comments.stream()
            .map(comment -> ReviewCommentId.of(comment.id()))
            .toList();

        List<ReviewReplyItemResult> allReplies = reviewQueryDao.findVisibleReplies(commentIds);

        Map<Long, List<ReviewReplyItemResult>> repliesByCommentId = allReplies.stream()
            .collect(Collectors.groupingBy(ReviewReplyItemResult::commentId));

        List<ReviewCommentResponse> commentResponses = comments.stream()
            .map(comment -> {
                List<ReviewReplyResponse> replyResponses = repliesByCommentId.getOrDefault(comment.id(), List.of()).stream()
                    .map(this::toReplyResponse)
                    .toList();
                return toCommentResponse(comment, replyResponses);
            })
            .toList();

        int totalCount = comments.size() + allReplies.size();
        return ReviewCommentListResponse.from(commentResponses, totalCount);
    }

    /**
     * 리뷰 상세 + 연결 상품 정보. 상품이 없어도 리뷰 정보만으로 응답한다(상품 필드는 비운다).
     */
    public Optional<ReviewProductResponse> findReviewProduct(Long reviewId) {
        Optional<ReviewDetailResult> reviewDetailOpt = findReviewDetailResult(ReviewId.of(reviewId));
        if (reviewDetailOpt.isEmpty()) {
            return Optional.empty();
        }

        ReviewDetailResult reviewDetail = reviewDetailOpt.get();

        List<String> reviewImageUrls = reviewDetail.imageUrls();
        String reviewMemberProfileImageUrl = reviewDetail.memberProfileImageUrl();

        return productQueryDao.findProductDetailById(findProductIdOfReview(reviewId))
            .map(product -> {
                Integer price = product.discountPrice() != null
                    ? product.discountPrice()
                    : product.originalPrice();

                return ReviewProductResponse.from(
                    product.id(),
                    product.name(),
                    getFirstImageUrl(product.id()),
                    price,
                    reviewDetail.id(),
                    reviewDetail.content(),
                    reviewDetail.totalRating(),
                    reviewDetail.tasteRating(),
                    reviewDetail.amountRating(),
                    reviewDetail.priceRating(),
                    reviewDetail.atmosphereRating(),
                    reviewDetail.kindnessRating(),
                    reviewDetail.hygieneRating(),
                    reviewDetail.willRevisit(),
                    reviewDetail.memberId().value(),
                    reviewDetail.memberNickname(),
                    reviewMemberProfileImageUrl,
                    reviewDetail.createdAt(),
                    reviewImageUrls,
                    reviewDetail.tagNames()
                );
            })
            .or(() -> Optional.of(
                ReviewProductResponse.from(
                    null, null, null, null,
                    reviewDetail.id(),
                    reviewDetail.content(),
                    reviewDetail.totalRating(),
                    reviewDetail.tasteRating(),
                    reviewDetail.amountRating(),
                    reviewDetail.priceRating(),
                    reviewDetail.atmosphereRating(),
                    reviewDetail.kindnessRating(),
                    reviewDetail.hygieneRating(),
                    reviewDetail.willRevisit(),
                    reviewDetail.memberId().value(),
                    reviewDetail.memberNickname(),
                    reviewMemberProfileImageUrl,
                    reviewDetail.createdAt(),
                    reviewImageUrls,
                    reviewDetail.tagNames()
                )
            ));
    }

    /**
     * 리뷰 작성 화면 정보 — 주문 상품에서 상품을 찾아 가격·대표 이미지와 작성 이력 여부를 함께 준다.
     */
    public ReviewWriteInfoResponse getReviewWriteInfo(Long orderProductId, Long memberId) {
        OrderProduct orderProduct = orderProductRepository.findById(OrderProductId.of(orderProductId))
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.REVIEW_ORDER_PRODUCT_NOT_FOUND));

        ProductDetailResult product = productQueryDao.findProductDetailById(orderProduct.getProductId().value())
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ORDER_PRODUCT_NOT_FOUND));

        Integer price = product.discountPrice() != null
            ? product.discountPrice()
            : product.originalPrice();

        boolean reviewed = reviewQueryDao.existsByOrderIdAndProductIdAndMemberId(
            orderProduct.getOrderId().value(), orderProduct.getProductId().value(), MemberId.of(memberId)
        );

        return ReviewWriteInfoResponse.from(
            product.id(),
            product.name(),
            getFirstImageUrl(product.id()),
            price,
            orderProduct.getOrderId().value(),
            reviewed
        );
    }

    /**
     * 특정 회원이 쓴 리뷰 목록(대표 이미지 1장).
     */
    public PaginationResponse<ReviewMemberListItemResponse> findMemberReviews(Long memberId, int page, int size) {
        PageResult<ReviewMemberListItemResponse> pageResult =
            reviewQueryDao.findReviewsByMemberId(MemberId.of(memberId), PageQuery.of(page, size))
                .map(dto -> ReviewMemberListItemResponse.from(
                    dto.id(),
                    dto.imageUrl()
                ));
        return PaginationResponse.from(pageResult);
    }

    /**
     * 가게의 평점대별 리뷰 묶음 — 1~5점 각 5건과 전체 페이지, 전체 건수를 조합한다.
     */
    public ReviewsByRatingResult findShopReviewsByRating(Long shopId, int page, int size, Boolean hasImage) {
        Map<Integer, List<LatestReviewListItemResult>> reviewsByRating = new HashMap<>();
        for (int rating = 1; rating <= 5; rating++) {
            reviewsByRating.put(rating, reviewQueryDao.findReviewsByShopIdAndRating(shopId, rating, 5));
        }

        PageQuery pageQuery = PageQuery.of(page, size);
        PageResult<LatestReviewListItemResult> allReviewsPage =
            reviewQueryDao.findLatestReviewsByShopId(shopId, null, pageQuery, hasImage, ReviewSortType.LATEST);

        Long totalReviewCount = reviewStatisticsQueryDao.countByShopIdAndHiddenFalse(shopId);

        return new ReviewsByRatingResult(
            reviewsByRating,
            allReviewsPage.content(),
            totalReviewCount,
            allReviewsPage.totalElements(),
            allReviewsPage.totalPages(),
            allReviewsPage.page(),
            allReviewsPage.size()
        );
    }


    /**
     * 가게 리뷰 통계 — 리뷰가 하나도 없으면 평균·재방문율·월별 집계를 계산하지 않고 비운다.
     */
    public ShopReviewStatisticsResult findShopReviewStatistics(Long shopId) {
        Long totalCount = reviewStatisticsQueryDao.countByShopIdAndHiddenFalse(shopId);

        Map<Integer, Long> ratingMap = reviewStatisticsQueryDao.getRatingCounts(shopId);
        for (int rating = 1; rating <= 5; rating++) {
            ratingMap.putIfAbsent(rating, 0L);
        }

        if (totalCount > 0) {
            Long willRevisitCount = reviewStatisticsQueryDao.countWillRevisit(shopId);
            double willRevisitPercentage = (willRevisitCount * 100.0) / totalCount;

            int currentYear = LocalDateTime.now().getYear();
            Map<Integer, Long> monthlyMap = reviewStatisticsQueryDao.getMonthlyReviewCounts(shopId, currentYear);

            return new ShopReviewStatisticsResult(
                totalCount,
                reviewStatisticsQueryDao.getAverageTasteRating(shopId),
                reviewStatisticsQueryDao.getAverageAmountRating(shopId),
                reviewStatisticsQueryDao.getAveragePriceRating(shopId),
                reviewStatisticsQueryDao.getAverageAtmosphereRating(shopId),
                reviewStatisticsQueryDao.getAverageKindnessRating(shopId),
                reviewStatisticsQueryDao.getAverageHygieneRating(shopId),
                willRevisitPercentage,
                ratingMap,
                monthlyMap
            );
        }

        return new ShopReviewStatisticsResult(
            totalCount,
            null, null, null, null, null, null, null,
            ratingMap,
            null
        );
    }

    /**
     * 회원이 쓴 노출 리뷰 수.
     */
    public long countVisibleReviewsByMemberId(Long memberId) {
        return reviewStatisticsQueryDao.countVisibleReviewsByMemberId(MemberId.of(memberId));
    }

    /**
     * 한 주문 안에서 회원이 이미 리뷰를 쓴 상품 식별자 집합.
     *
     * <p>주문 상세처럼 주문상품이 여러 건인 화면이 상품마다 단건 조회를 부르면 상품 수만큼
     * 쿼리가 나가므로(N+1), 호출부가 루프 전에 이 메서드로 1회 조회한 뒤 메모리에서 판정한다.
     */
    public Set<Long> findReviewedProductIds(Long orderId, Long memberId, Collection<Long> productIds) {
        return reviewQueryDao.findReviewedProductIds(orderId, MemberId.of(memberId), productIds);
    }

    /**
     * 내가 쓴 리뷰 목록(원본 result 반환 — 호출부가 Response를 조립한다).
     */
    public PageResult<MyReviewListItemResult> findMyReviews(Long memberId, int page, int size) {
        return reviewQueryDao.findMyReviews(MemberId.of(memberId), PageQuery.of(page, size));
    }

    /**
     * 리뷰 상세 결과 — 본문 조회 후 태그명을 덧붙인다. 과거 core 조회 서비스가 하던 조합을 그대로 옮겼다.
     */
    private Optional<ReviewDetailResult> findReviewDetailResult(ReviewId reviewId) {
        return reviewQueryDao.findReviewDetail(reviewId).map(result -> {
            List<Long> tagIds = reviewQueryDao.findTagIdsByReviewId(reviewId.value());
            if (tagIds.isEmpty()) {
                return result;
            }
            return result.withTagNames(reviewQueryDao.findTagNamesByIds(tagIds));
        });
    }

    /**
     * 리뷰가 가리키는 상품 식별자 — 상품 정보 조회에만 쓰인다. 상세 조회로 리뷰 존재는 이미 확인된 상태다.
     */
    private Long findProductIdOfReview(Long reviewId) {
        return reviewQueryDao.findProductIdByReviewId(reviewId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.REVIEW_NOT_FOUND));
    }

    /**
     * 팔로잉 타임라인 — 팔로우한 회원이 없으면 조회하지 않고 빈 페이지를 돌려준다.
     */
    private PageResult<LatestReviewListItemResult> findLatestReviewsByFollowing(MemberId memberId, int page, int size) {
        List<Long> followingMemberIds = memberFollowRepository.findFollowingIdsByFollowerId(memberId);

        if (followingMemberIds.isEmpty()) {
            return PageResult.empty(page, size);
        }

        return reviewQueryDao.findLatestReviewsByFollowing(followingMemberIds, PageQuery.of(page, size));
    }

    private ReviewBestListItemResponse toBestReviewListItemResponse(BestReviewListItemResult dto) {
        return ReviewBestListItemResponse.from(
            dto.id(),
            dto.imageUrl(),
            dto.stationName(),
            dto.shopName(),
            dto.productName(),
            dto.totalRating(),
            dto.content()
        );
    }

    private ReviewLatestListItemResponse toLatestReviewListItemResponse(LatestReviewListItemResult dto) {
        return ReviewLatestListItemResponse.from(
            dto.id(),
            dto.imageUrls(),
            dto.stationName(),
            dto.totalRating(),
            dto.content(),
            dto.memberId().value(),
            dto.memberNickname(),
            dto.memberProfileImageUrl(),
            dto.createdAt(),
            dto.likeCount(),
            dto.commentCount()
        );
    }

    private ReviewDetailResponse toReviewDetailResponse(ReviewDetailResult dto) {
        return ReviewDetailResponse.from(
            dto.id(),
            dto.shopId(),
            dto.shopName(),
            dto.stationName(),
            dto.content(),
            dto.totalRating(),
            dto.tasteRating(),
            dto.amountRating(),
            dto.priceRating(),
            dto.atmosphereRating(),
            dto.kindnessRating(),
            dto.hygieneRating(),
            dto.willRevisit(),
            dto.memberId().value(),
            dto.memberNickname(),
            dto.memberProfileImageUrl(),
            dto.createdAt(),
            dto.imageUrls(),
            dto.tagNames()
        );
    }

    private ReviewCommentResponse toCommentResponse(ReviewCommentItemResult dto, List<ReviewReplyResponse> replies) {
        return ReviewCommentResponse.from(
            dto.id(),
            dto.reviewId(),
            dto.memberId() != null ? dto.memberId().value() : null,
            dto.memberNickname(),
            dto.memberProfileImageUrl(),
            dto.content(),
            dto.createdAt(),
            replies
        );
    }

    private ReviewReplyResponse toReplyResponse(ReviewReplyItemResult dto) {
        return ReviewReplyResponse.from(
            dto.id(),
            dto.commentId(),
            dto.memberId() != null ? dto.memberId().value() : null,
            dto.memberNickname(),
            dto.memberProfileImageUrl(),
            dto.replyToMemberId() != null ? dto.replyToMemberId().value() : null,
            dto.replyToMemberNickname(),
            dto.content(),
            dto.createdAt()
        );
    }

    private String getFirstImageUrl(Long productId) {
        return productQueryDao.findProductImageUrls(productId).stream()
            .findFirst()
            .orElse(null);
    }
}
