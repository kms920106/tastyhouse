package com.tastyhouse.infrastructure.persistence.review.persistence;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.persistence.shared.persistence.BaseEntity;

@Entity
@Table(name = "REVIEW_BLIND_REQUEST")
public class ReviewBlindRequestJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "review_id", nullable = false)
    private Long reviewId;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "ceo_id", nullable = false)
    private Long ceoId;

    @Column(name = "reason", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private String reason;

    @Column(name = "detail_reason", length = 500)
    private String detailReason;

    @Column(name = "status", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private String status;

    @Column(name = "reject_reason", length = 500)
    private String rejectReason;

    @Column(name = "blind_until")
    private LocalDateTime blindUntil;

    protected ReviewBlindRequestJpaEntity() {
    }

    private ReviewBlindRequestJpaEntity(
        Long reviewId,
        Long shopId,
        Long ceoId,
        String reason,
        String detailReason,
        String status,
        String rejectReason,
        LocalDateTime blindUntil
    ) {
        this.reviewId = reviewId;
        this.shopId = shopId;
        this.ceoId = ceoId;
        this.reason = reason;
        this.detailReason = detailReason;
        this.status = status;
        this.rejectReason = rejectReason;
        this.blindUntil = blindUntil;
    }

    static ReviewBlindRequestJpaEntity create(
        Long reviewId,
        Long shopId,
        Long ceoId,
        String reason,
        String detailReason,
        String status,
        String rejectReason,
        LocalDateTime blindUntil
    ) {
        return new ReviewBlindRequestJpaEntity(
            reviewId, shopId, ceoId, reason, detailReason, status, rejectReason, blindUntil
        );
    }

    void applyChanges(String status, String rejectReason, LocalDateTime blindUntil) {
        this.status = status;
        this.rejectReason = rejectReason;
        this.blindUntil = blindUntil;
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

    public String getReason() {
        return this.reason;
    }

    public String getDetailReason() {
        return this.detailReason;
    }

    public String getStatus() {
        return this.status;
    }

    public String getRejectReason() {
        return this.rejectReason;
    }

    public LocalDateTime getBlindUntil() {
        return this.blindUntil;
    }
}
