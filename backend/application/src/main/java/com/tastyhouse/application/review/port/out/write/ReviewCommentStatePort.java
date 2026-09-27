package com.tastyhouse.application.review.port.out.write;

import java.util.Optional;

public interface ReviewCommentStatePort {
    Optional<ReviewCommentState> findById(Long id);

    ReviewCommentState save(ReviewCommentState state);

    void deleteById(Long id);
}
