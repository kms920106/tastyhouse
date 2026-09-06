package com.tastyhouse.domain.review.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.domain.shop.vo.ShopId;

public class ReviewOwnerReply {
    public static final int REPLY_PERIOD_DAYS = 30;

    private final Long id;
    private final ReviewId reviewId;
    private final ShopId shopId;
    private final CeoId ceoId;
    private String content;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private ReviewOwnerReply(
        Long id,
        ReviewId reviewId,
        ShopId shopId,
        CeoId ceoId,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.reviewId = reviewId;
        this.shopId = shopId;
        this.ceoId = ceoId;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ReviewOwnerReply of(ReviewId reviewId, ShopId shopId, CeoId ceoId, String content) {
        return new ReviewOwnerReply(null, reviewId, shopId, ceoId, content, null, null);
    }

    public static ReviewOwnerReply reconstitute(
        Long id,
        ReviewId reviewId,
        ShopId shopId,
        CeoId ceoId,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new ReviewOwnerReply(id, reviewId, shopId, ceoId, content, createdAt, updatedAt);
    }

    public void updateContent(String content) {
        this.content = content;
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

    public String getContent() {
        return this.content;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }
}
