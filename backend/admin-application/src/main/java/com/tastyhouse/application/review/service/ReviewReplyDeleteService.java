package com.tastyhouse.application.review.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.review.vo.ReviewReplyId;
import com.tastyhouse.application.review.port.in.ReviewReplyDeleteCommand;
import com.tastyhouse.application.review.port.in.ReviewReplyDeleteUseCase;
import com.tastyhouse.application.review.port.out.write.ReviewReplyPersistencePort;
import com.tastyhouse.application.shared.exception.AdminErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional
class ReviewReplyDeleteService implements ReviewReplyDeleteUseCase {

    private final ReviewReplyPersistencePort reviewReplyPersistencePort;

    public ReviewReplyDeleteService(ReviewReplyPersistencePort reviewReplyPersistencePort) {
        this.reviewReplyPersistencePort = reviewReplyPersistencePort;
    }

    @Override
    public void deleteReply(ReviewReplyDeleteCommand command) {
        Long replyId = command.replyId();
        ReviewReplyId reviewReplyId = ReviewReplyId.of(replyId);
        reviewReplyPersistencePort.findById(reviewReplyId)
            .orElseThrow(() -> new ResourceNotFoundException(AdminErrorCode.REVIEW_REPLY_NOT_FOUND));

        reviewReplyPersistencePort.deleteById(reviewReplyId);
    }
}
