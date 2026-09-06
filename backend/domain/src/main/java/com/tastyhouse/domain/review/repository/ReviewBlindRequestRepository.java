package com.tastyhouse.domain.review.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.review.model.ReviewBlindRequest;
import com.tastyhouse.domain.review.model.ReviewBlindStatus;
import com.tastyhouse.domain.review.vo.ReviewBlindRequestId;
import com.tastyhouse.domain.review.vo.ReviewId;

public interface ReviewBlindRequestRepository {
    Optional<ReviewBlindRequest> findById(ReviewBlindRequestId reviewBlindRequestId);

    boolean existsByReviewIdAndStatus(ReviewId reviewId, ReviewBlindStatus status);

    boolean existsTerminatedByReviewId(ReviewId reviewId);

    List<ReviewBlindRequest> findExpirableBlinds(LocalDateTime now);

    Optional<ReviewBlindRequest> findApprovedByReviewId(ReviewId reviewId);

    ReviewBlindRequest save(ReviewBlindRequest reviewBlindRequest);
}
