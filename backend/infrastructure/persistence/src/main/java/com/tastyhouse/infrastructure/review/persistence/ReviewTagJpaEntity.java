package com.tastyhouse.infrastructure.review.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "REVIEW_TAG")
public class ReviewTagJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "review_id", nullable = false)
    private Long reviewId;

    @Column(name = "tag_id", nullable = false)
    private Long tagId;

    protected ReviewTagJpaEntity() {
    }

    private ReviewTagJpaEntity(Long reviewId, Long tagId) {
        this.reviewId = reviewId;
        this.tagId = tagId;
    }

    static ReviewTagJpaEntity create(Long reviewId, Long tagId) {
        return new ReviewTagJpaEntity(reviewId, tagId);
    }

    public Long getId() {
        return this.id;
    }

    public Long getReviewId() {
        return this.reviewId;
    }
}
