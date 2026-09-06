package com.tastyhouse.domain.shop.model;

import com.tastyhouse.domain.shop.vo.ShopAmenityCategoryId;
import com.tastyhouse.domain.shop.vo.ShopId;

public class ShopAmenity {
    private final Long id;
    private final ShopId shopId;
    private final ShopAmenityCategoryId shopAmenityCategoryId;

    private ShopAmenity(Long id, ShopId shopId, ShopAmenityCategoryId shopAmenityCategoryId) {
        this.id = id;
        this.shopId = shopId;
        this.shopAmenityCategoryId = shopAmenityCategoryId;
    }

    public static ShopAmenity of(ShopId shopId, ShopAmenityCategoryId shopAmenityCategoryId) {
        return new ShopAmenity(null, shopId, shopAmenityCategoryId);
    }

    public static ShopAmenity reconstitute(Long id, ShopId shopId, ShopAmenityCategoryId shopAmenityCategoryId) {
        return new ShopAmenity(id, shopId, shopAmenityCategoryId);
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public ShopAmenityCategoryId getShopAmenityCategoryId() {
        return this.shopAmenityCategoryId;
    }
}
