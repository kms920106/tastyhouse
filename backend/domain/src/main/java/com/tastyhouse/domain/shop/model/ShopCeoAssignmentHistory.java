package com.tastyhouse.domain.shop.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.shop.vo.ShopId;

public class ShopCeoAssignmentHistory {
    private final Long id;
    private final ShopId shopId;
    private final CeoId ceoId;
    private final ShopCeoAssignmentActionType actionType;
    private final Long actorAdminId;
    private final LocalDateTime createdAt;

    private ShopCeoAssignmentHistory(
        Long id,
        ShopId shopId,
        CeoId ceoId,
        ShopCeoAssignmentActionType actionType,
        Long actorAdminId,
        LocalDateTime createdAt
    ) {
        this.id = id;
        this.shopId = shopId;
        this.ceoId = ceoId;
        this.actionType = actionType;
        this.actorAdminId = actorAdminId;
        this.createdAt = createdAt;
    }

    public static ShopCeoAssignmentHistory of(
        ShopId shopId,
        CeoId ceoId,
        ShopCeoAssignmentActionType actionType,
        Long actorAdminId
    ) {
        return new ShopCeoAssignmentHistory(null, shopId, ceoId, actionType, actorAdminId, null);
    }

    public static ShopCeoAssignmentHistory reconstitute(
        Long id,
        ShopId shopId,
        CeoId ceoId,
        ShopCeoAssignmentActionType actionType,
        Long actorAdminId,
        LocalDateTime createdAt
    ) {
        return new ShopCeoAssignmentHistory(id, shopId, ceoId, actionType, actorAdminId, createdAt);
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public CeoId getCeoId() {
        return this.ceoId;
    }

    public ShopCeoAssignmentActionType getActionType() {
        return this.actionType;
    }

    public Long getActorAdminId() {
        return this.actorAdminId;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }
}
