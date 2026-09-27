package com.tastyhouse.application.review.store;

import java.util.Optional;

import com.tastyhouse.domain.review.model.ReviewOwnerReply;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.domain.review.vo.ReviewOwnerReplyId;
import com.tastyhouse.application.review.port.out.write.ReviewOwnerReplyStatePort;

public class ReviewOwnerReplyStore implements ReviewOwnerReplyRepository {
    private final ReviewOwnerReplyStatePort reviewOwnerReplyStatePort;

    public ReviewOwnerReplyStore(ReviewOwnerReplyStatePort reviewOwnerReplyStatePort) {
        this.reviewOwnerReplyStatePort = reviewOwnerReplyStatePort;
    }

    @Override
    public Optional<ReviewOwnerReply> findById(ReviewOwnerReplyId reviewOwnerReplyId) {
        return reviewOwnerReplyStatePort.findById(reviewOwnerReplyId.value())
            .map(ReviewOwnerReplyStateMapper::toDomain);
    }

    @Override
    public Optional<ReviewOwnerReply> findByReviewId(ReviewId reviewId) {
        return reviewOwnerReplyStatePort.findByReviewId(reviewId.value())
            .map(ReviewOwnerReplyStateMapper::toDomain);
    }

    @Override
    public boolean existsByReviewId(ReviewId reviewId) {
        return reviewOwnerReplyStatePort.existsByReviewId(reviewId.value());
    }

    @Override
    public ReviewOwnerReply save(ReviewOwnerReply reviewOwnerReply) {
        return ReviewOwnerReplyStateMapper.toDomain(
            reviewOwnerReplyStatePort.save(ReviewOwnerReplyStateMapper.toState(reviewOwnerReply)));
    }

    @Override
    public void delete(ReviewOwnerReply reviewOwnerReply) {
        reviewOwnerReplyStatePort.delete(ReviewOwnerReplyStateMapper.toState(reviewOwnerReply));
    }
}
