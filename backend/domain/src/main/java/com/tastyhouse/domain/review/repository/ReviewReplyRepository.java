package com.tastyhouse.domain.review.repository;

import java.util.Optional;

import com.tastyhouse.domain.review.model.ReviewReply;
import com.tastyhouse.domain.review.vo.ReviewReplyId;

public interface ReviewReplyRepository {
    Optional<ReviewReply> findById(ReviewReplyId replyId);

    ReviewReply save(ReviewReply reply);

    void deleteById(ReviewReplyId replyId);
}
