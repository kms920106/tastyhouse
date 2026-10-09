package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopBannerImageDeleteCommand;
import com.tastyhouse.application.shop.port.in.ShopBannerImageDeleteUseCase;
import com.tastyhouse.application.shop.port.out.write.ShopDetailSavePort;

@Service
@Transactional
class ShopBannerImageDeleteService implements ShopBannerImageDeleteUseCase {

    private final ShopDetailSavePort shopDetailSavePort;

    public ShopBannerImageDeleteService(ShopDetailSavePort shopDetailSavePort) {
        this.shopDetailSavePort = shopDetailSavePort;
    }

    @Override
    public void deleteBannerImage(ShopBannerImageDeleteCommand command) {
        Long bannerImageId = command.bannerImageId();

        shopDetailSavePort.deleteBannerImageById(bannerImageId);
    }
}
