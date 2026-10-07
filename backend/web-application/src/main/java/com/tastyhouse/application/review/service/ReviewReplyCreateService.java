package com.tastyhouse.application.review.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.review.model.ReviewReply;
import com.tastyhouse.domain.review.vo.ReviewCommentId;
import com.tastyhouse.application.review.port.in.ReviewReplyCreateCommand;
import com.tastyhouse.application.review.port.in.ReviewReplyCreateUseCase;
import com.tastyhouse.application.review.port.out.write.ReviewReplyPersistencePort;

@Service
@Transactional
class ReviewReplyCreateService implements ReviewReplyCreateUseCase {

    private final ReviewReplyPersistencePort reviewReplyPersistencePort;

    public ReviewReplyCreateService(ReviewReplyPersistencePort reviewReplyPersistencePort) {
        this.reviewReplyPersistencePort = reviewReplyPersistencePort;
    }

    @Override
    public Long createReply(ReviewReplyCreateCommand command) {
        Long replyToMemberId = command.replyToMemberId();
        ReviewCommentId reviewCommentId = ReviewCommentId.of(command.commentId());
        ReviewReply reply = reviewReplyPersistencePort.save(ReviewReply.of(
            reviewCommentId,
            MemberId.of(command.memberId()),
            replyToMemberId == null ? null : MemberId.of(replyToMemberId),
            command.content()
        ));

        return reply.getId();
    }
}
