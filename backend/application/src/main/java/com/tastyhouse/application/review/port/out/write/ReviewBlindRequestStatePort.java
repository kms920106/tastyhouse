package com.tastyhouse.application.review.port.out.write;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReviewBlindRequestStatePort {
    Optional<ReviewBlindRequestState> findById(Long id);

    boolean existsByReviewIdAndStatus(Long reviewId, String status);

    boolean existsTerminatedByReviewId(Long reviewId);

    List<ReviewBlindRequestState> findExpirableBlinds(LocalDateTime now);

    Optional<ReviewBlindRequestState> findApprovedByReviewId(Long reviewId);

    ReviewBlindRequestState save(ReviewBlindRequestState state);
}
