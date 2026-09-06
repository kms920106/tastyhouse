package com.tastyhouse.domain.review.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.domain.shop.vo.ShopId;

public class ReviewBlindRequest {
    public static final int BLIND_PERIOD_DAYS = 30;

    private final Long id;
    private final ReviewId reviewId;
    private final ShopId shopId;
    private final CeoId ceoId;
    private final ReviewBlindReason reason;
    private final String detailReason;
    private ReviewBlindStatus status;
    private String rejectReason;
    private LocalDateTime blindUntil;
    private final LocalDateTime createdAt;

    private ReviewBlindRequest(
        Long id,
        ReviewId reviewId,
        ShopId shopId,
        CeoId ceoId,
        ReviewBlindReason reason,
        String detailReason,
        ReviewBlindStatus status,
        String rejectReason,
        LocalDateTime blindUntil,
        LocalDateTime createdAt
    ) {
        this.id = id;
        this.reviewId = reviewId;
        this.shopId = shopId;
        this.ceoId = ceoId;
        this.reason = reason;
        this.detailReason = detailReason;
        this.status = status;
        this.rejectReason = rejectReason;
        this.blindUntil = blindUntil;
        this.createdAt = createdAt;
    }

    public static ReviewBlindRequest of(
        ReviewId reviewId,
        ShopId shopId,
        CeoId ceoId,
        ReviewBlindReason reason,
        String detailReason
    ) {
        return new ReviewBlindRequest(
            null, reviewId, shopId, ceoId, reason, detailReason, ReviewBlindStatus.PENDING, null, null, null
        );
    }

    public static ReviewBlindRequest reconstitute(
        Long id,
        ReviewId reviewId,
        ShopId shopId,
        CeoId ceoId,
        ReviewBlindReason reason,
        String detailReason,
        ReviewBlindStatus status,
        String rejectReason,
        LocalDateTime blindUntil,
        LocalDateTime createdAt
    ) {
        return new ReviewBlindRequest(
            id, reviewId, shopId, ceoId, reason, detailReason, status, rejectReason, blindUntil, createdAt
        );
    }

    public void approve(LocalDateTime blindUntil) {
        requirePending();
        this.status = ReviewBlindStatus.APPROVED;
        this.blindUntil = blindUntil;
    }

    public void reject(String reason) {
        requirePending();
        this.status = ReviewBlindStatus.REJECTED;
        this.rejectReason = reason;
    }

    public void cancel() {
        requirePending();
        this.status = ReviewBlindStatus.CANCELED;
        this.rejectReason = null;
    }

    public void expire() {
        requireApproved();
        this.status = ReviewBlindStatus.EXPIRED;
        this.blindUntil = null;
    }

    public void deleteByConsent() {
        requireApproved();
        this.status = ReviewBlindStatus.DELETED;
        this.blindUntil = null;
    }

    private void requirePending() {
        if (this.status != ReviewBlindStatus.PENDING) {
            throw new BusinessException(ErrorCode.REVIEW_BLIND_REQUEST_NOT_PENDING);
        }
    }

    private void requireApproved() {
        if (this.status != ReviewBlindStatus.APPROVED) {
            throw new BusinessException(ErrorCode.REVIEW_BLIND_REQUEST_NOT_APPROVED);
        }
    }

    public Long getId() {
        return this.id;
    }

    public ReviewId getReviewId() {
        return this.reviewId;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public CeoId getCeoId() {
        return this.ceoId;
    }

    public ReviewBlindReason getReason() {
        return this.reason;
    }

    public String getDetailReason() {
        return this.detailReason;
    }

    public ReviewBlindStatus getStatus() {
        return this.status;
    }

    public String getRejectReason() {
        return this.rejectReason;
    }

    public LocalDateTime getBlindUntil() {
        return this.blindUntil;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }
}
