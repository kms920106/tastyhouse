package com.tastyhouse.application.review.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.review.model.Review;
import com.tastyhouse.domain.review.model.ReviewComment;
import com.tastyhouse.domain.review.model.ReviewReply;
import com.tastyhouse.domain.review.vo.ReviewCommentId;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.domain.review.vo.ReviewReplyId;
import com.tastyhouse.application.review.port.in.ReviewCommentDeleteCommand;
import com.tastyhouse.application.review.port.in.ReviewCommentHiddenChangeCommand;
import com.tastyhouse.application.review.port.in.ReviewHiddenChangeCommand;
import com.tastyhouse.application.review.port.in.ReviewManagementCommandUseCase;
import com.tastyhouse.application.review.port.in.ReviewManagementDeleteCommand;
import com.tastyhouse.application.review.port.in.ReviewReplyDeleteCommand;
import com.tastyhouse.application.review.port.in.ReviewReplyHiddenChangeCommand;
import com.tastyhouse.application.review.port.out.write.ReviewCommentPersistencePort;
import com.tastyhouse.application.review.port.out.write.ReviewPersistencePort;
import com.tastyhouse.application.review.port.out.write.ReviewReplyPersistencePort;

@Service
@Transactional
class ReviewManagementCommandService implements ReviewManagementCommandUseCase {

    private final ReviewLifecycleService reviewLifecycleService;
    private final ReviewPersistencePort reviewPersistencePort;
    private final ReviewCommentPersistencePort reviewCommentPersistencePort;
    private final ReviewReplyPersistencePort reviewReplyPersistencePort;

    public ReviewManagementCommandService(
        ReviewLifecycleService reviewLifecycleService,
        ReviewPersistencePort reviewPersistencePort,
        ReviewCommentPersistencePort reviewCommentPersistencePort,
        ReviewReplyPersistencePort reviewReplyPersistencePort
    ) {
        this.reviewLifecycleService = reviewLifecycleService;
        this.reviewPersistencePort = reviewPersistencePort;
        this.reviewCommentPersistencePort = reviewCommentPersistencePort;
        this.reviewReplyPersistencePort = reviewReplyPersistencePort;
    }

    @Override
    public void changeReviewHidden(ReviewHiddenChangeCommand command) {
        Long id = command.reviewId();
        boolean hidden = command.hidden();
        ReviewId reviewId = ReviewId.of(id);
        Review review = reviewPersistencePort.findById(reviewId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REVIEW_NOT_FOUND));

        if (hidden) {
            review.hide();
        } else {
            review.unhide();
        }

        reviewPersistencePort.save(review);
    }

    @Override
    public void deleteReview(ReviewManagementDeleteCommand command) {
        Long id = command.reviewId();
        ReviewId reviewId = ReviewId.of(id);
        reviewLifecycleService.remove(reviewId);
    }

    @Override
    public void changeCommentHidden(ReviewCommentHiddenChangeCommand command) {
        Long commentId = command.commentId();
        boolean hidden = command.hidden();
        ReviewCommentId reviewCommentId = ReviewCommentId.of(commentId);
        ReviewComment comment = reviewCommentPersistencePort.findById(reviewCommentId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REVIEW_COMMENT_NOT_FOUND));

        if (hidden) {
            comment.hide();
        } else {
            comment.unhide();
        }

        reviewCommentPersistencePort.save(comment);
    }

    @Override
    public void deleteComment(ReviewCommentDeleteCommand command) {
        Long commentId = command.commentId();
        ReviewCommentId reviewCommentId = ReviewCommentId.of(commentId);
        reviewCommentPersistencePort.findById(reviewCommentId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REVIEW_COMMENT_NOT_FOUND));

        reviewCommentPersistencePort.deleteById(reviewCommentId);
    }

    @Override
    public void changeReplyHidden(ReviewReplyHiddenChangeCommand command) {
        Long replyId = command.replyId();
        boolean hidden = command.hidden();
        ReviewReplyId reviewReplyId = ReviewReplyId.of(replyId);
        ReviewReply reply = reviewReplyPersistencePort.findById(reviewReplyId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REVIEW_REPLY_NOT_FOUND));

        if (hidden) {
            reply.hide();
        } else {
            reply.unhide();
        }

        reviewReplyPersistencePort.save(reply);
    }

    @Override
    public void deleteReply(ReviewReplyDeleteCommand command) {
        Long replyId = command.replyId();
        ReviewReplyId reviewReplyId = ReviewReplyId.of(replyId);
        reviewReplyPersistencePort.findById(reviewReplyId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REVIEW_REPLY_NOT_FOUND));

        reviewReplyPersistencePort.deleteById(reviewReplyId);
    }
}
