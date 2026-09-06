package com.tastyhouse.domain.shop.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.tastyhouse.domain.shop.vo.ShopId;

public class ShopHygieneBadge {
    private final Long id;
    private final ShopId shopId;
    private final HygieneBadgeType badgeType;
    private final LocalDate certifiedDate;
    private final String lastInspectionMonth;
    private final LocalDateTime createdAt;

    private ShopHygieneBadge(
        Long id,
        ShopId shopId,
        HygieneBadgeType badgeType,
        LocalDate certifiedDate,
        String lastInspectionMonth,
        LocalDateTime createdAt
    ) {
        this.id = id;
        this.shopId = shopId;
        this.badgeType = badgeType;
        this.certifiedDate = certifiedDate;
        this.lastInspectionMonth = lastInspectionMonth;
        this.createdAt = createdAt;
    }

    public static ShopHygieneBadge of(
        ShopId shopId,
        HygieneBadgeType badgeType,
        LocalDate certifiedDate,
        String lastInspectionMonth
    ) {
        return new ShopHygieneBadge(null, shopId, badgeType, certifiedDate, lastInspectionMonth, null);
    }

    public static ShopHygieneBadge reconstitute(
        Long id,
        ShopId shopId,
        HygieneBadgeType badgeType,
        LocalDate certifiedDate,
        String lastInspectionMonth,
        LocalDateTime createdAt
    ) {
        return new ShopHygieneBadge(id, shopId, badgeType, certifiedDate, lastInspectionMonth, createdAt);
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public HygieneBadgeType getBadgeType() {
        return this.badgeType;
    }

    public LocalDate getCertifiedDate() {
        return this.certifiedDate;
    }

    public String getLastInspectionMonth() {
        return this.lastInspectionMonth;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }
}
