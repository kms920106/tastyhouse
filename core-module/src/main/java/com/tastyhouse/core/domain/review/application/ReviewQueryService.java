package com.tastyhouse.core.domain.review.application;

import com.tastyhouse.core.common.PageResult;
import com.tastyhouse.core.domain.follow.application.FollowQueryService;
import com.tastyhouse.core.domain.member.application.MemberQueryService;
import com.tastyhouse.core.domain.member.application.dto.result.MemberWithProfileImageResult;
import com.tastyhouse.core.domain.review.application.dto.result.BestReviewListItemResult;
import com.tastyhouse.core.domain.review.application.dto.result.LatestReviewListItemResult;
import com.tastyhouse.core.domain.review.application.dto.result.MyReviewListItemResult;
import com.tastyhouse.core.domain.review.application.dto.result.PlaceReviewStatisticsResult;
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
import com.tastyhouse.core.domain.place.domain.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    // TODO: Place BC 미전환 과도기 허용 — Place 도메인 DDD 전환 후 PlaceTagQueryService 참조로 교체
    private final TagRepository tagRepository;

    public Page<BestReviewListItemResult> findBestReviewsWithPagination(int page, int size) {
        return reviewRepository.findBestReviews(PageRequest.of(page, size));
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

    public Page<LatestReviewListItemResult> findLatestReviewsWithPagination(int page, int size) {
        return reviewRepository.findLatestReviews(PageRequest.of(page, size));
    }

    public Page<LatestReviewListItemResult> findLatestReviewsByFollowingWithPagination(Long memberId, int page, int size) {
        List<Long> followingMemberIds = followQueryService.findFollowingIds(memberId);

        PageRequest pageRequest = PageRequest.of(page, size);
        if (followingMemberIds.isEmpty()) {
            return new PageImpl<>(new ArrayList<>(), pageRequest, 0);
        }

        return reviewRepository.findLatestReviewsByFollowing(followingMemberIds, pageRequest);
    }

    public ReviewsByRatingResult findPlaceReviewsByRating(Long placeId, int page, int size) {
        List<LatestReviewListItemResult> rating1Reviews = reviewRepository.findReviewsByPlaceIdAndRating(placeId, 1, 5);
        List<LatestReviewListItemResult> rating2Reviews = reviewRepository.findReviewsByPlaceIdAndRating(placeId, 2, 5);
        List<LatestReviewListItemResult> rating3Reviews = reviewRepository.findReviewsByPlaceIdAndRating(placeId, 3, 5);
        List<LatestReviewListItemResult> rating4Reviews = reviewRepository.findReviewsByPlaceIdAndRating(placeId, 4, 5);
        List<LatestReviewListItemResult> rating5Reviews = reviewRepository.findReviewsByPlaceIdAndRating(placeId, 5, 5);

        PageRequest pageRequest = PageRequest.of(page, size);
        Page<LatestReviewListItemResult> allReviewsPage = reviewRepository.findLatestReviewsByPlaceId(placeId, null, pageRequest, null, "LATEST");

        Long totalReviewCount = reviewRepository.countByPlaceIdAndIsHiddenFalse(placeId);

        Map<Integer, List<LatestReviewListItemResult>> reviewsByRating = new HashMap<>();
        reviewsByRating.put(1, rating1Reviews);
        reviewsByRating.put(2, rating2Reviews);
        reviewsByRating.put(3, rating3Reviews);
        reviewsByRating.put(4, rating4Reviews);
        reviewsByRating.put(5, rating5Reviews);

        return new ReviewsByRatingResult(
            reviewsByRating,
            allReviewsPage.getContent(),
            totalReviewCount,
            allReviewsPage.getTotalElements(),
            allReviewsPage.getTotalPages(),
            allReviewsPage.getNumber(),
            allReviewsPage.getSize()
        );
    }

    public ReviewsByRatingResult findProductReviewsByRating(Long productId, int page, int size) {
        List<LatestReviewListItemResult> rating1Reviews = reviewRepository.findReviewsByProductIdAndRating(productId, 1, 5);
        List<LatestReviewListItemResult> rating2Reviews = reviewRepository.findReviewsByProductIdAndRating(productId, 2, 5);
        List<LatestReviewListItemResult> rating3Reviews = reviewRepository.findReviewsByProductIdAndRating(productId, 3, 5);
        List<LatestReviewListItemResult> rating4Reviews = reviewRepository.findReviewsByProductIdAndRating(productId, 4, 5);
        List<LatestReviewListItemResult> rating5Reviews = reviewRepository.findReviewsByProductIdAndRating(productId, 5, 5);

        PageRequest pageRequest = PageRequest.of(page, size);
        Page<LatestReviewListItemResult> allReviewsPage = reviewRepository.findLatestReviewsByProductId(productId, null, pageRequest, null, "LATEST");

        Long totalReviewCount = reviewRepository.countByProductIdAndIsHiddenFalse(productId);

        Map<Integer, List<LatestReviewListItemResult>> reviewsByRating = new HashMap<>();
        reviewsByRating.put(1, rating1Reviews);
        reviewsByRating.put(2, rating2Reviews);
        reviewsByRating.put(3, rating3Reviews);
        reviewsByRating.put(4, rating4Reviews);
        reviewsByRating.put(5, rating5Reviews);

        return new ReviewsByRatingResult(
            reviewsByRating,
            allReviewsPage.getContent(),
            totalReviewCount,
            allReviewsPage.getTotalElements(),
            allReviewsPage.getTotalPages(),
            allReviewsPage.getNumber(),
            allReviewsPage.getSize()
        );
    }

    public PlaceReviewStatisticsResult findPlaceReviewStatistics(Long placeId) {
        Long totalCount = reviewRepository.countByPlaceIdAndIsHiddenFalse(placeId);

        Map<Integer, Long> ratingMap = reviewRepository.getRatingCounts(placeId);
        for (int r = 1; r <= 5; r++) {
            ratingMap.putIfAbsent(r, 0L);
        }

        if (totalCount > 0) {
            Long willRevisitCount = reviewRepository.countWillRevisit(placeId);
            double willRevisitPercentage = (willRevisitCount * 100.0) / totalCount;

            int currentYear = LocalDateTime.now().getYear();
            Map<Integer, Long> monthlyMap = reviewRepository.getMonthlyReviewCounts(placeId, currentYear);

            return new PlaceReviewStatisticsResult(
                totalCount,
                reviewRepository.getAverageTasteRating(placeId),
                reviewRepository.getAverageAmountRating(placeId),
                reviewRepository.getAveragePriceRating(placeId),
                reviewRepository.getAverageAtmosphereRating(placeId),
                reviewRepository.getAverageKindnessRating(placeId),
                reviewRepository.getAverageHygieneRating(placeId),
                willRevisitPercentage,
                ratingMap,
                monthlyMap
            );
        }

        return new PlaceReviewStatisticsResult(
            totalCount,
            null, null, null, null, null, null, null,
            ratingMap,
            null
        );
    }

    public ProductReviewStatisticsResult findProductReviewStatistics(Long productId) {
        Long totalCount = reviewRepository.countByProductIdAndIsHiddenFalse(productId);

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

    public Page<MyReviewListItemResult> findMyReviews(Long memberId, int page, int size) {
        return reviewRepository.findMyReviews(memberId, PageRequest.of(page, size));
    }

    public Page<MyReviewListItemResult> findReviewsByMemberId(Long memberId, int page, int size) {
        return reviewRepository.findReviewsByMemberId(memberId, PageRequest.of(page, size));
    }

    public boolean isReviewedByOrderAndProduct(Long orderId, Long productId, Long memberId) {
        return reviewRepository.existsByOrderIdAndProductIdAndMemberId(orderId, productId, memberId);
    }

    public boolean existsReviewByOrderItemAndMember(Long orderId, Long productId, Long memberId) {
        return reviewRepository.existsByOrderIdAndProductIdAndMemberId(orderId, productId, memberId);
    }

    public Optional<Review> findReviewByIdAndMemberId(Long reviewId, Long memberId) {
        return reviewRepository.findByIdAndMemberId(reviewId, memberId);
    }

    public List<ReviewComment> findCommentsByReviewId(Long reviewId) {
        return reviewCommentRepository.findByReviewIdOrderByCreatedAtDesc(reviewId);
    }

    public List<ReviewReply> findRepliesByCommentIds(List<Long> commentIds) {
        if (commentIds.isEmpty()) {
            return List.of();
        }
        return reviewReplyRepository.findByCommentIdInAndIsHiddenFalseOrderByCreatedAtAsc(commentIds);
    }

    public Map<Long, MemberWithProfileImageResult> findMemberWithProfileImagesByIds(Collection<Long> memberIds) {
        return memberQueryService.findMemberWithProfileImagesByIds(memberIds);
    }

    public List<MemberReviewCountResult> countReviewsByMemberWithPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        return reviewRepository.countReviewsByMemberWithPeriod(startDate, endDate);
    }

    public Page<LatestReviewListItemResult> findLatestReviewsByPlaceId(Long placeId, Integer rating, int page, int size, Boolean hasImage, String sortType) {
        return reviewRepository.findLatestReviewsByPlaceId(placeId, rating, PageRequest.of(page, size), hasImage, sortType);
    }

    public Page<LatestReviewListItemResult> findLatestReviewsByProductId(Long productId, Integer rating, int page, int size, Boolean hasImage, String sortType) {
        return reviewRepository.findLatestReviewsByProductId(productId, rating, PageRequest.of(page, size), hasImage, sortType);
    }
}
