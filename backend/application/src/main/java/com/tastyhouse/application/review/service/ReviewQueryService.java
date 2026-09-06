package com.tastyhouse.application.review.service;

import com.tastyhouse.application.shared.marker.WebApp;
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
import com.tastyhouse.application.review.port.out.ReviewCommentListView;
import com.tastyhouse.application.review.port.out.ReviewDetailView;
import com.tastyhouse.application.review.port.out.ReviewProductView;
import com.tastyhouse.application.review.port.out.ReviewSubmitResultView;
import com.tastyhouse.application.review.port.out.ReviewWriteInfoView;
import com.tastyhouse.application.review.port.in.ReviewQueryUseCase;

@Service
@WebApp
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

    @Override
    public PageResult<BestReviewListItemResult> searchBestReviewList(int page, int size) {
        return reviewQueryPort.findBestReviews(PageQuery.of(page, size));
    }

    @Override
    public PageResult<LatestReviewListItemResult> searchLatestReviewList(
        int page,
        int size,
        String type,
        Long memberId
    ) {
        if (ReviewListType.from(type) == ReviewListType.FOLLOWING && memberId != null) {
            return findLatestReviewsByFollowing(MemberId.of(memberId), page, size);
        }
        return reviewQueryPort.findLatestReviews(PageQuery.of(page, size));
    }

    @Override
    public Optional<ReviewDetailView> findReviewDetail(Long reviewId, Long viewerMemberId) {
        return findReviewDetailResult(ReviewId.of(reviewId), viewerMemberId)
            .map(result -> toReviewDetailView(result, viewerMemberId));
    }

    @Override
    public ReviewSubmitResultView getReviewSubmitResult(Long reviewId, Long authorMemberId) {
        ReviewDetailResult detail = findReviewDetailResult(ReviewId.of(reviewId), authorMemberId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REVIEW_NOT_FOUND));

        return new ReviewSubmitResultView(
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

    @Override
    public boolean isLiked(Long reviewId, Long memberId) {
        return reviewQueryPort.existsLike(ReviewId.of(reviewId), memberId);
    }

    @Override
    public ReviewCommentListView searchCommentsWithReplies(Long reviewId, Long viewerMemberId) {
        requireVisibleReview(reviewId, viewerMemberId);

        List<ReviewCommentItemResult> comments = reviewQueryPort.findComments(ReviewId.of(reviewId));

        if (comments.isEmpty()) {
            return new ReviewCommentListView(List.of(), 0);
        }

        List<ReviewCommentId> commentIds = comments.stream()
            .map(comment -> ReviewCommentId.of(comment.id()))
            .toList();

        List<ReviewReplyItemResult> allReplies = reviewQueryPort.findVisibleReplies(commentIds);

        Map<Long, List<ReviewReplyItemResult>> repliesByCommentId = allReplies.stream()
            .collect(Collectors.groupingBy(ReviewReplyItemResult::commentId));

        List<ReviewCommentListView.CommentWithReplies> items = comments.stream()
            .map(comment -> new ReviewCommentListView.CommentWithReplies(
                comment,
                repliesByCommentId.getOrDefault(comment.id(), List.of())
            ))
            .toList();

        int totalCount = comments.size() + allReplies.size();
        return new ReviewCommentListView(items, totalCount);
    }

    @Override
    public Optional<ReviewProductView> findReviewProduct(Long reviewId, Long viewerMemberId) {
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

                return new ReviewProductView(
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
                new ReviewProductView(
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

    @Override
    public ReviewWriteInfoView getReviewWriteInfo(Long orderProductId, Long memberId) {
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

        return new ReviewWriteInfoView(
            product.id(),
            product.name(),
            getFirstImageUrl(product.id()),
            price,
            ownership.orderId(),
            reviewed,
            ownership.orderMethod()
        );
    }

    private ReviewSortType resolveSortType(Long shopId, String sortType) {
        if (sortType != null) {
            return ReviewSortType.from(sortType);
        }
        return shopReviewDisplaySettingQueryPort.findSortTypeByShopId(shopId)
            .orElse(ReviewSortType.LATEST);
    }

    @Override
    public PageResult<MyReviewListItemResult> findMemberReviews(Long memberId, int page, int size) {
        return reviewQueryPort.findReviewsByMemberId(memberId, PageQuery.of(page, size));
    }

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

    public long countVisibleReviewsByMemberId(Long memberId) {
        return reviewStatisticsQueryPort.countVisibleReviewsByMemberId(memberId);
    }

    public Set<Long> findReviewedProductIds(Long orderId, Long memberId, Collection<Long> productIds) {
        return reviewQueryPort.findReviewedProductIds(orderId, memberId, productIds);
    }

    public PageResult<MyReviewListItemResult> findMyReviews(Long memberId, int page, int size) {
        return reviewQueryPort.findMyReviews(memberId, PageQuery.of(page, size));
    }

    @Override
    public void requireVisibleReview(Long reviewId, Long viewerMemberId) {
        findReviewDetailResult(ReviewId.of(reviewId), viewerMemberId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REVIEW_NOT_FOUND));
    }

    private Optional<ReviewDetailResult> findReviewDetailResult(ReviewId reviewId, Long viewerMemberId) {
        return reviewQueryPort.findReviewDetail(reviewId, viewerMemberId).map(result -> {
            List<Long> tagIds = reviewTagQueryPort.findTagIdsByReviewId(reviewId.value());
            if (tagIds.isEmpty()) {
                return result;
            }
            return result.withTagNames(reviewTagQueryPort.findTagNamesByIds(tagIds));
        });
    }

    private Long findProductIdOfReview(Long reviewId) {
        return reviewQueryPort.findProductIdByReviewId(reviewId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REVIEW_NOT_FOUND));
    }

    private PageResult<LatestReviewListItemResult> findLatestReviewsByFollowing(MemberId memberId, int page, int size) {
        List<Long> followingMemberIds = memberFollowQueryPort.findFollowingIds(memberId);

        if (followingMemberIds.isEmpty()) {
            return PageResult.empty(page, size);
        }

        return reviewQueryPort.findLatestReviewsByFollowing(followingMemberIds, PageQuery.of(page, size));
    }

    private ReviewDetailView toReviewDetailView(ReviewDetailResult dto, Long viewerMemberId) {
        boolean author = viewerMemberId != null && viewerMemberId.equals(dto.memberId());
        OrderMethod orderMethod = author ? dto.orderMethod() : null;

        return new ReviewDetailView(
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

    private String getFirstImageUrl(Long productId) {
        return productQueryPort.findProductImageUrls(productId).stream()
            .findFirst()
            .orElse(null);
    }
}
