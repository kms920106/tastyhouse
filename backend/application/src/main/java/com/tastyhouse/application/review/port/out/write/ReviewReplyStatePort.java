package com.tastyhouse.application.review.port.out.write;

import java.util.Optional;

public interface ReviewReplyStatePort {
    Optional<ReviewReplyState> findById(Long id);

    ReviewReplyState save(ReviewReplyState state);

    void deleteById(Long id);
}
