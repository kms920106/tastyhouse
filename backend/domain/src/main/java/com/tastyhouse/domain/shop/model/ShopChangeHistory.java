package com.tastyhouse.domain.shop.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.shop.vo.ShopId;

public class ShopChangeHistory {
    private final Long id;
    private final ShopId shopId;
    private final ShopChangeCategory category;
    private final ShopChangeType changeType;
    private final ShopChangeActionType actionType;
    private final ShopChangeActorType actorType;
    private final Long actorId;
    private final String previousValue;
    private final String newValue;
    private final LocalDateTime createdAt;

    private ShopChangeHistory(
        Long id,
        ShopId shopId,
        ShopChangeCategory category,
        ShopChangeType changeType,
        ShopChangeActionType actionType,
        ShopChangeActorType actorType,
        Long actorId,
        String previousValue,
        String newValue,
        LocalDateTime createdAt
    ) {
        this.id = id;
        this.shopId = shopId;
        this.category = category;
        this.changeType = changeType;
        this.actionType = actionType;
        this.actorType = actorType;
        this.actorId = actorId;
        this.previousValue = previousValue;
        this.newValue = newValue;
        this.createdAt = createdAt;
    }

    public static ShopChangeHistory of(
        ShopId shopId,
        ShopChangeType changeType,
        ShopChangeActionType actionType,
        ShopChangeActor actor,
        String previousValue,
        String newValue
    ) {
        return new ShopChangeHistory(null, shopId, changeType.getCategory(), changeType, actionType,
            actor.actorType(), actor.actorId(), previousValue, newValue, null);
    }

    public static ShopChangeHistory reconstitute(
        Long id,
        ShopId shopId,
        ShopChangeCategory category,
        ShopChangeType changeType,
        ShopChangeActionType actionType,
        ShopChangeActorType actorType,
        Long actorId,
        String previousValue,
        String newValue,
        LocalDateTime createdAt
    ) {
        return new ShopChangeHistory(id, shopId, category, changeType, actionType, actorType, actorId,
            previousValue, newValue, createdAt);
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public ShopChangeCategory getCategory() {
        return this.category;
    }

    public ShopChangeType getChangeType() {
        return this.changeType;
    }

    public ShopChangeActionType getActionType() {
        return this.actionType;
    }

    public ShopChangeActorType getActorType() {
        return this.actorType;
    }

    public Long getActorId() {
        return this.actorId;
    }

    public String getPreviousValue() {
        return this.previousValue;
    }

    public String getNewValue() {
        return this.newValue;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }
}
