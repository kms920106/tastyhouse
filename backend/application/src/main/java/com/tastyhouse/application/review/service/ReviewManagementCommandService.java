package com.tastyhouse.application.review.service;

import com.tastyhouse.application.shared.marker.AdminApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.review.port.in.ReviewManagementCommandUseCase;
import com.tastyhouse.application.review.port.in.ReviewCommentDeleteCommand;
import com.tastyhouse.application.review.port.in.ReviewCommentHiddenChangeCommand;
import com.tastyhouse.application.review.port.in.ReviewManagementDeleteCommand;
import com.tastyhouse.application.review.port.in.ReviewHiddenChangeCommand;
import com.tastyhouse.application.review.port.in.ReviewReplyDeleteCommand;
import com.tastyhouse.application.review.port.in.ReviewReplyHiddenChangeCommand;
import com.tastyhouse.domain.review.model.ReviewComment;
import com.tastyhouse.domain.review.model.ReviewReply;
import com.tastyhouse.domain.review.model.Review;
import com.tastyhouse.domain.review.repository.ReviewCommentRepository;
import com.tastyhouse.domain.review.repository.ReviewReplyRepository;
import com.tastyhouse.domain.review.repository.ReviewRepository;
import com.tastyhouse.domain.review.service.ReviewLifecycleService;
import com.tastyhouse.domain.review.vo.ReviewCommentId;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.domain.review.vo.ReviewReplyId;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

@Service
@AdminApp
@Transactional
public class ReviewManagementCommandService implements ReviewManagementCommandUseCase {

    private final ReviewLifecycleService reviewLifecycleService;
    private final ReviewRepository reviewRepository;
    private final ReviewCommentRepository reviewCommentRepository;
    private final ReviewReplyRepository reviewReplyRepository;

    public ReviewManagementCommandService(
        ReviewLifecycleService reviewLifecycleService,
        ReviewRepository reviewRepository,
        ReviewCommentRepository reviewCommentRepository,
        ReviewReplyRepository reviewReplyRepository
    ) {
        this.reviewLifecycleService = reviewLifecycleService;
        this.reviewRepository = reviewRepository;
        this.reviewCommentRepository = reviewCommentRepository;
        this.reviewReplyRepository = reviewReplyRepository;
    }

    @Override
    public void changeReviewHidden(ReviewHiddenChangeCommand command) {
        Long id = command.reviewId();
        boolean hidden = command.hidden();
        ReviewId reviewId = ReviewId.of(id);
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REVIEW_NOT_FOUND));

        if (hidden) {
            review.hide();
        } else {
            review.unhide();
        }

        reviewRepository.save(review);
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
        ReviewComment comment = reviewCommentRepository.findById(reviewCommentId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REVIEW_COMMENT_NOT_FOUND));

        if (hidden) {
            comment.hide();
        } else {
            comment.unhide();
        }

        reviewCommentRepository.save(comment);
    }

    @Override
    public void deleteComment(ReviewCommentDeleteCommand command) {
        Long commentId = command.commentId();
        ReviewCommentId reviewCommentId = ReviewCommentId.of(commentId);
        reviewCommentRepository.findById(reviewCommentId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REVIEW_COMMENT_NOT_FOUND));

        reviewCommentRepository.deleteById(reviewCommentId);
    }

    @Override
    public void changeReplyHidden(ReviewReplyHiddenChangeCommand command) {
        Long replyId = command.replyId();
        boolean hidden = command.hidden();
        ReviewReplyId reviewReplyId = ReviewReplyId.of(replyId);
        ReviewReply reply = reviewReplyRepository.findById(reviewReplyId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REVIEW_REPLY_NOT_FOUND));

        if (hidden) {
            reply.hide();
        } else {
            reply.unhide();
        }

        reviewReplyRepository.save(reply);
    }

    @Override
    public void deleteReply(ReviewReplyDeleteCommand command) {
        Long replyId = command.replyId();
        ReviewReplyId reviewReplyId = ReviewReplyId.of(replyId);
        reviewReplyRepository.findById(reviewReplyId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REVIEW_REPLY_NOT_FOUND));

        reviewReplyRepository.deleteById(reviewReplyId);
    }
}
