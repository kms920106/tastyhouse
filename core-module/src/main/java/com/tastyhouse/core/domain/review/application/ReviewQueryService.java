package com.tastyhouse.core.domain.review.application;

import com.tastyhouse.core.domain.follow.application.FollowQueryService;
import com.tastyhouse.core.domain.member.application.MemberQueryService;
import com.tastyhouse.core.domain.member.application.dto.result.MemberWithProfileImageResult;
import com.tastyhouse.core.domain.review.application.dto.result.BestReviewListItemResult;
import com.tastyhouse.core.domain.review.application.dto.result.LatestReviewListItemResult;
import com.tastyhouse.core.domain.review.application.dto.result.MyReviewListItemResult;
import com.tastyhouse.core.domain.review.application.dto.result.ShopReviewStatisticsResult;
import com.tastyhouse.core.domain.review.application.dto.result.ProductReviewStatisticsResult;
import com.tastyhouse.core.domain.review.application.dto.result.ReviewDetailResult;
import com.tastyhouse.core.domain.review.application.dto.result.ReviewsByRatingResult;
import com.tastyhouse.core.domain.rank.application.dto.result.MemberReviewCountResult;
import com.tastyhouse.core.domain.review.domain.model.Review;
import com.tastyhouse.core.domain.review.domain.repository.ReviewRepository;
import com.tastyhouse.core.domain.review.domain.repository.ReviewLikeRepository;
import com.tastyhouse.core.domain.review.domain.repository.ReviewTagRepository;
import com.tastyhouse.core.domain.review.domain.model.ReviewComment;
import com.tastyhouse.core.domain.review.domain.model.ReviewReply;
import com.tastyhouse.core.domain.review.domain.repository.ReviewCommentRepository;
import com.tastyhouse.core.domain.review.domain.repository.ReviewReplyRepository;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.core.domain.shop.domain.repository.TagRepository;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReviewQueryService {

    private final ReviewRepository reviewRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final ReviewTagRepository reviewTagRepository;
    private final ReviewCommentRepository reviewCommentRepository;
    private final ReviewReplyRepository reviewReplyRepository;
    private final MemberQueryService memberQueryService;
    private final FollowQueryService followQueryService;
    private final TagRepository tagRepository;

    public PageResult<BestReviewListItemResult> findBestReviewsWithPagination(int page, int size) {
        return reviewRepository.findBestReviews(PageQuery.of(page, size));
    }

    public Review findById(Long id) {
        return reviewRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.REVIEW_NOT_FOUND));
    }

    public Optional<ReviewDetailResult> findReviewDetail(Long reviewId) {
        return reviewRepository.findReviewDetail(reviewId).map(result -> {
            List<Long> tagIds = reviewTagRepository.findTagIdsByReviewId(reviewId);
            if (!tagIds.isEmpty()) {
                List<String> tagNames = tagRepository.findTagNamesByIds(tagIds);
                return result.withTagNames(tagNames);
            }
            return result;
        });
    }

    public boolean isLikedByMember(Long reviewId, Long memberId) {
        return reviewLikeRepository.existsByReviewIdAndMemberId(reviewId, memberId);
    }

    public PageResult<LatestReviewListItemResult> findLatestReviewsWithPagination(int page, int size) {
        return reviewRepository.findLatestReviews(PageQuery.of(page, size));
    }

    public PageResult<LatestReviewListItemResult> findLatestReviewsByFollowingWithPagination(Long memberId, int page, int size) {
        List<Long> followingMemberIds = followQueryService.findFollowingIds(memberId);

        if (followingMemberIds.isEmpty()) {
            return PageResult.empty(page, size);
        }

        return reviewRepository.findLatestReviewsByFollowing(followingMemberIds, PageQuery.of(page, size));
    }

    public ReviewsByRatingResult findShopReviewsByRating(Long shopId, int page, int size, Boolean hasImage) {
        List<LatestReviewListItemResult> rating1Reviews = reviewRepository.findReviewsByShopIdAndRating(shopId, 1, 5);
        List<LatestReviewListItemResult> rating2Reviews = reviewRepository.findReviewsByShopIdAndRating(shopId, 2, 5);
        List<LatestReviewListItemResult> rating3Reviews = reviewRepository.findReviewsByShopIdAndRating(shopId, 3, 5);
        List<LatestReviewListItemResult> rating4Reviews = reviewRepository.findReviewsByShopIdAndRating(shopId, 4, 5);
        List<LatestReviewListItemResult> rating5Reviews = reviewRepository.findReviewsByShopIdAndRating(shopId, 5, 5);

        PageResult<LatestReviewListItemResult> allReviewsPage = reviewRepository.findLatestReviewsByShopId(shopId, null, PageQuery.of(page, size), hasImage, "LATEST");

        Long totalReviewCount = reviewRepository.countByShopIdAndHiddenFalse(shopId);

        Map<Integer, List<LatestReviewListItemResult>> reviewsByRating = new HashMap<>();
        reviewsByRating.put(1, rating1Reviews);
        reviewsByRating.put(2, rating2Reviews);
        reviewsByRating.put(3, rating3Reviews);
        reviewsByRating.put(4, rating4Reviews);
        reviewsByRating.put(5, rating5Reviews);

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

    public ReviewsByRatingResult findProductReviewsByRating(Long productId, int page, int size, Boolean hasImage) {
        List<LatestReviewListItemResult> rating1Reviews = reviewRepository.findReviewsByProductIdAndRating(productId, 1, 5);
        List<LatestReviewListItemResult> rating2Reviews = reviewRepository.findReviewsByProductIdAndRating(productId, 2, 5);
        List<LatestReviewListItemResult> rating3Reviews = reviewRepository.findReviewsByProductIdAndRating(productId, 3, 5);
        List<LatestReviewListItemResult> rating4Reviews = reviewRepository.findReviewsByProductIdAndRating(productId, 4, 5);
        List<LatestReviewListItemResult> rating5Reviews = reviewRepository.findReviewsByProductIdAndRating(productId, 5, 5);

        PageResult<LatestReviewListItemResult> allReviewsPage = reviewRepository.findLatestReviewsByProductId(productId, null, PageQuery.of(page, size), hasImage, "LATEST");

        Long totalReviewCount = reviewRepository.countByProductIdAndHiddenFalse(productId);

        Map<Integer, List<LatestReviewListItemResult>> reviewsByRating = new HashMap<>();
        reviewsByRating.put(1, rating1Reviews);
        reviewsByRating.put(2, rating2Reviews);
        reviewsByRating.put(3, rating3Reviews);
        reviewsByRating.put(4, rating4Reviews);
        reviewsByRating.put(5, rating5Reviews);

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
        Long totalCount = reviewRepository.countByShopIdAndHiddenFalse(shopId);

        Map<Integer, Long> ratingMap = reviewRepository.getRatingCounts(shopId);
        for (int r = 1; r <= 5; r++) {
            ratingMap.putIfAbsent(r, 0L);
        }

        if (totalCount > 0) {
            Long willRevisitCount = reviewRepository.countWillRevisit(shopId);
            double willRevisitPercentage = (willRevisitCount * 100.0) / totalCount;

            int currentYear = LocalDateTime.now().getYear();
            Map<Integer, Long> monthlyMap = reviewRepository.getMonthlyReviewCounts(shopId, currentYear);

            return new ShopReviewStatisticsResult(
                totalCount,
                reviewRepository.getAverageTasteRating(shopId),
                reviewRepository.getAverageAmountRating(shopId),
                reviewRepository.getAveragePriceRating(shopId),
                reviewRepository.getAverageAtmosphereRating(shopId),
                reviewRepository.getAverageKindnessRating(shopId),
                reviewRepository.getAverageHygieneRating(shopId),
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

    public ProductReviewStatisticsResult findProductReviewStatistics(Long productId) {
        Long totalCount = reviewRepository.countByProductIdAndHiddenFalse(productId);

        if (totalCount > 0) {
            return new ProductReviewStatisticsResult(
                totalCount,
                reviewRepository.getAverageTasteRatingByProductId(productId),
                reviewRepository.getAverageAmountRatingByProductId(productId),
                reviewRepository.getAveragePriceRatingByProductId(productId)
            );
        }

        return new ProductReviewStatisticsResult(totalCount, null, null, null);
    }

    public long countVisibleReviewsByMemberId(Long memberId) {
        return reviewRepository.countVisibleReviewsByMemberId(memberId);
    }

    public PageResult<MyReviewListItemResult> findMyReviews(Long memberId, int page, int size) {
        return reviewRepository.findMyReviews(memberId, PageQuery.of(page, size));
    }

    public PageResult<MyReviewListItemResult> findReviewsByMemberId(Long memberId, int page, int size) {
        return reviewRepository.findReviewsByMemberId(memberId, PageQuery.of(page, size));
    }

    public boolean isReviewedByOrderAndProduct(Long orderId, Long productId, Long memberId) {
        return reviewRepository.existsByOrderIdAndProductIdAndMemberId(orderId, productId, memberId);
    }

    public List<ReviewComment> findCommentsByReviewId(Long reviewId) {
        return reviewCommentRepository.findByReviewIdOrderByCreatedAtDesc(reviewId);
    }

    public List<ReviewReply> findRepliesByCommentIds(List<Long> commentIds) {
        if (commentIds.isEmpty()) {
            return List.of();
        }
        return reviewReplyRepository.findByCommentIdInAndHiddenFalseOrderByCreatedAtAsc(commentIds);
    }

    public Map<Long, MemberWithProfileImageResult> findMemberWithProfileImagesByIds(Collection<Long> memberIds) {
        return memberQueryService.findMemberWithProfileImagesByIds(memberIds);
    }

    public List<MemberReviewCountResult> countReviewsByMemberWithPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        return reviewRepository.countReviewsByMemberWithPeriod(startDate, endDate);
    }
}
