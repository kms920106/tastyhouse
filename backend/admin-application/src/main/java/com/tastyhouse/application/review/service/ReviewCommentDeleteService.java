package com.tastyhouse.application.review.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.review.vo.ReviewCommentId;
import com.tastyhouse.application.review.port.in.ReviewCommentDeleteCommand;
import com.tastyhouse.application.review.port.in.ReviewCommentDeleteUseCase;
import com.tastyhouse.application.review.port.out.write.ReviewCommentPersistencePort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional
class ReviewCommentDeleteService implements ReviewCommentDeleteUseCase {

    private final ReviewCommentPersistencePort reviewCommentPersistencePort;

    public ReviewCommentDeleteService(ReviewCommentPersistencePort reviewCommentPersistencePort) {
        this.reviewCommentPersistencePort = reviewCommentPersistencePort;
    }

    @Override
    public void deleteComment(ReviewCommentDeleteCommand command) {
        Long commentId = command.commentId();
        ReviewCommentId reviewCommentId = ReviewCommentId.of(commentId);
        reviewCommentPersistencePort.findById(reviewCommentId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.REVIEW_COMMENT_NOT_FOUND));

        reviewCommentPersistencePort.deleteById(reviewCommentId);
    }
}
