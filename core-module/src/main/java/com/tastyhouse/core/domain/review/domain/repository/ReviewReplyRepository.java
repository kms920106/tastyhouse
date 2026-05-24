package com.tastyhouse.core.domain.review.domain.repository;

import com.tastyhouse.core.domain.review.domain.model.ReviewReply;

import java.util.List;

public interface ReviewReplyRepository {

    List<ReviewReply> findByCommentIdAndIsHiddenFalseOrderByCreatedAtAsc(Long commentId);

    List<ReviewReply> findByCommentIdInAndIsHiddenFalseOrderByCreatedAtAsc(List<Long> commentIds);

    ReviewReply save(ReviewReply reply);
}
