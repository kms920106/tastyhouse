package com.tastyhouse.infrastructure.review.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.review.vo.ReviewCommentId;
import com.tastyhouse.infrastructure.member.persistence.MemberIdConverter;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 리뷰 답글 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code ReviewReply}와 분리된 영속 전용 엔티티다. DB 매핑(테이블/컬럼/감사 필드)만
 * 담당하고 비즈니스 행위는 갖지 않는다. 도메인↔엔티티 변환은 {@code ReviewReplyMapper}가 수행한다.
 */
@Entity
@Table(name = "REVIEW_REPLY")
public class ReviewReplyJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = ReviewCommentIdConverter.class)
    @Column(name = "comment_id", nullable = false)
    private ReviewCommentId commentId;

    @Convert(converter = MemberIdConverter.class)
    @Column(name = "member_id", nullable = false)
    private MemberId memberId;

    @Convert(converter = MemberIdConverter.class)
    @Column(name = "reply_to_member_id")
    private MemberId replyToMemberId;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "is_hidden", nullable = false)
    private boolean hidden;

    protected ReviewReplyJpaEntity() {
    }

    private ReviewReplyJpaEntity(
        ReviewCommentId commentId,
        MemberId memberId,
        MemberId replyToMemberId,
        String content,
        boolean hidden
    ) {
        this.commentId = commentId;
        this.memberId = memberId;
        this.replyToMemberId = replyToMemberId;
        this.content = content;
        this.hidden = hidden;
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code ReviewReplyMapper#toEntity}에서만 호출한다.
     */
    static ReviewReplyJpaEntity create(
        ReviewCommentId commentId,
        MemberId memberId,
        MemberId replyToMemberId,
        String content,
        boolean hidden
    ) {
        return new ReviewReplyJpaEntity(commentId, memberId, replyToMemberId, content, hidden);
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update용 dirty checking 대체). 감사 필드·식별자는 건드리지 않는다.
     */
    void applyChanges(boolean hidden) {
        this.hidden = hidden;
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
}
