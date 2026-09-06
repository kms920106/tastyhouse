package com.tastyhouse.infrastructure.review.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(name = "REVIEW_REPLY")
public class ReviewReplyJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "comment_id", nullable = false)
    private Long commentId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "reply_to_member_id")
    private Long replyToMemberId;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "is_hidden", nullable = false)
    private boolean hidden;

    protected ReviewReplyJpaEntity() {
    }

    private ReviewReplyJpaEntity(
        Long commentId,
        Long memberId,
        Long replyToMemberId,
        String content,
        boolean hidden
    ) {
        this.commentId = commentId;
        this.memberId = memberId;
        this.replyToMemberId = replyToMemberId;
        this.content = content;
        this.hidden = hidden;
    }

    static ReviewReplyJpaEntity create(
        Long commentId,
        Long memberId,
        Long replyToMemberId,
        String content,
        boolean hidden
    ) {
        return new ReviewReplyJpaEntity(commentId, memberId, replyToMemberId, content, hidden);
    }

    void applyChanges(boolean hidden) {
        this.hidden = hidden;
    }

    public Long getId() {
        return this.id;
    }

    public Long getCommentId() {
        return this.commentId;
    }

    public Long getMemberId() {
        return this.memberId;
    }

    public Long getReplyToMemberId() {
        return this.replyToMemberId;
    }

    public String getContent() {
        return this.content;
    }

    public boolean isHidden() {
        return this.hidden;
    }
}
