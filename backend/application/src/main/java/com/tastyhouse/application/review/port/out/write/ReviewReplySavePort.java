package com.tastyhouse.application.review.port.out.write;

import com.tastyhouse.domain.review.model.ReviewReply;
import com.tastyhouse.domain.review.vo.ReviewReplyId;

public interface ReviewReplySavePort {

    ReviewReply save(ReviewReply reply);

    void deleteById(ReviewReplyId replyId);
}
