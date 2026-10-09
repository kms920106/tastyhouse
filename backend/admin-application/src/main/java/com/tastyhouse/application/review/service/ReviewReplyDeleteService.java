package com.tastyhouse.application.review.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.review.vo.ReviewReplyId;
import com.tastyhouse.application.review.port.in.ReviewReplyDeleteCommand;
import com.tastyhouse.application.review.port.in.ReviewReplyDeleteUseCase;
import com.tastyhouse.application.review.port.out.write.ReviewReplyLoadPort;
import com.tastyhouse.application.review.port.out.write.ReviewReplySavePort;
import com.tastyhouse.application.shared.exception.AdminErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional
class ReviewReplyDeleteService implements ReviewReplyDeleteUseCase {

    private final ReviewReplyLoadPort reviewReplyLoadPort;
    private final ReviewReplySavePort reviewReplySavePort;

    public ReviewReplyDeleteService(ReviewReplyLoadPort reviewReplyLoadPort, ReviewReplySavePort reviewReplySavePort) {
        this.reviewReplyLoadPort = reviewReplyLoadPort;
        this.reviewReplySavePort = reviewReplySavePort;
    }

    @Override
    public void deleteReply(ReviewReplyDeleteCommand command) {
        Long replyId = command.replyId();
        ReviewReplyId reviewReplyId = ReviewReplyId.of(replyId);
        reviewReplyLoadPort.findById(reviewReplyId)
            .orElseThrow(() -> new ResourceNotFoundException(AdminErrorCode.REVIEW_REPLY_NOT_FOUND));

        reviewReplySavePort.deleteById(reviewReplyId);
    }
}
