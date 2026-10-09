package com.tastyhouse.application.review.port.out.write;

import java.util.Optional;

import com.tastyhouse.domain.review.model.ReviewReply;
import com.tastyhouse.domain.review.vo.ReviewReplyId;

public interface ReviewReplyLoadPort {

    Optional<ReviewReply> findById(ReviewReplyId replyId);
}
