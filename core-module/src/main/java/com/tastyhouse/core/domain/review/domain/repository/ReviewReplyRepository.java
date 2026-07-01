package com.tastyhouse.core.domain.review.domain.repository;

import java.util.List;

import com.tastyhouse.core.domain.review.domain.model.ReviewReply;

public interface ReviewReplyRepository {

    List<ReviewReply> findByCommentIdInAndHiddenFalseOrderByCreatedAtAsc(List<Long> commentIds);

    ReviewReply save(ReviewReply reply);
}
