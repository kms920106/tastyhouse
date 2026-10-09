package com.tastyhouse.application.shop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopPhotoCategoryManagementQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopMediaManagementQueryPort;
import com.tastyhouse.application.shop.port.out.ShopPhotoCategoryResult;

@Service
@Transactional(readOnly = true)
class ShopPhotoCategoryManagementQueryService implements ShopPhotoCategoryManagementQueryUseCase {

    private final ShopMediaManagementQueryPort shopMediaManagementQueryPort;

    public ShopPhotoCategoryManagementQueryService(ShopMediaManagementQueryPort shopMediaManagementQueryPort) {
        this.shopMediaManagementQueryPort = shopMediaManagementQueryPort;
    }

    @Override
    public List<ShopPhotoCategoryResult> getPhotoCategories(Long id) {
        return shopMediaManagementQueryPort.findPhotoCategories(id);
    }
}
