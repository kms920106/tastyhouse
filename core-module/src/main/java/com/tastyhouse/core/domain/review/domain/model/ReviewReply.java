package com.tastyhouse.core.domain.review.domain.model;

import com.tastyhouse.core.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Getter
@Entity
@Table(name = "REVIEW_REPLY")
public class ReviewReply extends BaseEntity {

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
    private Boolean isHidden = false;

    protected ReviewReply() {
    }

    public ReviewReply(Long commentId, Long memberId, String content) {
        this(commentId, memberId, null, content);
    }

    public ReviewReply(Long commentId, Long memberId, Long replyToMemberId, String content) {
        this.commentId = commentId;
        this.memberId = memberId;
        this.replyToMemberId = replyToMemberId;
        this.content = content;
    }
}
