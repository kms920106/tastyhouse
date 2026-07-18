package com.tastyhouse.core.domain.review.domain.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.core.domain.review.domain.model.ReviewReply;
import com.tastyhouse.core.domain.review.domain.vo.ReviewCommentId;
import com.tastyhouse.core.domain.review.domain.vo.ReviewReplyId;

public interface ReviewReplyRepository {

    List<ReviewReply> findByCommentIdInAndHiddenFalseOrderByCreatedAtAsc(List<ReviewCommentId> commentIds);

    List<ReviewReply> findByCommentIdInOrderByCreatedAtAsc(List<ReviewCommentId> commentIds);

    Optional<ReviewReply> findById(ReviewReplyId replyId);

    ReviewReply save(ReviewReply reply);

    void deleteById(ReviewReplyId replyId);
}
