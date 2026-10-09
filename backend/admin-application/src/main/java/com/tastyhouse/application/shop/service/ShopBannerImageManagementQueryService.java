package com.tastyhouse.application.shop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopBannerImageManagementQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopBannerImageResult;
import com.tastyhouse.application.shop.port.out.ShopMediaManagementQueryPort;

@Service
@Transactional(readOnly = true)
class ShopBannerImageManagementQueryService implements ShopBannerImageManagementQueryUseCase {

    private final ShopMediaManagementQueryPort shopMediaManagementQueryPort;

    public ShopBannerImageManagementQueryService(ShopMediaManagementQueryPort shopMediaManagementQueryPort) {
        this.shopMediaManagementQueryPort = shopMediaManagementQueryPort;
    }

    @Override
    public List<ShopBannerImageResult> getBannerImages(Long id) {
        return shopMediaManagementQueryPort.findBannerImages(id);
    }
}
