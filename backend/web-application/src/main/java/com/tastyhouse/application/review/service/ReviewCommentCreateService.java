package com.tastyhouse.application.review.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.review.model.ReviewComment;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.application.review.port.in.ReviewCommentCreateCommand;
import com.tastyhouse.application.review.port.in.ReviewCommentCreateUseCase;
import com.tastyhouse.application.review.port.out.write.ReviewCommentPersistencePort;

@Service
@Transactional
class ReviewCommentCreateService implements ReviewCommentCreateUseCase {

    private final ReviewCommentPersistencePort reviewCommentPersistencePort;

    public ReviewCommentCreateService(ReviewCommentPersistencePort reviewCommentPersistencePort) {
        this.reviewCommentPersistencePort = reviewCommentPersistencePort;
    }

    @Override
    public Long createComment(ReviewCommentCreateCommand command) {
        ReviewId targetReviewId = ReviewId.of(command.reviewId());
        ReviewComment comment = reviewCommentPersistencePort.save(
            ReviewComment.of(targetReviewId, MemberId.of(command.memberId()), command.content())
        );
        return comment.getId();
    }
}
