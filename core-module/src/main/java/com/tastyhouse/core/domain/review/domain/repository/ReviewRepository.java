package com.tastyhouse.core.domain.review.domain.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.tastyhouse.core.domain.rank.application.dto.result.MemberReviewCountResult;
import com.tastyhouse.core.domain.review.application.dto.result.BestReviewListItemResult;
import com.tastyhouse.core.domain.review.application.dto.result.LatestReviewListItemResult;
import com.tastyhouse.core.domain.review.application.dto.result.MyReviewListItemResult;
import com.tastyhouse.core.domain.review.application.dto.result.ReviewDetailResult;
import com.tastyhouse.core.domain.review.application.dto.result.SearchReviewItemResult;
import com.tastyhouse.core.domain.review.domain.model.Review;
import com.tastyhouse.core.domain.review.domain.vo.ReviewId;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

public interface ReviewRepository {

    PageResult<BestReviewListItemResult> findBestReviews(PageQuery pageQuery);

    PageResult<SearchReviewItemResult> searchByKeyword(String keyword, PageQuery pageQuery);

    PageResult<LatestReviewListItemResult> findLatestReviews(PageQuery pageQuery);

    PageResult<LatestReviewListItemResult> findLatestReviewsByFollowing(List<Long> followingMemberIds, PageQuery pageQuery);

    PageResult<LatestReviewListItemResult> findLatestReviewsByShopId(Long shopId, Integer rating, PageQuery pageQuery, Boolean hasImage, String sortType);

    List<LatestReviewListItemResult> findReviewsByShopIdAndRating(Long shopId, Integer rating, int limit);

    PageResult<LatestReviewListItemResult> findLatestReviewsByProductId(Long productId, Integer rating, PageQuery pageQuery, Boolean hasImage, String sortType);

    List<LatestReviewListItemResult> findReviewsByProductIdAndRating(Long productId, Integer rating, int limit);

    List<MemberReviewCountResult> countReviewsByMemberWithPeriod(LocalDateTime startDate, LocalDateTime endDate);

    Optional<ReviewDetailResult> findReviewDetail(ReviewId reviewId);

    PageResult<MyReviewListItemResult> findMyReviews(Long memberId, PageQuery pageQuery);

    PageResult<MyReviewListItemResult> findReviewsByMemberId(Long memberId, PageQuery pageQuery);

    Long countByShopIdAndHiddenFalse(Long shopId);

    Long countWillRevisit(Long shopId);

    Double getAverageTasteRating(Long shopId);

    Double getAverageAmountRating(Long shopId);

    Double getAveragePriceRating(Long shopId);

    Double getAverageAtmosphereRating(Long shopId);

    Double getAverageKindnessRating(Long shopId);

    Double getAverageHygieneRating(Long shopId);

    Map<Integer, Long> getRatingCounts(Long shopId);

    Map<Integer, Long> getMonthlyReviewCounts(Long shopId, int year);

    Long countByProductIdAndHiddenFalse(Long productId);

    Double getAverageTasteRatingByProductId(Long productId);

    Double getAverageAmountRatingByProductId(Long productId);

    Double getAveragePriceRatingByProductId(Long productId);

    Optional<Review> findById(ReviewId reviewId);

    Optional<Review> findByIdAndMemberId(ReviewId reviewId, Long memberId);

    long countVisibleReviewsByMemberId(Long memberId);

    boolean existsByOrderIdAndProductIdAndMemberId(Long orderId, Long productId, Long memberId);

    Review save(Review review);

    void deleteById(ReviewId reviewId);
}
