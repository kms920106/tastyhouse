package com.tastyhouse.core.domain.review.application;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.review.domain.model.Review;
import com.tastyhouse.core.domain.review.domain.model.ReviewComment;
import com.tastyhouse.core.domain.review.domain.model.ReviewReply;
import com.tastyhouse.core.domain.review.domain.repository.ReviewCommentRepository;
import com.tastyhouse.core.domain.review.domain.repository.ReviewLikeRepository;
import com.tastyhouse.core.domain.review.domain.repository.ReviewReplyRepository;
import com.tastyhouse.core.domain.review.domain.repository.ReviewRepository;
import com.tastyhouse.core.domain.review.domain.repository.ReviewTagRepository;
import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.review.domain.vo.ReviewCommentId;
import com.tastyhouse.core.domain.review.domain.vo.ReviewId;
import com.tastyhouse.core.domain.shop.domain.repository.TagRepository;
import com.tastyhouse.core.domain.member.domain.repository.MemberRepository;
import com.tastyhouse.core.domain.member.follow.domain.repository.MemberFollowRepository;
import com.tastyhouse.core.domain.rank.application.dto.result.MemberReviewCountResult;
import com.tastyhouse.core.domain.review.application.dto.ReviewSearchCondition;
import com.tastyhouse.core.domain.review.application.dto.result.BestReviewListItemResult;
import com.tastyhouse.core.domain.review.application.dto.result.LatestReviewListItemResult;
import com.tastyhouse.core.domain.review.application.dto.result.MyReviewListItemResult;
import com.tastyhouse.core.domain.review.application.dto.result.ProductReviewStatisticsResult;
import com.tastyhouse.core.domain.review.application.dto.result.ReviewCommentListItemResult;
import com.tastyhouse.core.domain.review.application.dto.result.ReviewDetailResult;
import com.tastyhouse.core.domain.review.application.dto.result.ReviewListItemResult;
import com.tastyhouse.core.domain.review.application.dto.result.ReviewManagementDetailResult;
import com.tastyhouse.core.domain.review.application.dto.result.ReviewReplyListItemResult;
import com.tastyhouse.core.domain.review.application.dto.result.ReviewsByRatingResult;
import com.tastyhouse.core.domain.review.application.dto.result.ShopReviewStatisticsResult;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReviewQueryService {

    private final ReviewRepository reviewRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final ReviewTagRepository reviewTagRepository;
    private final ReviewCommentRepository reviewCommentRepository;
    private final ReviewReplyRepository reviewReplyRepository;
    private final MemberRepository memberRepository;
    private final MemberFollowRepository memberFollowRepository;
    private final TagRepository tagRepository;

    public PageResult<BestReviewListItemResult> findBestReviewsWithPagination(int page, int size) {
        PageQuery pageQuery = PageQuery.of(page, size);
        return reviewRepository.findBestReviews(pageQuery);
    }

    public Review findById(ReviewId id) {
        return reviewRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.REVIEW_NOT_FOUND));
    }

    public Optional<ReviewDetailResult> findReviewDetail(ReviewId reviewId) {
        return reviewRepository.findReviewDetail(reviewId).map(result -> {
            List<Long> tagIds = reviewTagRepository.findTagIdsByReviewId(reviewId.value());
            if (!tagIds.isEmpty()) {
                List<String> tagNames = tagRepository.findTagNamesByIds(tagIds);
                return result.withTagNames(tagNames);
            }
            return result;
        });
    }

    public boolean isLikedByMember(ReviewId reviewId, MemberId memberId) {
        return reviewLikeRepository.existsByReviewIdAndMemberId(reviewId, memberId);
    }

    public PageResult<LatestReviewListItemResult> findLatestReviewsWithPagination(int page, int size) {
        PageQuery pageQuery = PageQuery.of(page, size);
        return reviewRepository.findLatestReviews(pageQuery);
    }

    public PageResult<LatestReviewListItemResult> findLatestReviewsByFollowingWithPagination(MemberId memberId, int page, int size) {
        List<Long> followingMemberIds = memberFollowRepository.findFollowingIdsByFollowerId(memberId);

        if (followingMemberIds.isEmpty()) {
            return PageResult.empty(page, size);
        }

        PageQuery pageQuery = PageQuery.of(page, size);
        return reviewRepository.findLatestReviewsByFollowing(followingMemberIds, pageQuery);
    }

    public ReviewsByRatingResult findShopReviewsByRating(Long shopId, int page, int size, Boolean hasImage) {
        List<LatestReviewListItemResult> rating1Reviews = reviewRepository.findReviewsByShopIdAndRating(shopId, 1, 5);
        List<LatestReviewListItemResult> rating2Reviews = reviewRepository.findReviewsByShopIdAndRating(shopId, 2, 5);
        List<LatestReviewListItemResult> rating3Reviews = reviewRepository.findReviewsByShopIdAndRating(shopId, 3, 5);
        List<LatestReviewListItemResult> rating4Reviews = reviewRepository.findReviewsByShopIdAndRating(shopId, 4, 5);
        List<LatestReviewListItemResult> rating5Reviews = reviewRepository.findReviewsByShopIdAndRating(shopId, 5, 5);

        PageQuery pageQuery = PageQuery.of(page, size);
        PageResult<LatestReviewListItemResult> allReviewsPage = reviewRepository.findLatestReviewsByShopId(shopId, null, pageQuery, hasImage, "LATEST");

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

        PageQuery pageQuery = PageQuery.of(page, size);
        PageResult<LatestReviewListItemResult> allReviewsPage = reviewRepository.findLatestReviewsByProductId(productId, null, pageQuery, hasImage, "LATEST");

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

    public long countVisibleReviewsByMemberId(MemberId memberId) {
        return reviewRepository.countVisibleReviewsByMemberId(memberId);
    }

    public PageResult<MyReviewListItemResult> findMyReviews(MemberId memberId, int page, int size) {
        PageQuery pageQuery = PageQuery.of(page, size);
        return reviewRepository.findMyReviews(memberId, pageQuery);
    }

    public PageResult<MyReviewListItemResult> findReviewsByMemberId(MemberId memberId, int page, int size) {
        PageQuery pageQuery = PageQuery.of(page, size);
        return reviewRepository.findReviewsByMemberId(memberId, pageQuery);
    }

    public boolean isReviewedByOrderAndProduct(Long orderId, Long productId, MemberId memberId) {
        return reviewRepository.existsByOrderIdAndProductIdAndMemberId(orderId, productId, memberId);
    }

    public List<ReviewComment> findCommentsByReviewId(ReviewId reviewId) {
        return reviewCommentRepository.findByReviewIdOrderByCreatedAtDesc(reviewId);
    }

    public List<ReviewReply> findRepliesByCommentIds(List<ReviewCommentId> commentIds) {
        if (commentIds.isEmpty()) {
            return List.of();
        }
        return reviewReplyRepository.findByCommentIdInAndHiddenFalseOrderByCreatedAtAsc(commentIds);
    }

    public Map<Long, String> findNicknamesByIds(Collection<Long> memberIds) {
        return memberRepository.findNicknamesByIds(memberIds);
    }

    public List<MemberReviewCountResult> countReviewsByMemberWithPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        return reviewRepository.countReviewsByMemberWithPeriod(startDate, endDate);
    }

    public PageResult<ReviewListItemResult> findReviews(ReviewSearchCondition condition, int page, int size) {
        PageQuery pageQuery = PageQuery.of(page, size);
        return reviewRepository.findReviews(condition, pageQuery);
    }

    public Optional<ReviewManagementDetailResult> findReviewManagementDetail(ReviewId reviewId) {
        return reviewRepository.findReviewManagementDetail(reviewId).map(result -> {
            List<Long> tagIds = reviewTagRepository.findTagIdsByReviewId(reviewId.value());
            if (!tagIds.isEmpty()) {
                List<String> tagNames = tagRepository.findTagNamesByIds(tagIds);
                return result.withTagNames(tagNames);
            }
            return result;
        });
    }

    public List<ReviewCommentListItemResult> findCommentsIncludingHidden(ReviewId reviewId) {
        List<ReviewComment> comments = reviewCommentRepository.findByReviewIdOrderByCreatedAtDesc(reviewId);
        Map<Long, String> nicknameMap = findNicknamesByIds(
            comments.stream().map(comment -> comment.getMemberId().value()).toList()
        );

        return comments.stream()
            .map(comment -> ReviewCommentListItemResult.of(
                comment.getId(),
                comment.getMemberId(),
                nicknameOf(nicknameMap, comment.getMemberId().value()),
                comment.getContent(),
                comment.isHidden(),
                comment.getCreatedAt()
            ))
            .toList();
    }

    public List<ReviewReplyListItemResult> findRepliesIncludingHidden(List<ReviewCommentId> commentIds) {
        if (commentIds.isEmpty()) {
            return List.of();
        }
        List<ReviewReply> replies = reviewReplyRepository.findByCommentIdInOrderByCreatedAtAsc(commentIds);

        List<Long> memberIds = new ArrayList<>();
        for (ReviewReply reply : replies) {
            memberIds.add(reply.getMemberId().value());
            if (reply.getReplyToMemberId() != null) {
                memberIds.add(reply.getReplyToMemberId().value());
            }
        }
        Map<Long, String> nicknameMap = findNicknamesByIds(memberIds);

        return replies.stream()
            .map(reply -> ReviewReplyListItemResult.of(
                reply.getId(),
                reply.getCommentId(),
                reply.getMemberId(),
                nicknameOf(nicknameMap, reply.getMemberId().value()),
                reply.getReplyToMemberId(),
                reply.getReplyToMemberId() != null ? nicknameOf(nicknameMap, reply.getReplyToMemberId().value()) : null,
                reply.getContent(),
                reply.isHidden(),
                reply.getCreatedAt()
            ))
            .toList();
    }

    private String nicknameOf(Map<Long, String> nicknameMap, Long memberId) {
        return nicknameMap.get(memberId);
    }
}
