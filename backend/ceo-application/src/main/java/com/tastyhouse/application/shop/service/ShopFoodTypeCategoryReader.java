package com.tastyhouse.application.shop.service;

import java.util.Set;

import org.springframework.stereotype.Component;

import com.tastyhouse.application.shop.port.out.ShopClassificationOwnerQueryPort;

@Component
public class ShopFoodTypeCategoryReader {

    private final ShopClassificationOwnerQueryPort shopClassificationOwnerQueryPort;

    public ShopFoodTypeCategoryReader(ShopClassificationOwnerQueryPort shopClassificationOwnerQueryPort) {
        this.shopClassificationOwnerQueryPort = shopClassificationOwnerQueryPort;
    }

    public Set<String> readCategoryNames(Long shopId) {
        return Set.copyOf(shopClassificationOwnerQueryPort.findFoodTypeCategoryNames(shopId));
    }
}
