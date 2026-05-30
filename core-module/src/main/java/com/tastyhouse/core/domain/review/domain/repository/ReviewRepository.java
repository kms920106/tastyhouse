package com.tastyhouse.core.domain.review.domain.repository;

import com.tastyhouse.core.domain.rank.application.dto.result.MemberReviewCountResult;
import com.tastyhouse.core.domain.review.application.dto.result.BestReviewListItemResult;
import com.tastyhouse.core.domain.review.application.dto.result.LatestReviewListItemResult;
import com.tastyhouse.core.domain.review.application.dto.result.MyReviewListItemResult;
import com.tastyhouse.core.domain.review.application.dto.result.ReviewDetailResult;
import com.tastyhouse.core.domain.review.application.dto.result.SearchReviewItemResult;
import com.tastyhouse.core.domain.review.domain.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ReviewRepository {

    Page<BestReviewListItemResult> findBestReviews(Pageable pageable);

    Page<SearchReviewItemResult> searchByKeyword(String keyword, Pageable pageable);

    Page<LatestReviewListItemResult> findLatestReviews(Pageable pageable);

    Page<LatestReviewListItemResult> findLatestReviewsByFollowing(List<Long> followingMemberIds, Pageable pageable);

    Page<LatestReviewListItemResult> findLatestReviewsByShopId(Long shopId, Integer rating, Pageable pageable, Boolean hasImage, String sortType);

    List<LatestReviewListItemResult> findReviewsByShopIdAndRating(Long shopId, Integer rating, int limit);

    Page<LatestReviewListItemResult> findLatestReviewsByProductId(Long productId, Integer rating, Pageable pageable, Boolean hasImage, String sortType);

    List<LatestReviewListItemResult> findReviewsByProductIdAndRating(Long productId, Integer rating, int limit);

    List<MemberReviewCountResult> countReviewsByMemberWithPeriod(LocalDateTime startDate, LocalDateTime endDate);

    Optional<ReviewDetailResult> findReviewDetail(Long reviewId);

    Page<MyReviewListItemResult> findMyReviews(Long memberId, Pageable pageable);

    Page<MyReviewListItemResult> findReviewsByMemberId(Long memberId, Pageable pageable);

    Long countByShopIdAndIsHiddenFalse(Long shopId);

    Long countWillRevisit(Long shopId);

    Double getAverageTasteRating(Long shopId);

    Double getAverageAmountRating(Long shopId);

    Double getAveragePriceRating(Long shopId);

    Double getAverageAtmosphereRating(Long shopId);

    Double getAverageKindnessRating(Long shopId);

    Double getAverageHygieneRating(Long shopId);

    Map<Integer, Long> getRatingCounts(Long shopId);

    Map<Integer, Long> getMonthlyReviewCounts(Long shopId, int year);

    Long countByProductIdAndIsHiddenFalse(Long productId);

    Double getAverageTasteRatingByProductId(Long productId);

    Double getAverageAmountRatingByProductId(Long productId);

    Double getAveragePriceRatingByProductId(Long productId);

    Optional<Review> findById(Long reviewId);

    Optional<Review> findByIdAndMemberId(Long reviewId, Long memberId);

    long countVisibleReviewsByMemberId(Long memberId);

    boolean existsByOrderIdAndProductIdAndMemberId(Long orderId, Long productId, Long memberId);

    Review save(Review review);

    void deleteById(Long reviewId);
}
