package com.tastyhouse.application.shop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopBannerImageManagementQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopBannerImageResult;
import com.tastyhouse.application.shop.port.out.ShopBasicInfoQueryPort;

@Service
@Transactional(readOnly = true)
class ShopBannerImageManagementQueryService implements ShopBannerImageManagementQueryUseCase {

    private final ShopBasicInfoQueryPort shopBasicInfoQueryPort;

    public ShopBannerImageManagementQueryService(ShopBasicInfoQueryPort shopBasicInfoQueryPort) {
        this.shopBasicInfoQueryPort = shopBasicInfoQueryPort;
    }

    @Override
    public List<ShopBannerImageResult> getBannerImages(Long id) {
        return shopBasicInfoQueryPort.findBannerImages(id);
    }
}
