package com.tastyhouse.core.domain.review.domain.repository;

import java.util.List;

import com.tastyhouse.core.domain.review.domain.model.ReviewReply;
import com.tastyhouse.core.domain.review.domain.vo.ReviewCommentId;

public interface ReviewReplyRepository {

    List<ReviewReply> findByCommentIdInAndHiddenFalseOrderByCreatedAtAsc(List<ReviewCommentId> commentIds);

    ReviewReply save(ReviewReply reply);
}
