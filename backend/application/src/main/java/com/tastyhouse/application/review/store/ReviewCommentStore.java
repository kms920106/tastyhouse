package com.tastyhouse.application.review.store;

import java.util.Optional;

import com.tastyhouse.domain.review.model.ReviewComment;
import com.tastyhouse.domain.review.vo.ReviewCommentId;
import com.tastyhouse.application.review.port.out.write.ReviewCommentStatePort;

public class ReviewCommentStore implements ReviewCommentRepository {
    private final ReviewCommentStatePort reviewCommentStatePort;

    public ReviewCommentStore(ReviewCommentStatePort reviewCommentStatePort) {
        this.reviewCommentStatePort = reviewCommentStatePort;
    }

    @Override
    public Optional<ReviewComment> findById(ReviewCommentId commentId) {
        return reviewCommentStatePort.findById(commentId.value()).map(ReviewCommentStateMapper::toDomain);
    }

    @Override
    public ReviewComment save(ReviewComment comment) {
        return ReviewCommentStateMapper.toDomain(reviewCommentStatePort.save(ReviewCommentStateMapper.toState(comment)));
    }

    @Override
    public void deleteById(ReviewCommentId commentId) {
        reviewCommentStatePort.deleteById(commentId.value());
    }
}
