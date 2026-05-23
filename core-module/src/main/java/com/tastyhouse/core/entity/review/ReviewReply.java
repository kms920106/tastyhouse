package com.tastyhouse.core.entity.review;

import com.tastyhouse.core.entity.BaseEntity;
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
    private Long id; // PK

    @Column(name = "comment_id", nullable = false)
    private Long commentId; // 댓글 ID (REVIEW_COMMENT.id 참조)

    @Column(name = "member_id", nullable = false)
    private Long memberId; // 답글 작성자 회원 ID (MEMBER.id 참조)

    @Column(name = "reply_to_member_id")
    private Long replyToMemberId; // 답글 대상 회원 ID (MEMBER.id 참조, null이면 댓글에 대한 첫 답글)

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content; // 답글 내용

    @Column(name = "is_hidden", nullable = false)
    private Boolean isHidden = false; // 숨김 여부 (true: 관리자에 의해 숨김 처리)

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
