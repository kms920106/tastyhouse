package com.tastyhouse.application.shop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopPhotoCategoryManagementQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopBasicInfoQueryPort;
import com.tastyhouse.application.shop.port.out.ShopPhotoCategoryResult;

@Service
@Transactional(readOnly = true)
class ShopPhotoCategoryManagementQueryService implements ShopPhotoCategoryManagementQueryUseCase {

    private final ShopBasicInfoQueryPort shopBasicInfoQueryPort;

    public ShopPhotoCategoryManagementQueryService(ShopBasicInfoQueryPort shopBasicInfoQueryPort) {
        this.shopBasicInfoQueryPort = shopBasicInfoQueryPort;
    }

    @Override
    public List<ShopPhotoCategoryResult> getPhotoCategories(Long id) {
        return shopBasicInfoQueryPort.findPhotoCategories(id);
    }
}
