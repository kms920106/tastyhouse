package com.tastyhouse.domain.shop.model;

import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.shop.vo.ShopId;

public class ShopOrderMethod {
    private final Long id;
    private final ShopId shopId;
    private final OrderMethod orderMethod;

    private ShopOrderMethod(Long id, ShopId shopId, OrderMethod orderMethod) {
        this.id = id;
        this.shopId = shopId;
        this.orderMethod = orderMethod;
    }

    public static ShopOrderMethod of(ShopId shopId, OrderMethod orderMethod) {
        return new ShopOrderMethod(null, shopId, orderMethod);
    }

    public static ShopOrderMethod reconstitute(Long id, ShopId shopId, OrderMethod orderMethod) {
        return new ShopOrderMethod(id, shopId, orderMethod);
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public OrderMethod getOrderMethod() {
        return this.orderMethod;
    }
}
