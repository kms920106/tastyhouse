package com.tastyhouse.application.review.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.review.vo.ReviewCommentId;
import com.tastyhouse.application.review.port.in.ReviewCommentLookupUseCase;
import com.tastyhouse.application.review.port.out.write.ReviewCommentPersistencePort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional
class ReviewCommentLookupService implements ReviewCommentLookupUseCase {

    private final ReviewCommentPersistencePort reviewCommentPersistencePort;

    public ReviewCommentLookupService(ReviewCommentPersistencePort reviewCommentPersistencePort) {
        this.reviewCommentPersistencePort = reviewCommentPersistencePort;
    }

    @Override
    public Long findReviewIdOfComment(Long commentId) {
        return reviewCommentPersistencePort.findById(ReviewCommentId.of(commentId))
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.REVIEW_COMMENT_NOT_FOUND))
            .getReviewId()
            .value();
    }
}
