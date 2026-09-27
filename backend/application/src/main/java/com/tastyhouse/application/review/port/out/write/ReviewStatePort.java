package com.tastyhouse.application.review.port.out.write;

import java.util.Optional;

public interface ReviewStatePort {
    Optional<ReviewState> findById(Long id);

    Optional<ReviewState> findByIdAndMemberId(Long id, Long memberId);

    boolean existsByOrderIdAndProductId(Long orderId, Long productId);

    ReviewState save(ReviewState state);

    void deleteById(Long id);
}
