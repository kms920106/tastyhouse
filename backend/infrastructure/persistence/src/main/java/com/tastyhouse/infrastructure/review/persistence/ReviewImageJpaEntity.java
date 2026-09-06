package com.tastyhouse.infrastructure.review.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(
    name = "REVIEW_IMAGE",
    indexes = {
        @Index(name = "idx_review_image_review_id", columnList = "review_id")
    }
)
public class ReviewImageJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "review_id", nullable = false)
    private Long reviewId;

    @Column(name = "image_file_id", nullable = false)
    private Long imageFileId;

    @Column(name = "sort", nullable = false)
    private Integer sort;

    protected ReviewImageJpaEntity() {
    }

    private ReviewImageJpaEntity(Long reviewId, Long imageFileId, Integer sort) {
        this.reviewId = reviewId;
        this.imageFileId = imageFileId;
        this.sort = sort;
    }

    static ReviewImageJpaEntity create(Long reviewId, Long imageFileId, Integer sort) {
        return new ReviewImageJpaEntity(reviewId, imageFileId, sort);
    }

    public Long getId() {
        return this.id;
    }

    public Long getReviewId() {
        return this.reviewId;
    }

    public Long getImageFileId() {
        return this.imageFileId;
    }

    public Integer getSort() {
        return this.sort;
    }
}
