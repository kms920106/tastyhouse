package com.tastyhouse.application.review.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.review.vo.ReviewCommentId;
import com.tastyhouse.application.review.port.in.ReviewCommentDeleteCommand;
import com.tastyhouse.application.review.port.in.ReviewCommentDeleteUseCase;
import com.tastyhouse.application.review.port.out.write.ReviewCommentLoadPort;
import com.tastyhouse.application.review.port.out.write.ReviewCommentSavePort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional
class ReviewCommentDeleteService implements ReviewCommentDeleteUseCase {

    private final ReviewCommentLoadPort reviewCommentLoadPort;
    private final ReviewCommentSavePort reviewCommentSavePort;

    public ReviewCommentDeleteService(ReviewCommentLoadPort reviewCommentLoadPort, ReviewCommentSavePort reviewCommentSavePort) {
        this.reviewCommentLoadPort = reviewCommentLoadPort;
        this.reviewCommentSavePort = reviewCommentSavePort;
    }

    @Override
    public void deleteComment(ReviewCommentDeleteCommand command) {
        Long commentId = command.commentId();
        ReviewCommentId reviewCommentId = ReviewCommentId.of(commentId);
        reviewCommentLoadPort.findById(reviewCommentId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.REVIEW_COMMENT_NOT_FOUND));

        reviewCommentSavePort.deleteById(reviewCommentId);
    }
}
