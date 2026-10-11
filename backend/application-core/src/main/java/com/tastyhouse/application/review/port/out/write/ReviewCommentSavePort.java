package com.tastyhouse.application.review.port.out.write;

import com.tastyhouse.domain.review.model.ReviewComment;
import com.tastyhouse.domain.review.vo.ReviewCommentId;

public interface ReviewCommentSavePort {

    ReviewComment save(ReviewComment comment);

    void deleteById(ReviewCommentId commentId);
}
