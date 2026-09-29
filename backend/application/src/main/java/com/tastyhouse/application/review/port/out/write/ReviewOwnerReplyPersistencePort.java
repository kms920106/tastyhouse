package com.tastyhouse.application.review.port.out.write;

import java.util.Optional;

import com.tastyhouse.domain.review.model.ReviewOwnerReply;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.domain.review.vo.ReviewOwnerReplyId;

public interface ReviewOwnerReplyPersistencePort {
    Optional<ReviewOwnerReply> findById(ReviewOwnerReplyId reviewOwnerReplyId);

    Optional<ReviewOwnerReply> findByReviewId(ReviewId reviewId);

    boolean existsByReviewId(ReviewId reviewId);

    ReviewOwnerReply save(ReviewOwnerReply reviewOwnerReply);

    void delete(ReviewOwnerReply reviewOwnerReply);
}
