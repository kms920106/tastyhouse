package com.tastyhouse.application.review.store;

import com.tastyhouse.application.review.port.out.write.ReviewReplyState;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.review.model.ReviewReply;
import com.tastyhouse.domain.review.vo.ReviewCommentId;

final class ReviewReplyStateMapper {
    private ReviewReplyStateMapper() {
    }

    static ReviewReply toDomain(ReviewReplyState state) {
        return ReviewReply.reconstitute(
            state.id(),
            state.commentId() == null ? null : ReviewCommentId.of(state.commentId()),
            state.memberId() == null ? null : MemberId.of(state.memberId()),
            state.replyToMemberId() == null ? null : MemberId.of(state.replyToMemberId()),
            state.content(),
            state.hidden(),
            state.createdAt()
        );
    }

    static ReviewReplyState toState(ReviewReply reply) {
        return new ReviewReplyState(
            reply.getId(),
            reply.getCommentId() == null ? null : reply.getCommentId().value(),
            reply.getMemberId() == null ? null : reply.getMemberId().value(),
            reply.getReplyToMemberId() == null ? null : reply.getReplyToMemberId().value(),
            reply.getContent(),
            reply.isHidden(),
            reply.getCreatedAt()
        );
    }
}
