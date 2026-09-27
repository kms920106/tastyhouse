package com.tastyhouse.application.review.store;

import java.util.Optional;

import com.tastyhouse.application.review.port.out.write.ReviewReplyStatePort;
import com.tastyhouse.domain.review.model.ReviewReply;
import com.tastyhouse.domain.review.vo.ReviewReplyId;

public class ReviewReplyStore implements ReviewReplyRepository {
    private final ReviewReplyStatePort reviewReplyStatePort;

    public ReviewReplyStore(ReviewReplyStatePort reviewReplyStatePort) {
        this.reviewReplyStatePort = reviewReplyStatePort;
    }

    @Override
    public Optional<ReviewReply> findById(ReviewReplyId replyId) {
        return reviewReplyStatePort.findById(replyId.value()).map(ReviewReplyStateMapper::toDomain);
    }

    @Override
    public ReviewReply save(ReviewReply reply) {
        return ReviewReplyStateMapper.toDomain(reviewReplyStatePort.save(ReviewReplyStateMapper.toState(reply)));
    }

    @Override
    public void deleteById(ReviewReplyId replyId) {
        reviewReplyStatePort.deleteById(replyId.value());
    }
}
