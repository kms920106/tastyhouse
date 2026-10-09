package com.tastyhouse.application.review.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.review.model.ReviewReply;
import com.tastyhouse.domain.review.vo.ReviewReplyId;
import com.tastyhouse.application.review.port.in.ReviewReplyHiddenChangeCommand;
import com.tastyhouse.application.review.port.in.ReviewReplyHiddenChangeUseCase;
import com.tastyhouse.application.review.port.out.write.ReviewReplyLoadPort;
import com.tastyhouse.application.review.port.out.write.ReviewReplySavePort;
import com.tastyhouse.application.shared.exception.AdminErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional
class ReviewReplyHiddenChangeService implements ReviewReplyHiddenChangeUseCase {

    private final ReviewReplyLoadPort reviewReplyLoadPort;
    private final ReviewReplySavePort reviewReplySavePort;

    public ReviewReplyHiddenChangeService(ReviewReplyLoadPort reviewReplyLoadPort, ReviewReplySavePort reviewReplySavePort) {
        this.reviewReplyLoadPort = reviewReplyLoadPort;
        this.reviewReplySavePort = reviewReplySavePort;
    }

    @Override
    public void changeReplyHidden(ReviewReplyHiddenChangeCommand command) {
        Long replyId = command.replyId();
        boolean hidden = command.hidden();
        ReviewReplyId reviewReplyId = ReviewReplyId.of(replyId);
        ReviewReply reply = reviewReplyLoadPort.findById(reviewReplyId)
            .orElseThrow(() -> new ResourceNotFoundException(AdminErrorCode.REVIEW_REPLY_NOT_FOUND));

        if (hidden) {
            reply.hide();
        } else {
            reply.unhide();
        }

        reviewReplySavePort.save(reply);
    }
}
