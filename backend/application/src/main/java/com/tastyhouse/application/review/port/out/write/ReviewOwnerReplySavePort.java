package com.tastyhouse.application.review.port.out.write;

import com.tastyhouse.domain.review.model.ReviewOwnerReply;

public interface ReviewOwnerReplySavePort {

    ReviewOwnerReply save(ReviewOwnerReply reviewOwnerReply);

    void delete(ReviewOwnerReply reviewOwnerReply);
}
