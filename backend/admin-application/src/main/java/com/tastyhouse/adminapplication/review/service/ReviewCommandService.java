package com.tastyhouse.adminapplication.review.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.adminapplication.review.port.in.ReviewCommandUseCase;
import com.tastyhouse.adminapplication.review.port.in.ReviewCommentDeleteCommand;
import com.tastyhouse.adminapplication.review.port.in.ReviewCommentHiddenChangeCommand;
import com.tastyhouse.adminapplication.review.port.in.ReviewDeleteCommand;
import com.tastyhouse.adminapplication.review.port.in.ReviewHiddenChangeCommand;
import com.tastyhouse.adminapplication.review.port.in.ReviewReplyDeleteCommand;
import com.tastyhouse.adminapplication.review.port.in.ReviewReplyHiddenChangeCommand;
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

/**
 * 리뷰 관리 명령 서비스(admin).
 *
 * <p>리뷰 삭제는 이미지·태그를 함께 정리해야 하는 크로스 애그리거트 불변식이라 도메인 서비스
 * {@link ReviewLifecycleService}에 위임하고, 숨김 전환·댓글/답글 삭제처럼 단일 애그리거트 상태 전이는
 * write 포트를 직접 다룬다.
 *
 * <p>도메인 모델이 순수 POJO라 더티 체킹이 없으므로 상태 전이 후 명시적으로 {@code save}를 호출한다.
 *
 * <p>조회 전용 동작은 {@link ReviewQueryService}로 분리했다(CQRS).
 */
@Service
@Transactional
public class ReviewCommandService implements ReviewCommandUseCase {

    private final ReviewLifecycleService reviewLifecycleService;
    private final ReviewRepository reviewRepository;
    private final ReviewCommentRepository reviewCommentRepository;
    private final ReviewReplyRepository reviewReplyRepository;

    public ReviewCommandService(
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

    /**
     * 리뷰 숨김/노출 전환.
     */
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

    /**
     * 리뷰 삭제(관리자) — 소유권 검증 없이 이미지·태그까지 함께 정리한다.
     */
    @Override
    public void deleteReview(ReviewDeleteCommand command) {
        Long id = command.reviewId();
        ReviewId reviewId = ReviewId.of(id);
        reviewLifecycleService.remove(reviewId);
    }

    /**
     * 댓글 숨김/노출 전환.
     */
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

    /**
     * 댓글 삭제.
     */
    @Override
    public void deleteComment(ReviewCommentDeleteCommand command) {
        Long commentId = command.commentId();
        ReviewCommentId reviewCommentId = ReviewCommentId.of(commentId);
        reviewCommentRepository.findById(reviewCommentId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REVIEW_COMMENT_NOT_FOUND));

        reviewCommentRepository.deleteById(reviewCommentId);
    }

    /**
     * 답글 숨김/노출 전환.
     */
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

    /**
     * 답글 삭제.
     */
    @Override
    public void deleteReply(ReviewReplyDeleteCommand command) {
        Long replyId = command.replyId();
        ReviewReplyId reviewReplyId = ReviewReplyId.of(replyId);
        reviewReplyRepository.findById(reviewReplyId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REVIEW_REPLY_NOT_FOUND));

        reviewReplyRepository.deleteById(reviewReplyId);
    }
}
