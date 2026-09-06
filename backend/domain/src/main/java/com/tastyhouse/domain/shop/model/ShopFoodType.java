package com.tastyhouse.domain.shop.model;

import com.tastyhouse.domain.shop.vo.ShopFoodTypeCategoryId;
import com.tastyhouse.domain.shop.vo.ShopId;

public class ShopFoodType {
    private final Long id;
    private final ShopId shopId;
    private final ShopFoodTypeCategoryId shopFoodTypeCategoryId;

    private ShopFoodType(Long id, ShopId shopId, ShopFoodTypeCategoryId shopFoodTypeCategoryId) {
        this.id = id;
        this.shopId = shopId;
        this.shopFoodTypeCategoryId = shopFoodTypeCategoryId;
    }

    public static ShopFoodType of(ShopId shopId, ShopFoodTypeCategoryId shopFoodTypeCategoryId) {
        return new ShopFoodType(null, shopId, shopFoodTypeCategoryId);
    }

    public static ShopFoodType reconstitute(Long id, ShopId shopId, ShopFoodTypeCategoryId shopFoodTypeCategoryId) {
        return new ShopFoodType(id, shopId, shopFoodTypeCategoryId);
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public ShopFoodTypeCategoryId getShopFoodTypeCategoryId() {
        return this.shopFoodTypeCategoryId;
    }
}
