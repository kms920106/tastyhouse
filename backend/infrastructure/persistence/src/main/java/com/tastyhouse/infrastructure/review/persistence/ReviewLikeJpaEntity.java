package com.tastyhouse.infrastructure.review.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(name = "REVIEW_LIKE")
public class ReviewLikeJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "review_id", nullable = false)
    private Long reviewId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    protected ReviewLikeJpaEntity() {
    }

    private ReviewLikeJpaEntity(Long reviewId, Long memberId) {
        this.reviewId = reviewId;
        this.memberId = memberId;
    }

    static ReviewLikeJpaEntity create(Long reviewId, Long memberId) {
        return new ReviewLikeJpaEntity(reviewId, memberId);
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
}
