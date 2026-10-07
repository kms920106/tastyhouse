package com.tastyhouse.application.shop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopBannerListQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopBannerImageResult;
import com.tastyhouse.application.shop.port.out.ShopBasicInfoQueryPort;

@Service
@Transactional(readOnly = true)
class ShopBannerListQueryService implements ShopBannerListQueryUseCase {

    private final ShopBasicInfoQueryPort shopBasicInfoQueryPort;

    public ShopBannerListQueryService(ShopBasicInfoQueryPort shopBasicInfoQueryPort) {
        this.shopBasicInfoQueryPort = shopBasicInfoQueryPort;
    }

    @Override
    public List<ShopBannerImageResult> getShopBanners(Long shopId) {
        return shopBasicInfoQueryPort.findBannerImages(shopId);
    }
}
