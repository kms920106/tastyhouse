package com.tastyhouse.core.repository.review;

import com.tastyhouse.core.entity.review.ReviewReply;

import java.util.List;

public interface ReviewReplyRepository {

    List<ReviewReply> findByCommentIdAndIsHiddenFalseOrderByCreatedAtAsc(Long commentId);

    List<ReviewReply> findByCommentIdInAndIsHiddenFalseOrderByCreatedAtAsc(List<Long> commentIds);

    ReviewReply save(ReviewReply reply);
}
