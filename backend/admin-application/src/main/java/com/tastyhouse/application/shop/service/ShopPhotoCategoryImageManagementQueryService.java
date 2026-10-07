package com.tastyhouse.application.shop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopPhotoCategoryImageManagementQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopManagementQueryPort;
import com.tastyhouse.application.shop.port.out.ShopPhotoCategoryImageManagementResult;

@Service
@Transactional(readOnly = true)
class ShopPhotoCategoryImageManagementQueryService implements ShopPhotoCategoryImageManagementQueryUseCase {

    private final ShopManagementQueryPort shopManagementQueryPort;

    public ShopPhotoCategoryImageManagementQueryService(ShopManagementQueryPort shopManagementQueryPort) {
        this.shopManagementQueryPort = shopManagementQueryPort;
    }

    @Override
    public List<ShopPhotoCategoryImageManagementResult> getPhotoCategoryImages(Long categoryId) {
        return shopManagementQueryPort.findPhotoCategoryImages(categoryId);
    }
}
