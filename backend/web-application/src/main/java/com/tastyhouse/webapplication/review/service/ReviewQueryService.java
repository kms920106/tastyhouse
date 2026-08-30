package com.tastyhouse.webapplication.review.service;

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

import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.review.model.ReviewSortType;
import com.tastyhouse.domain.review.vo.ReviewCommentId;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.order.port.out.OrderProductOwnershipResult;
import com.tastyhouse.application.order.port.out.OrderQueryPort;
import com.tastyhouse.application.member.follow.port.out.MemberFollowQueryPort;
import com.tastyhouse.application.product.port.out.ProductDetailResult;
import com.tastyhouse.application.product.port.out.ProductQueryPort;
import com.tastyhouse.application.review.port.out.BestReviewListItemResult;
import com.tastyhouse.application.review.port.out.LatestReviewListItemResult;
import com.tastyhouse.application.review.port.out.MyReviewListItemResult;
import com.tastyhouse.application.review.port.out.ReviewCommentItemResult;
import com.tastyhouse.application.review.port.out.ReviewDetailResult;
import com.tastyhouse.application.review.port.out.ReviewQueryPort;
import com.tastyhouse.application.review.port.out.ReviewTagQueryPort;
import com.tastyhouse.application.review.port.out.ReviewReplyItemResult;
import com.tastyhouse.application.review.port.out.ReviewStatisticsQueryPort;
import com.tastyhouse.application.review.port.out.ReviewsByRatingResult;
import com.tastyhouse.application.review.port.out.ShopReviewDisplaySettingQueryPort;
import com.tastyhouse.application.review.port.out.ShopReviewStatisticsResult;
import com.tastyhouse.webapplication.review.response.ReviewBestListItemResponse;
import com.tastyhouse.webapplication.review.response.ReviewCommentListResponse;
import com.tastyhouse.webapplication.review.response.ReviewCommentResponse;
import com.tastyhouse.webapplication.review.response.ReviewDetailResponse;
import com.tastyhouse.webapplication.review.response.ReviewLatestListItemResponse;
import com.tastyhouse.webapplication.review.response.ReviewLikeStatusResponse;
import com.tastyhouse.webapplication.review.response.ReviewMemberListItemResponse;
import com.tastyhouse.webapplication.review.response.ReviewProductResponse;
import com.tastyhouse.webapplication.review.response.ReviewReplyResponse;
import com.tastyhouse.webapplication.review.response.ReviewResponse;
import com.tastyhouse.webapplication.review.response.ReviewWriteInfoResponse;
import com.tastyhouse.webapplication.review.port.in.ReviewQueryUseCase;

/**
 * 리뷰 조회 서비스(web).
 *
 * <p>읽기 포트({@link ReviewQueryPort}·{@link ReviewStatisticsQueryPort})만 주입해 조회하고
 * Response를 조립한다(private 매퍼).
 * 파일 경로 → 표시용 URL 변환은 DAO({@code FileUrlResolver})가 담당하므로 이 계층은 이미 URL이 된
 * 필드를 그대로 조립한다(응답에 파일 식별자·경로를 노출하지 않는다는 규칙).
 *
 * <p>과거 core 조회 서비스가 여러 조회를 조합해 만들던 값(리뷰 상세의 태그명)도 이 계층이 조합한다 —
 * DAO는 단일 조회 단위만 제공한다.
 *
 * <p>리뷰 화면이 곁들여 보여주는 상품 정보(상품명·가격·대표 이미지)는 다른 도메인의 QueryService를 경유하지
 * 않고 {@link ProductQueryPort}를 직접 주입해 조회한다 — 서비스를 경유하면 상품 쪽이 리뷰 통계를 얻기 위해
 * 이 서비스를 다시 주입해야 해서 빈 순환 참조가 생긴다. 표현 목적 조회는 DAO 계층에서 교차하는 것이 옳다.
 *
 * <p>명령 동작은 {@link ReviewCommandService}로 분리했다(CQRS).
 */
@Service
@Transactional(readOnly = true)
public class ReviewQueryService implements ReviewQueryUseCase {

    private final ReviewQueryPort reviewQueryPort;
    private final ReviewTagQueryPort reviewTagQueryPort;
    private final ReviewStatisticsQueryPort reviewStatisticsQueryPort;
    private final ShopReviewDisplaySettingQueryPort shopReviewDisplaySettingQueryPort;
    private final ProductQueryPort productQueryPort;
    private final MemberFollowQueryPort memberFollowQueryPort;
    private final OrderQueryPort orderQueryPort;

    public ReviewQueryService(
        ReviewQueryPort reviewQueryPort,
        ReviewTagQueryPort reviewTagQueryPort,
        ReviewStatisticsQueryPort reviewStatisticsQueryPort,
        ShopReviewDisplaySettingQueryPort shopReviewDisplaySettingQueryPort,
        ProductQueryPort productQueryPort,
        MemberFollowQueryPort memberFollowQueryPort,
        OrderQueryPort orderQueryPort
    ) {
        this.reviewQueryPort = reviewQueryPort;
        this.reviewTagQueryPort = reviewTagQueryPort;
        this.reviewStatisticsQueryPort = reviewStatisticsQueryPort;
        this.shopReviewDisplaySettingQueryPort = shopReviewDisplaySettingQueryPort;
        this.productQueryPort = productQueryPort;
        this.memberFollowQueryPort = memberFollowQueryPort;
        this.orderQueryPort = orderQueryPort;
    }

    /**
     * 베스트 리뷰 목록.
     */
    @Override
    public PaginationResponse<ReviewBestListItemResponse> searchBestReviewList(int page, int size) {
        PageResult<ReviewBestListItemResponse> pageResult = reviewQueryPort.findBestReviews(PageQuery.of(page, size))
            .map(this::toBestReviewListItemResponse);
        return PaginationResponse.from(pageResult);
    }

    /**
     * 최신 리뷰 목록 — FOLLOWING이면 로그인 회원이 팔로우한 회원들의 리뷰만 조회한다.
     */
    @Override
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
            pageResult = reviewQueryPort.findLatestReviews(PageQuery.of(page, size))
                .map(this::toLatestReviewListItemResponse);
        }
        return PaginationResponse.from(pageResult);
    }

    /**
     * 리뷰 상세 — 상세 본문 조회와 태그명 조회를 조합한다. 태그가 없으면 조합하지 않는다.
     *
     * <p>{@code viewerMemberId}는 <b>선택적</b>이다({@code null} = 비로그인). 사장님만보기 리뷰는
     * 작성자 본인에게만 노출되며, 그 외에는 빈 결과가 돌아가 컨트롤러가 404를 낸다.
     */
    @Override
    public Optional<ReviewDetailResponse> findReviewDetail(Long reviewId, Long viewerMemberId) {
        return findReviewDetailResult(ReviewId.of(reviewId), viewerMemberId)
            .map(result -> toReviewDetailResponse(result, viewerMemberId));
    }

    /**
     * 리뷰 등록·수정 응답 — 명령이 돌려준 식별자로 커밋 이후 재조회해 조립한다.
     *
     * <p>{@link ReviewCommandService}가 식별자만 반환하므로(CQRS 교차 주입 금지) 등록·수정 API의 응답은
     * 이 메서드가 만든다. 응답 계약을 바꾸지 않기 위해 {@code ReviewResponse}의 필드 구성은 그대로 두고,
     * 값의 출처만 "명령이 들고 있던 도메인 모델"에서 "커밋된 행의 투영"으로 옮겼다.
     *
     * <p><b>⚠️ {@code authorMemberId}(작성자)를 반드시 뷰어로 넘겨야 한다.</b> 리뷰 상세 투영은 이제
     * 사장님만보기 리뷰를 <b>작성자 본인에게만</b> 노출하므로, 뷰어를 넘기지 않으면 사장님만보기로 등록하는
     * 순간 등록 자체는 성공했는데 응답 조립에서 {@code REVIEW_NOT_FOUND}(404)가 나는 회귀가 생긴다.
     */
    @Override
    public ReviewResponse getReviewResponse(Long reviewId, Long authorMemberId) {
        ReviewDetailResult detail = findReviewDetailResult(ReviewId.of(reviewId), authorMemberId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REVIEW_NOT_FOUND));

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
    @Override
    public ReviewLikeStatusResponse isLiked(Long reviewId, Long memberId) {
        boolean liked = reviewQueryPort.existsLike(ReviewId.of(reviewId), memberId);
        return ReviewLikeStatusResponse.from(liked);
    }

    /**
     * 리뷰의 댓글·답글 목록. 댓글은 숨김 포함, 답글은 숨김 제외다(기존 동작 유지).
     *
     * <p><b>리뷰 가시성 가드가 선행한다(기존 결함 수정).</b> 과거에는 이 경로가 리뷰의 노출 여부를 전혀
     * 확인하지 않아 {@code reviewId}만 알면 숨김 리뷰의 댓글도 누구나 조회할 수 있었다. 리뷰 상세 투영이
     * {@code hidden}·{@code ownerOnly} 두 축을 함께 판정하므로 가드 한 번으로 둘 다 막힌다(추가 쿼리
     * 1회 비용은 감수한다).
     */
    @Override
    public ReviewCommentListResponse searchCommentsWithReplies(Long reviewId, Long viewerMemberId) {
        requireVisibleReview(reviewId, viewerMemberId);

        List<ReviewCommentItemResult> comments = reviewQueryPort.findComments(ReviewId.of(reviewId));

        if (comments.isEmpty()) {
            return ReviewCommentListResponse.from(List.of(), 0);
        }

        List<ReviewCommentId> commentIds = comments.stream()
            .map(comment -> ReviewCommentId.of(comment.id()))
            .toList();

        List<ReviewReplyItemResult> allReplies = reviewQueryPort.findVisibleReplies(commentIds);

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
    @Override
    public Optional<ReviewProductResponse> findReviewProduct(Long reviewId, Long viewerMemberId) {
        Optional<ReviewDetailResult> reviewDetailOpt = findReviewDetailResult(ReviewId.of(reviewId), viewerMemberId);
        if (reviewDetailOpt.isEmpty()) {
            return Optional.empty();
        }

        ReviewDetailResult reviewDetail = reviewDetailOpt.get();

        List<String> reviewImageUrls = reviewDetail.imageUrls();
        String reviewMemberProfileImageUrl = reviewDetail.memberProfileImageUrl();

        return productQueryPort.findProductDetailById(findProductIdOfReview(reviewId))
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
                    reviewDetail.memberId(),
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
                    reviewDetail.memberId(),
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
    @Override
    public ReviewWriteInfoResponse getReviewWriteInfo(Long orderProductId, Long memberId) {
        if (memberId == null) {
            throw new BusinessException(ErrorCode.AUTH_REQUIRED);
        }

        OrderProductOwnershipResult ownership = orderQueryPort.findOrderProductOwnership(orderProductId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REVIEW_ORDER_PRODUCT_NOT_FOUND));

        if (ownership.orderMemberId() == null) {
            throw new ResourceNotFoundException(ErrorCode.ORDER_NOT_FOUND);
        }
        if (!ownership.orderMemberId().equals(memberId)) {
            throw new BusinessException(ErrorCode.REVIEW_ORDER_ACCESS_DENIED);
        }

        ProductDetailResult product = productQueryPort.findProductDetailById(ownership.productId())
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORDER_PRODUCT_NOT_FOUND));

        Integer price = product.discountPrice() != null
            ? product.discountPrice()
            : product.originalPrice();

        boolean reviewed = reviewQueryPort.existsByOrderIdAndProductIdAndMemberId(
            ownership.orderId(), ownership.productId(), memberId
        );

        return ReviewWriteInfoResponse.from(
            product.id(),
            product.name(),
            getFirstImageUrl(product.id()),
            price,
            ownership.orderId(),
            reviewed,
            ownership.orderMethod()
        );
    }

    /**
     * 적용할 정렬을 결정한다 — 고객의 명시적 선택 &gt; 점주 저장 설정 &gt; {@link ReviewSortType#LATEST}.
     */
    private ReviewSortType resolveSortType(Long shopId, String sortType) {
        if (sortType != null) {
            return ReviewSortType.from(sortType);
        }
        return shopReviewDisplaySettingQueryPort.findSortTypeByShopId(shopId)
            .orElse(ReviewSortType.LATEST);
    }

    /**
     * 특정 회원이 쓴 리뷰 목록(대표 이미지 1장).
     */
    @Override
    public PaginationResponse<ReviewMemberListItemResponse> findMemberReviews(Long memberId, int page, int size) {
        PageResult<ReviewMemberListItemResponse> pageResult =
            reviewQueryPort.findReviewsByMemberId(memberId, PageQuery.of(page, size))
                .map(dto -> ReviewMemberListItemResponse.from(
                    dto.id(),
                    dto.imageUrl()
                ));
        return PaginationResponse.from(pageResult);
    }

    /**
     * 가게의 평점대별 리뷰 묶음 — 1~5점 각 5건과 전체 페이지, 전체 건수를 조합한다.
     *
     * <p>{@code sortType}이 <b>생략되면</b> 그 가게의 점주 저장 설정을 적용하고, 설정도 없으면
     * {@link ReviewSortType#LATEST}다. <b>명시되면 그 값이 우선</b>한다 — 고객이 앱에서 정렬을 바꿔 볼 수
     * 있어야 하므로 점주 설정이 명시적 선택을 덮어써서는 안 된다.
     *
     * <p>점주 설정을 write 포트가 아니라 {@code ShopReviewDisplaySettingQueryPort}로 읽는다 — 조회 전용
     * 서비스에 write 포트를 주입하면 CQRS 교차 주입 금지 규칙을 어긴다.
     */
    public ReviewsByRatingResult findShopReviewsByRating(
        Long shopId,
        int page,
        int size,
        Boolean hasImage,
        String sortType
    ) {
        Map<Integer, List<LatestReviewListItemResult>> reviewsByRating = new HashMap<>();
        for (int rating = 1; rating <= 5; rating++) {
            reviewsByRating.put(rating, reviewQueryPort.findReviewsByShopIdAndRating(shopId, rating, 5));
        }

        PageQuery pageQuery = PageQuery.of(page, size);
        PageResult<LatestReviewListItemResult> allReviewsPage = reviewQueryPort.findLatestReviewsByShopId(
            shopId,
            null,
            pageQuery,
            hasImage,
            resolveSortType(shopId, sortType)
        );

        Long totalReviewCount = reviewStatisticsQueryPort.countVisibleByShopId(shopId);

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
        Long totalCount = reviewStatisticsQueryPort.countVisibleByShopId(shopId);

        Map<Integer, Long> ratingMap = reviewStatisticsQueryPort.getRatingCounts(shopId);
        for (int rating = 1; rating <= 5; rating++) {
            ratingMap.putIfAbsent(rating, 0L);
        }

        if (totalCount > 0) {
            Long willRevisitCount = reviewStatisticsQueryPort.countWillRevisit(shopId);
            double willRevisitPercentage = (willRevisitCount * 100.0) / totalCount;

            int currentYear = LocalDateTime.now().getYear();
            Map<Integer, Long> monthlyMap = reviewStatisticsQueryPort.getMonthlyReviewCounts(shopId, currentYear);

            return new ShopReviewStatisticsResult(
                totalCount,
                reviewStatisticsQueryPort.getAverageTasteRating(shopId),
                reviewStatisticsQueryPort.getAverageAmountRating(shopId),
                reviewStatisticsQueryPort.getAveragePriceRating(shopId),
                reviewStatisticsQueryPort.getAverageAtmosphereRating(shopId),
                reviewStatisticsQueryPort.getAverageKindnessRating(shopId),
                reviewStatisticsQueryPort.getAverageHygieneRating(shopId),
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
        return reviewStatisticsQueryPort.countVisibleReviewsByMemberId(memberId);
    }

    /**
     * 한 주문 안에서 회원이 이미 리뷰를 쓴 상품 식별자 집합.
     *
     * <p>주문 상세처럼 주문상품이 여러 건인 화면이 상품마다 단건 조회를 부르면 상품 수만큼
     * 쿼리가 나가므로(N+1), 호출부가 루프 전에 이 메서드로 1회 조회한 뒤 메모리에서 판정한다.
     */
    public Set<Long> findReviewedProductIds(Long orderId, Long memberId, Collection<Long> productIds) {
        return reviewQueryPort.findReviewedProductIds(orderId, memberId, productIds);
    }

    /**
     * 내가 쓴 리뷰 목록(원본 result 반환 — 호출부가 Response를 조립한다).
     */
    public PageResult<MyReviewListItemResult> findMyReviews(Long memberId, int page, int size) {
        return reviewQueryPort.findMyReviews(memberId, PageQuery.of(page, size));
    }

    /**
     * 리뷰 가시성 가드 — 뷰어에게 보이지 않는 리뷰면 {@code REVIEW_NOT_FOUND}(404)를 던진다.
     *
     * <p>댓글 조회·등록처럼 리뷰에 종속된 경로가 리뷰 자체의 노출 여부를 우회하지 못하게 막는다.
     * 403이 아니라 404인 이유는 403이 "그 리뷰가 존재한다"는 사실을 노출하기 때문이다.
     *
     * <p><b>조회(GET)와 등록(POST)의 가드 위치가 다른 것은 의도적이다.</b> 조회는 이 서비스 안에서
     * ({@link #searchCommentsWithReplies}) 직접 걸지만, 등록은 컨트롤러가 이 메서드를 호출한 뒤
     * command 서비스를 부른다 — command 서비스가 query 서비스를 주입받는 것은 CQRS 교차 주입 금지
     * 규약 위반이기 때문이다. 한쪽으로 통일하려다 중복 쿼리를 만들지 말 것.
     */
    @Override
    public void requireVisibleReview(Long reviewId, Long viewerMemberId) {
        findReviewDetailResult(ReviewId.of(reviewId), viewerMemberId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REVIEW_NOT_FOUND));
    }

    /**
     * 리뷰 상세 결과 — 본문 조회 후 태그명을 덧붙인다. 과거 core 조회 서비스가 하던 조합을 그대로 옮겼다.
     */
    private Optional<ReviewDetailResult> findReviewDetailResult(ReviewId reviewId, Long viewerMemberId) {
        return reviewQueryPort.findReviewDetail(reviewId, viewerMemberId).map(result -> {
            List<Long> tagIds = reviewTagQueryPort.findTagIdsByReviewId(reviewId.value());
            if (tagIds.isEmpty()) {
                return result;
            }
            return result.withTagNames(reviewTagQueryPort.findTagNamesByIds(tagIds));
        });
    }

    /**
     * 리뷰가 가리키는 상품 식별자 — 상품 정보 조회에만 쓰인다. 상세 조회로 리뷰 존재는 이미 확인된 상태다.
     */
    private Long findProductIdOfReview(Long reviewId) {
        return reviewQueryPort.findProductIdByReviewId(reviewId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REVIEW_NOT_FOUND));
    }

    /**
     * 팔로잉 타임라인 — 팔로우한 회원이 없으면 조회하지 않고 빈 페이지를 돌려준다.
     */
    private PageResult<LatestReviewListItemResult> findLatestReviewsByFollowing(MemberId memberId, int page, int size) {
        List<Long> followingMemberIds = memberFollowQueryPort.findFollowingIds(memberId);

        if (followingMemberIds.isEmpty()) {
            return PageResult.empty(page, size);
        }

        return reviewQueryPort.findLatestReviewsByFollowing(followingMemberIds, PageQuery.of(page, size));
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
            dto.memberId(),
            dto.memberNickname(),
            dto.memberProfileImageUrl(),
            dto.createdAt(),
            dto.likeCount(),
            dto.commentCount()
        );
    }

    /**
     * 리뷰 상세 응답 조립 — 배달 평가 3필드는 <b>작성자 본인일 때만</b> 채운다.
     *
     * <p>배달 평가는 「배민 앱 미노출」 규격에 따라 다른 고객에게 보이지 않아야 하지만, 작성자 본인이
     * 자기 수정 폼을 열 때는 기존 값이 초깃값으로 필요하다. 두 요구가 충돌하지 않도록 노출 범위를
     * 프론트 신뢰가 아니라 <b>서버 판정</b>으로 가른다 — 뷰어가 작성자가 아니면 세 필드 모두 {@code null}이다.
     *
     * <p>{@code viewerMemberId}가 {@code null}(비로그인)이면 당연히 작성자가 아니므로 가려진다.
     *
     * <p><b>수정 폼은 여기서 받은 값을 그대로 되돌려 보내야 한다.</b> 수정 API는 PUT(전체 교체) 의미를
     * 유지해 받은 값을 조건 없이 덮어쓰므로, 폼이 값을 비운 채 제출하면 기존 배달 평가가 지워진다.
     * "값이 없으면 유지"를 서버에 넣지 않은 것은 그 순간 {@code null}이 "안 보냄"과 "지워줘" 두 뜻을 갖게
     * 되어 배달 평가를 지울 방법이 사라지기 때문이다.
     */
    private ReviewDetailResponse toReviewDetailResponse(ReviewDetailResult dto, Long viewerMemberId) {
        boolean author = viewerMemberId != null && viewerMemberId.equals(dto.memberId());
        OrderMethod orderMethod = author ? dto.orderMethod() : null;

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
            dto.memberId(),
            dto.memberNickname(),
            dto.memberProfileImageUrl(),
            dto.createdAt(),
            dto.imageUrls(),
            dto.tagNames(),
            dto.ownerOnly(),
            dto.ownerReplyContent(),
            dto.ownerReplyCreatedAt(),
            orderMethod == null ? null : orderMethod.name(),
            author ? dto.deliveryRating() : null,
            author ? dto.deliveryComment() : null
        );
    }

    private ReviewCommentResponse toCommentResponse(ReviewCommentItemResult dto, List<ReviewReplyResponse> replies) {
        return ReviewCommentResponse.from(
            dto.id(),
            dto.reviewId(),
            dto.memberId(),
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
            dto.memberId(),
            dto.memberNickname(),
            dto.memberProfileImageUrl(),
            dto.replyToMemberId(),
            dto.replyToMemberNickname(),
            dto.content(),
            dto.createdAt()
        );
    }

    private String getFirstImageUrl(Long productId) {
        return productQueryPort.findProductImageUrls(productId).stream()
            .findFirst()
            .orElse(null);
    }
}
