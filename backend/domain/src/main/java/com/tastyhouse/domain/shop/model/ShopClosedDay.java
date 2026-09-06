package com.tastyhouse.domain.shop.model;

import com.tastyhouse.domain.shop.vo.ShopId;

public class ShopClosedDay {
    private final Long id;
    private final ShopId shopId;
    private final ClosedDayType closedDayType;

    private ShopClosedDay(Long id, ShopId shopId, ClosedDayType closedDayType) {
        this.id = id;
        this.shopId = shopId;
        this.closedDayType = closedDayType;
    }

    public static ShopClosedDay of(ShopId shopId, ClosedDayType closedDayType) {
        return new ShopClosedDay(null, shopId, closedDayType);
    }

    public static ShopClosedDay reconstitute(Long id, ShopId shopId, ClosedDayType closedDayType) {
        return new ShopClosedDay(id, shopId, closedDayType);
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public ClosedDayType getClosedDayType() {
        return this.closedDayType;
    }
}
