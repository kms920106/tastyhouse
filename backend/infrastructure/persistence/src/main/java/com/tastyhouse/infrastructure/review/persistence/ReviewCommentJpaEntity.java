package com.tastyhouse.infrastructure.review.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(name = "REVIEW_COMMENT")
public class ReviewCommentJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "review_id", nullable = false)
    private Long reviewId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "is_hidden", nullable = false)
    private boolean hidden;

    protected ReviewCommentJpaEntity() {
    }

    private ReviewCommentJpaEntity(
        Long reviewId,
        Long memberId,
        String content,
        boolean hidden
    ) {
        this.reviewId = reviewId;
        this.memberId = memberId;
        this.content = content;
        this.hidden = hidden;
    }

    static ReviewCommentJpaEntity create(
        Long reviewId,
        Long memberId,
        String content,
        boolean hidden
    ) {
        return new ReviewCommentJpaEntity(reviewId, memberId, content, hidden);
    }

    void applyChanges(boolean hidden) {
        this.hidden = hidden;
    }

    public Long getId() {
        return this.id;
    }

    public Long getReviewId() {
        return this.reviewId;
    }

    public Long getMemberId() {
        return this.memberId;
    }

    public String getContent() {
        return this.content;
    }

    public boolean isHidden() {
        return this.hidden;
    }
}
