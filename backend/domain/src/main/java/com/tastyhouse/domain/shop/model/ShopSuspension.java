package com.tastyhouse.domain.shop.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.shop.vo.ShopId;

public class ShopSuspension {
    private final Long id;
    private final ShopId shopId;
    private final SuspensionReason reason;
    private final OrderMethod orderMethod;
    private final LocalDateTime startAt;
    private final LocalDateTime endAt;
    private LocalDateTime releasedAt;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private ShopSuspension(
        Long id,
        ShopId shopId,
        SuspensionReason reason,
        OrderMethod orderMethod,
        LocalDateTime startAt,
        LocalDateTime endAt,
        LocalDateTime releasedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.shopId = shopId;
        this.reason = reason;
        this.orderMethod = orderMethod;
        this.startAt = startAt;
        this.endAt = endAt;
        this.releasedAt = releasedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ShopSuspension of(
        ShopId shopId,
        SuspensionReason reason,
        OrderMethod orderMethod,
        LocalDateTime startAt,
        LocalDateTime endAt
    ) {
        if (endAt.isBefore(startAt)) {
            throw new BusinessException(ErrorCode.SHOP_SUSPENSION_INVALID_PERIOD);
        }

        return new ShopSuspension(null, shopId, reason, orderMethod, startAt, endAt, null, null, null);
    }

    public static ShopSuspension reconstitute(
        Long id,
        ShopId shopId,
        SuspensionReason reason,
        OrderMethod orderMethod,
        LocalDateTime startAt,
        LocalDateTime endAt,
        LocalDateTime releasedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new ShopSuspension(id, shopId, reason, orderMethod, startAt, endAt, releasedAt, createdAt, updatedAt);
    }

    public void release(LocalDateTime releasedAt) {
        this.releasedAt = releasedAt;
    }

    public boolean isActive(LocalDateTime now) {
        return releasedAt == null && !now.isBefore(startAt) && now.isBefore(endAt);
    }

    public boolean appliesTo(OrderMethod target) {
        if (this.orderMethod == null) {
            return true;
        }
        return this.orderMethod == target;
    }

    public boolean isActive(LocalDateTime now, OrderMethod target) {
        return isActive(now) && appliesTo(target);
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public SuspensionReason getReason() {
        return this.reason;
    }

    public OrderMethod getOrderMethod() {
        return this.orderMethod;
    }

    public LocalDateTime getStartAt() {
        return this.startAt;
    }

    public LocalDateTime getEndAt() {
        return this.endAt;
    }

    public LocalDateTime getReleasedAt() {
        return this.releasedAt;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }
}
