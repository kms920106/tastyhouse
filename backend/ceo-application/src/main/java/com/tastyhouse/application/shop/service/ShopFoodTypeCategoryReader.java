package com.tastyhouse.application.shop.service;

import java.util.Set;

import org.springframework.stereotype.Component;

import com.tastyhouse.application.shop.port.out.ShopOwnerQueryPort;

@Component
public class ShopFoodTypeCategoryReader {

    private final ShopOwnerQueryPort shopOwnerQueryPort;

    public ShopFoodTypeCategoryReader(ShopOwnerQueryPort shopOwnerQueryPort) {
        this.shopOwnerQueryPort = shopOwnerQueryPort;
    }

    public Set<String> readCategoryNames(Long shopId) {
        return Set.copyOf(shopOwnerQueryPort.findFoodTypeCategoryNames(shopId));
    }
}
