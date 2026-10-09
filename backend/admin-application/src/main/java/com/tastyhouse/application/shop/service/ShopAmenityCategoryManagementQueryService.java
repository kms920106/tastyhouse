package com.tastyhouse.application.shop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopAmenityCategoryManagementQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopAmenityCategoryResult;
import com.tastyhouse.application.shop.port.out.ShopClassificationManagementQueryPort;

@Service
@Transactional(readOnly = true)
class ShopAmenityCategoryManagementQueryService implements ShopAmenityCategoryManagementQueryUseCase {

    private final ShopClassificationManagementQueryPort shopClassificationManagementQueryPort;

    public ShopAmenityCategoryManagementQueryService(ShopClassificationManagementQueryPort shopClassificationManagementQueryPort) {
        this.shopClassificationManagementQueryPort = shopClassificationManagementQueryPort;
    }

    @Override
    public List<ShopAmenityCategoryResult> getAmenityCategories() {
        return shopClassificationManagementQueryPort.findAllAmenityCategories();
    }
}
