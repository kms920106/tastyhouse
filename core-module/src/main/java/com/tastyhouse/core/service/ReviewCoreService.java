package com.tastyhouse.core.service;

import com.tastyhouse.core.common.ReviewsByRatingResult;
import com.tastyhouse.core.entity.rank.dto.MemberReviewCountDto;
import com.tastyhouse.core.entity.review.Review;
import com.tastyhouse.core.entity.review.dto.MyReviewListItemDto;
import com.tastyhouse.core.entity.review.ReviewComment;
import com.tastyhouse.core.entity.review.ReviewImage;
import com.tastyhouse.core.entity.review.ReviewReply;
import com.tastyhouse.core.entity.review.ReviewTag;
import com.tastyhouse.core.entity.review.dto.BestReviewListItemDto;
import com.tastyhouse.core.entity.review.dto.LatestReviewListItemDto;
import com.tastyhouse.core.entity.review.dto.PlaceReviewStatisticsDto;
import com.tastyhouse.core.entity.review.dto.ReviewDetailDto;
import com.tastyhouse.core.entity.place.Tag;
import com.tastyhouse.core.domain.follow.application.FollowQueryService;
import com.tastyhouse.core.domain.member.application.MemberQueryService;
import com.tastyhouse.core.domain.member.application.dto.result.MemberWithProfileImageResult;
import com.tastyhouse.core.repository.place.TagRepository;
import com.tastyhouse.core.repository.review.ReviewCommentRepository;
import com.tastyhouse.core.repository.review.ReviewImageRepository;
import com.tastyhouse.core.repository.review.ReviewLikeRepository;
import com.tastyhouse.core.repository.review.ReviewReplyRepository;
import com.tastyhouse.core.repository.review.ReviewRepository;
import com.tastyhouse.core.repository.review.ReviewTagRepository;
import com.tastyhouse.core.entity.review.ReviewLike;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewCoreService {

    private final ReviewRepository reviewRepository;
    private final FollowQueryService followQueryService;
    private final ReviewTagRepository reviewTagRepository;
    private final TagRepository tagRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final ReviewCommentRepository reviewCommentRepository;
    private final ReviewReplyRepository reviewReplyRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final MemberQueryService memberQueryService;

    @Transactional(readOnly = true)
    public Page<BestReviewListItemDto> findBestReviewsWithPagination(int page, int size) {
        return reviewRepository.findBestReviews(PageRequest.of(page, size));
    }

    @Transactional(readOnly = true)
    public Review findById(Long id) {
        return reviewRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.REVIEW_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public Optional<ReviewDetailDto> findReviewDetail(Long reviewId) {
        return reviewRepository.findReviewDetail(reviewId).map(dto -> {
            List<Long> tagIds = reviewTagRepository.findTagIdsByReviewId(reviewId);
            if (!tagIds.isEmpty()) {
                List<String> tagNames = tagRepository.findTagNamesByIds(tagIds);
                return dto.withTagNames(tagNames);
            }
            return dto;
        });
    }

    @Transactional(readOnly = true)
    public boolean isLikedByMember(Long reviewId, Long memberId) {
        return reviewLikeRepository.existsByReviewIdAndMemberId(reviewId, memberId);
    }

    @Transactional(readOnly = true)
    public Page<LatestReviewListItemDto> findLatestReviewsWithPagination(int page, int size) {
        return reviewRepository.findLatestReviews(PageRequest.of(page, size));
    }

    @Transactional(readOnly = true)
    public Page<LatestReviewListItemDto> findLatestReviewsByFollowingWithPagination(Long memberId, int page, int size) {
        List<Long> followingMemberIds = followQueryService.findFollowingIds(memberId);

        PageRequest pageRequest = PageRequest.of(page, size);
        if (followingMemberIds.isEmpty()) {
            return new PageImpl<>(new java.util.ArrayList<>(), pageRequest, 0);
        }

        return reviewRepository.findLatestReviewsByFollowing(followingMemberIds, pageRequest);
    }

    @Transactional(readOnly = true)
    public ReviewsByRatingResult findPlaceReviewsByRating(Long placeId, int page, int size) {
        List<LatestReviewListItemDto> rating1Reviews = reviewRepository.findReviewsByPlaceIdAndRating(placeId, 1, 5);
        List<LatestReviewListItemDto> rating2Reviews = reviewRepository.findReviewsByPlaceIdAndRating(placeId, 2, 5);
        List<LatestReviewListItemDto> rating3Reviews = reviewRepository.findReviewsByPlaceIdAndRating(placeId, 3, 5);
        List<LatestReviewListItemDto> rating4Reviews = reviewRepository.findReviewsByPlaceIdAndRating(placeId, 4, 5);
        List<LatestReviewListItemDto> rating5Reviews = reviewRepository.findReviewsByPlaceIdAndRating(placeId, 5, 5);

        PageRequest pageRequest = PageRequest.of(page, size);
        Page<LatestReviewListItemDto> allReviewsPage = reviewRepository.findLatestReviewsByPlaceId(placeId, null, pageRequest, null, "LATEST");
        List<LatestReviewListItemDto> allReviews = allReviewsPage.getContent();

        Long totalReviewCount = reviewRepository.countByPlaceIdAndIsHiddenFalse(placeId);

        Map<Integer, List<LatestReviewListItemDto>> reviewsByRating = new HashMap<>();
        reviewsByRating.put(1, rating1Reviews);
        reviewsByRating.put(2, rating2Reviews);
        reviewsByRating.put(3, rating3Reviews);
        reviewsByRating.put(4, rating4Reviews);
        reviewsByRating.put(5, rating5Reviews);

        return new ReviewsByRatingResult(
            reviewsByRating,
            allReviews,
            totalReviewCount,
            allReviewsPage.getTotalElements(),
            allReviewsPage.getTotalPages(),
            allReviewsPage.getNumber(),
            allReviewsPage.getSize()
        );
    }

    @Transactional
    public boolean toggleReviewLike(Long reviewId, Long memberId) {
        boolean alreadyLiked = reviewLikeRepository.existsByReviewIdAndMemberId(reviewId, memberId);

        if (alreadyLiked) {
            reviewLikeRepository.deleteByReviewIdAndMemberId(reviewId, memberId);
            return false;
        } else {
            ReviewLike reviewLike = new ReviewLike(reviewId, memberId);
            reviewLikeRepository.save(reviewLike);
            return true;
        }
    }

    @Transactional
    public ReviewComment createComment(Long reviewId, Long memberId, String content) {
        ReviewComment comment = new ReviewComment(reviewId, memberId, content);
        return reviewCommentRepository.save(comment);
    }

    @Transactional
    public ReviewReply createReply(Long commentId, Long memberId, Long replyToMemberId, String content) {
        ReviewReply reply = new ReviewReply(commentId, memberId, replyToMemberId, content);
        return reviewReplyRepository.save(reply);
    }

    @Transactional(readOnly = true)
    public List<ReviewComment> findCommentsByReviewId(Long reviewId) {
        return reviewCommentRepository.findByReviewIdOrderByCreatedAtDesc(reviewId);
    }

    @Transactional(readOnly = true)
    public List<ReviewReply> findRepliesByCommentIds(List<Long> commentIds) {
        if (commentIds.isEmpty()) {
            return List.of();
        }
        return reviewReplyRepository.findByCommentIdInAndIsHiddenFalseOrderByCreatedAtAsc(commentIds);
    }

    @Transactional(readOnly = true)
    public PlaceReviewStatisticsDto findPlaceReviewStatistics(Long placeId) {
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

            return new PlaceReviewStatisticsDto(
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

        return new PlaceReviewStatisticsDto(
                totalCount,
                null, null, null, null, null, null, null,
                ratingMap,
                null
        );
    }

    @Transactional(readOnly = true)
    public ReviewsByRatingResult findProductReviewsByRating(Long productId, int page, int size) {
        List<LatestReviewListItemDto> rating1Reviews = reviewRepository.findReviewsByProductIdAndRating(productId, 1, 5);
        List<LatestReviewListItemDto> rating2Reviews = reviewRepository.findReviewsByProductIdAndRating(productId, 2, 5);
        List<LatestReviewListItemDto> rating3Reviews = reviewRepository.findReviewsByProductIdAndRating(productId, 3, 5);
        List<LatestReviewListItemDto> rating4Reviews = reviewRepository.findReviewsByProductIdAndRating(productId, 4, 5);
        List<LatestReviewListItemDto> rating5Reviews = reviewRepository.findReviewsByProductIdAndRating(productId, 5, 5);

        PageRequest pageRequest = PageRequest.of(page, size);
        Page<LatestReviewListItemDto> allReviewsPage = reviewRepository.findLatestReviewsByProductId(productId, null, pageRequest, null, "LATEST");
        List<LatestReviewListItemDto> allReviews = allReviewsPage.getContent();

        Long totalReviewCount = reviewRepository.countByProductIdAndIsHiddenFalse(productId);

        Map<Integer, List<LatestReviewListItemDto>> reviewsByRating = new HashMap<>();
        reviewsByRating.put(1, rating1Reviews);
        reviewsByRating.put(2, rating2Reviews);
        reviewsByRating.put(3, rating3Reviews);
        reviewsByRating.put(4, rating4Reviews);
        reviewsByRating.put(5, rating5Reviews);

        return new ReviewsByRatingResult(
            reviewsByRating,
            allReviews,
            totalReviewCount,
            allReviewsPage.getTotalElements(),
            allReviewsPage.getTotalPages(),
            allReviewsPage.getNumber(),
            allReviewsPage.getSize()
        );
    }

    @Transactional(readOnly = true)
    public long countVisibleReviewsByMemberId(Long memberId) {
        return reviewRepository.countVisibleReviewsByMemberId(memberId);
    }

    @Transactional(readOnly = true)
    public Page<MyReviewListItemDto> findMyReviews(Long memberId, int page, int size) {
        return reviewRepository.findMyReviews(memberId, PageRequest.of(page, size));
    }

    @Transactional(readOnly = true)
    public Page<MyReviewListItemDto> findReviewsByMemberId(Long memberId, int page, int size) {
        return reviewRepository.findReviewsByMemberId(memberId, PageRequest.of(page, size));
    }

    @Transactional(readOnly = true)
    public boolean isReviewedByOrderAndProduct(Long orderId, Long productId, Long memberId) {
        return reviewRepository.existsByOrderIdAndProductIdAndMemberId(orderId, productId, memberId);
    }

    @Transactional
    public Review saveReview(Review review) {
        return reviewRepository.save(review);
    }

    @Transactional
    public void saveReviewImages(List<ReviewImage> images) {
        reviewImageRepository.saveAll(images);
    }

    @Transactional
    public void saveReviewTags(List<ReviewTag> tags) {
        reviewTagRepository.saveAll(tags);
    }

    @Transactional(readOnly = true)
    public Optional<Review> findReviewByIdAndMemberId(Long reviewId, Long memberId) {
        return reviewRepository.findByIdAndMemberId(reviewId, memberId);
    }

    @Transactional
    public void deleteReviewImages(Long reviewId) {
        reviewImageRepository.deleteByReviewId(reviewId);
    }

    @Transactional
    public void deleteReviewTags(Long reviewId) {
        reviewTagRepository.deleteByReviewId(reviewId);
    }

    @Transactional
    public void deleteReview(Long reviewId) {
        reviewRepository.deleteById(reviewId);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> findProductReviewStatistics(Long productId) {
        Map<String, Object> statistics = new HashMap<>();

        Long totalCount = reviewRepository.countByProductIdAndIsHiddenFalse(productId);
        statistics.put("totalReviewCount", totalCount);

        if (totalCount > 0) {
            statistics.put("averageTasteRating", reviewRepository.getAverageTasteRatingByProductId(productId));
            statistics.put("averageAmountRating", reviewRepository.getAverageAmountRatingByProductId(productId));
            statistics.put("averagePriceRating", reviewRepository.getAveragePriceRatingByProductId(productId));
        }

        return statistics;
    }

    @Transactional(readOnly = true)
    public Map<Long, MemberWithProfileImageResult> findMemberWithProfileImagesByIds(Collection<Long> memberIds) {
        return memberQueryService.findMemberWithProfileImagesByIds(memberIds);
    }

    @Transactional
    public Tag findOrCreateTag(String tagName) {
        return tagRepository.findByTagName(tagName)
            .orElseGet(() -> tagRepository.save(new Tag(tagName)));
    }

    @Transactional(readOnly = true)
    public List<MemberReviewCountDto> countReviewsByMemberWithPeriod(
        LocalDateTime startDate,
        LocalDateTime endDate
    ) {
        return reviewRepository.countReviewsByMemberWithPeriod(startDate, endDate);
    }
}
