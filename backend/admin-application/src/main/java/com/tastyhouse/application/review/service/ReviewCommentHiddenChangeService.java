package com.tastyhouse.application.review.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.review.model.ReviewComment;
import com.tastyhouse.domain.review.vo.ReviewCommentId;
import com.tastyhouse.application.review.port.in.ReviewCommentHiddenChangeCommand;
import com.tastyhouse.application.review.port.in.ReviewCommentHiddenChangeUseCase;
import com.tastyhouse.application.review.port.out.write.ReviewCommentLoadPort;
import com.tastyhouse.application.review.port.out.write.ReviewCommentSavePort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional
class ReviewCommentHiddenChangeService implements ReviewCommentHiddenChangeUseCase {

    private final ReviewCommentLoadPort reviewCommentLoadPort;
    private final ReviewCommentSavePort reviewCommentSavePort;

    public ReviewCommentHiddenChangeService(ReviewCommentLoadPort reviewCommentLoadPort, ReviewCommentSavePort reviewCommentSavePort) {
        this.reviewCommentLoadPort = reviewCommentLoadPort;
        this.reviewCommentSavePort = reviewCommentSavePort;
    }

    @Override
    public void changeCommentHidden(ReviewCommentHiddenChangeCommand command) {
        Long commentId = command.commentId();
        boolean hidden = command.hidden();
        ReviewCommentId reviewCommentId = ReviewCommentId.of(commentId);
        ReviewComment comment = reviewCommentLoadPort.findById(reviewCommentId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.REVIEW_COMMENT_NOT_FOUND));

        if (hidden) {
            comment.hide();
        } else {
            comment.unhide();
        }

        reviewCommentSavePort.save(comment);
    }
}
