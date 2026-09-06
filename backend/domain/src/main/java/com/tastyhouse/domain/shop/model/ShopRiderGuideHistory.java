package com.tastyhouse.domain.shop.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.shop.vo.ShopId;

public class ShopRiderGuideHistory {
    private final Long id;
    private final ShopId shopId;
    private final RiderGuideActorType actorType;
    private final Long actorId;
    private final RiderGuideActionType actionType;
    private final String previousVisitGuide;
    private final String newVisitGuide;
    private final String reason;
    private final LocalDateTime createdAt;

    private ShopRiderGuideHistory(
        Long id,
        ShopId shopId,
        RiderGuideActorType actorType,
        Long actorId,
        RiderGuideActionType actionType,
        String previousVisitGuide,
        String newVisitGuide,
        String reason,
        LocalDateTime createdAt
    ) {
        this.id = id;
        this.shopId = shopId;
        this.actorType = actorType;
        this.actorId = actorId;
        this.actionType = actionType;
        this.previousVisitGuide = previousVisitGuide;
        this.newVisitGuide = newVisitGuide;
        this.reason = reason;
        this.createdAt = createdAt;
    }

    public static ShopRiderGuideHistory of(
        ShopId shopId,
        RiderGuideActorType actorType,
        Long actorId,
        RiderGuideActionType actionType,
        String previousVisitGuide,
        String newVisitGuide,
        String reason
    ) {
        return new ShopRiderGuideHistory(null, shopId, actorType, actorId, actionType, previousVisitGuide,
            newVisitGuide, reason, null);
    }

    public static ShopRiderGuideHistory reconstitute(
        Long id,
        ShopId shopId,
        RiderGuideActorType actorType,
        Long actorId,
        RiderGuideActionType actionType,
        String previousVisitGuide,
        String newVisitGuide,
        String reason,
        LocalDateTime createdAt
    ) {
        return new ShopRiderGuideHistory(id, shopId, actorType, actorId, actionType, previousVisitGuide,
            newVisitGuide, reason, createdAt);
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public RiderGuideActorType getActorType() {
        return this.actorType;
    }

    public Long getActorId() {
        return this.actorId;
    }

    public RiderGuideActionType getActionType() {
        return this.actionType;
    }

    public String getPreviousVisitGuide() {
        return this.previousVisitGuide;
    }

    public String getNewVisitGuide() {
        return this.newVisitGuide;
    }

    public String getReason() {
        return this.reason;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }
}
