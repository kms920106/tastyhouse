package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.tastyhouse.application.shop.port.out.ShopOwnerQueryPort;

@Component
@CeoApp
public class ShopFoodTypeCategoryReader {

    private final ShopOwnerQueryPort shopOwnerQueryPort;

    public ShopFoodTypeCategoryReader(ShopOwnerQueryPort shopOwnerQueryPort) {
        this.shopOwnerQueryPort = shopOwnerQueryPort;
    }

    public Set<String> readCategoryNames(Long shopId) {
        return Set.copyOf(shopOwnerQueryPort.findFoodTypeCategoryNames(shopId));
    }
}
