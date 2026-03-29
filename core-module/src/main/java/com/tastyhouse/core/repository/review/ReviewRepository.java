package com.tastyhouse.core.repository.review;

import com.tastyhouse.core.entity.rank.dto.MemberReviewCountDto;
import com.tastyhouse.core.entity.review.Review;
import com.tastyhouse.core.entity.review.dto.BestReviewListItemDto;
import com.tastyhouse.core.entity.review.dto.LatestReviewListItemDto;
import com.tastyhouse.core.entity.review.dto.MyReviewListItemDto;
import com.tastyhouse.core.entity.review.dto.ReviewDetailDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ReviewRepository {

    Page<BestReviewListItemDto> findBestReviews(Pageable pageable);

    Page<LatestReviewListItemDto> findLatestReviews(Pageable pageable);

    Page<LatestReviewListItemDto> findLatestReviewsByFollowing(List<Long> followingMemberIds, Pageable pageable);

    Page<LatestReviewListItemDto> findLatestReviewsByPlaceId(Long placeId, Integer rating, Pageable pageable, Boolean hasImage, String sortType);

    List<LatestReviewListItemDto> findReviewsByPlaceIdAndRating(Long placeId, Integer rating, int limit);

    Page<LatestReviewListItemDto> findLatestReviewsByProductId(Long productId, Integer rating, Pageable pageable, Boolean hasImage, String sortType);

    List<LatestReviewListItemDto> findReviewsByProductIdAndRating(Long productId, Integer rating, int limit);

    List<MemberReviewCountDto> countReviewsByMemberWithPeriod(
        LocalDateTime startDate,
        LocalDateTime endDate
    );

    Optional<ReviewDetailDto> findReviewDetail(Long reviewId);

    Page<MyReviewListItemDto> findMyReviews(Long memberId, Pageable pageable);

    Page<MyReviewListItemDto> findReviewsByMemberId(Long memberId, Pageable pageable);

    // 장소 리뷰 조회 (페이징)
    Page<Review> findPlaceReviewsByRating(Long placeId, Double rating, Pageable pageable);

    Page<Review> findPlaceReviews(Long placeId, Pageable pageable);

    // 장소 리뷰 통계
    Long countByPlaceIdAndIsHiddenFalse(Long placeId);

    Long countWillRevisit(Long placeId);

    Double getAverageTasteRating(Long placeId);

    Double getAverageAmountRating(Long placeId);

    Double getAveragePriceRating(Long placeId);

    Double getAverageAtmosphereRating(Long placeId);

    Double getAverageKindnessRating(Long placeId);

    Double getAverageHygieneRating(Long placeId);

    Map<Integer, Long> getRatingCounts(Long placeId);

    Map<Integer, Long> getMonthlyReviewCounts(Long placeId, int year);

    // 상품 리뷰 조회 (페이징)
    Page<Review> findProductReviewsByRating(Long productId, Double rating, Pageable pageable);

    Page<Review> findProductReviews(Long productId, Pageable pageable);

    // 상품 리뷰 통계
    Long countByProductIdAndIsHiddenFalse(Long productId);

    Double getAverageTasteRatingByProductId(Long productId);

    Double getAverageAmountRatingByProductId(Long productId);

    Double getAveragePriceRatingByProductId(Long productId);

    // 회원 리뷰 조회
    Optional<Review> findById(Long reviewId);

    Optional<Review> findByIdAndMemberId(Long reviewId, Long memberId);

    long countByMemberIdAndIsHiddenFalse(Long memberId);

    boolean existsByOrderIdAndProductIdAndMemberId(Long orderId, Long productId, Long memberId);

    Review save(Review review);

    void deleteById(Long reviewId);
}
