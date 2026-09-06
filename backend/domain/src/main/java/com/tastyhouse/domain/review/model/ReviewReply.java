package com.tastyhouse.domain.review.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.review.vo.ReviewCommentId;

public class ReviewReply {
    private final Long id;
    private final ReviewCommentId commentId;
    private final MemberId memberId;
    private final MemberId replyToMemberId;
    private final String content;
    private boolean hidden;
    private final LocalDateTime createdAt;

    private ReviewReply(
        Long id,
        ReviewCommentId commentId,
        MemberId memberId,
        MemberId replyToMemberId,
        String content,
        boolean hidden,
        LocalDateTime createdAt
    ) {
        this.id = id;
        this.commentId = commentId;
        this.memberId = memberId;
        this.replyToMemberId = replyToMemberId;
        this.content = content;
        this.hidden = hidden;
        this.createdAt = createdAt;
    }

    public static ReviewReply of(ReviewCommentId commentId, MemberId memberId, MemberId replyToMemberId, String content) {
        return new ReviewReply(null, commentId, memberId, replyToMemberId, content, false, null);
    }

    public static ReviewReply reconstitute(
        Long id,
        ReviewCommentId commentId,
        MemberId memberId,
        MemberId replyToMemberId,
        String content,
        boolean hidden,
        LocalDateTime createdAt
    ) {
        return new ReviewReply(id, commentId, memberId, replyToMemberId, content, hidden, createdAt);
    }

    public void hide() {
        this.hidden = true;
    }

    public void unhide() {
        this.hidden = false;
    }

    public Long getId() {
        return this.id;
    }

    public ReviewCommentId getCommentId() {
        return this.commentId;
    }

    public MemberId getMemberId() {
        return this.memberId;
    }

    public MemberId getReplyToMemberId() {
        return this.replyToMemberId;
    }

    public String getContent() {
        return this.content;
    }

    public boolean isHidden() {
        return this.hidden;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }
}
