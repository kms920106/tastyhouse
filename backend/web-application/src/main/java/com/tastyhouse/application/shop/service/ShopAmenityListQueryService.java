package com.tastyhouse.application.shop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopAmenityListQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopAmenityCategoryResult;
import com.tastyhouse.application.shop.port.out.ShopQueryPort;

@Service
@Transactional(readOnly = true)
class ShopAmenityListQueryService implements ShopAmenityListQueryUseCase {

    private final ShopQueryPort shopQueryPort;

    public ShopAmenityListQueryService(ShopQueryPort shopQueryPort) {
        this.shopQueryPort = shopQueryPort;
    }

    @Override
    public List<ShopAmenityCategoryResult> searchAllAmenities() {
        return shopQueryPort.findVisibleAmenityCategories();
    }
}
