package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopBannerImageDeleteCommand;
import com.tastyhouse.application.shop.port.in.ShopBannerImageDeleteUseCase;
import com.tastyhouse.application.shop.port.out.write.ShopBannerImageSavePort;

@Service
@Transactional
class ShopBannerImageDeleteService implements ShopBannerImageDeleteUseCase {

    private final ShopBannerImageSavePort shopBannerImageSavePort;

    public ShopBannerImageDeleteService(ShopBannerImageSavePort shopBannerImageSavePort) {
        this.shopBannerImageSavePort = shopBannerImageSavePort;
    }

    @Override
    public void deleteBannerImage(ShopBannerImageDeleteCommand command) {
        Long bannerImageId = command.bannerImageId();

        shopBannerImageSavePort.deleteBannerImageById(bannerImageId);
    }
}
