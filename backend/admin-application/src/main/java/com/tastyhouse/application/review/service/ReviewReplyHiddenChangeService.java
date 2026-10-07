package com.tastyhouse.application.review.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.review.model.ReviewReply;
import com.tastyhouse.domain.review.vo.ReviewReplyId;
import com.tastyhouse.application.review.port.in.ReviewReplyHiddenChangeCommand;
import com.tastyhouse.application.review.port.in.ReviewReplyHiddenChangeUseCase;
import com.tastyhouse.application.review.port.out.write.ReviewReplyPersistencePort;
import com.tastyhouse.application.shared.exception.AdminErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional
class ReviewReplyHiddenChangeService implements ReviewReplyHiddenChangeUseCase {

    private final ReviewReplyPersistencePort reviewReplyPersistencePort;

    public ReviewReplyHiddenChangeService(ReviewReplyPersistencePort reviewReplyPersistencePort) {
        this.reviewReplyPersistencePort = reviewReplyPersistencePort;
    }

    @Override
    public void changeReplyHidden(ReviewReplyHiddenChangeCommand command) {
        Long replyId = command.replyId();
        boolean hidden = command.hidden();
        ReviewReplyId reviewReplyId = ReviewReplyId.of(replyId);
        ReviewReply reply = reviewReplyPersistencePort.findById(reviewReplyId)
            .orElseThrow(() -> new ResourceNotFoundException(AdminErrorCode.REVIEW_REPLY_NOT_FOUND));

        if (hidden) {
            reply.hide();
        } else {
            reply.unhide();
        }

        reviewReplyPersistencePort.save(reply);
    }
}
