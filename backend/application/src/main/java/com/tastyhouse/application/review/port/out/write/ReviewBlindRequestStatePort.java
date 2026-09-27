package com.tastyhouse.application.review.port.out.write;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReviewBlindRequestStatePort {
    Optional<ReviewBlindRequestState> findById(Long id);

    boolean existsByReviewIdAndStatus(Long reviewId, String status);

    boolean existsByReviewIdAndStatusIn(Long reviewId, Collection<String> statuses);

    List<ReviewBlindRequestState> findByStatusExpiringBefore(String status, LocalDateTime now);

    Optional<ReviewBlindRequestState> findLatestByReviewIdAndStatus(Long reviewId, String status);

    ReviewBlindRequestState save(ReviewBlindRequestState state);
}
