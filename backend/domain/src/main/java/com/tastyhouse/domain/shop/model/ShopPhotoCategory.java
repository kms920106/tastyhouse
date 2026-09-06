package com.tastyhouse.domain.shop.model;

import com.tastyhouse.domain.shop.vo.ShopId;

public class ShopPhotoCategory {
    private final Long id;
    private final ShopId shopId;
    private String name;

    private ShopPhotoCategory(Long id, ShopId shopId, String name) {
        this.id = id;
        this.shopId = shopId;
        this.name = name;
    }

    public static ShopPhotoCategory of(ShopId shopId, String name) {
        return new ShopPhotoCategory(null, shopId, name);
    }

    public static ShopPhotoCategory reconstitute(Long id, ShopId shopId, String name) {
        return new ShopPhotoCategory(id, shopId, name);
    }

    public void update(String name) {
        this.name = name;
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public String getName() {
        return this.name;
    }
}
