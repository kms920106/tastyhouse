package com.tastyhouse.infrastructure.review.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(name = "REVIEW_OWNER_REPLY")
public class ReviewOwnerReplyJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "review_id", nullable = false)
    private Long reviewId;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "ceo_id", nullable = false)
    private Long ceoId;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    protected ReviewOwnerReplyJpaEntity() {
    }

    private ReviewOwnerReplyJpaEntity(Long reviewId, Long shopId, Long ceoId, String content) {
        this.reviewId = reviewId;
        this.shopId = shopId;
        this.ceoId = ceoId;
        this.content = content;
    }

    static ReviewOwnerReplyJpaEntity create(Long reviewId, Long shopId, Long ceoId, String content) {
        return new ReviewOwnerReplyJpaEntity(reviewId, shopId, ceoId, content);
    }

    void applyChanges(String content) {
        this.content = content;
    }

    public Long getId() {
        return this.id;
    }

    public Long getReviewId() {
        return this.reviewId;
    }

    public Long getShopId() {
        return this.shopId;
    }

    public Long getCeoId() {
        return this.ceoId;
    }

    public String getContent() {
        return this.content;
    }
}
