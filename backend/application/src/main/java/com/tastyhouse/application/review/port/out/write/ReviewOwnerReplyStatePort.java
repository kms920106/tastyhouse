package com.tastyhouse.application.review.port.out.write;

import java.util.Optional;

public interface ReviewOwnerReplyStatePort {
    Optional<ReviewOwnerReplyState> findById(Long id);

    Optional<ReviewOwnerReplyState> findByReviewId(Long reviewId);

    boolean existsByReviewId(Long reviewId);

    ReviewOwnerReplyState save(ReviewOwnerReplyState state);

    void delete(ReviewOwnerReplyState state);
}
