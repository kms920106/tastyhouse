package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopBannerImageDeleteCommand;
import com.tastyhouse.application.shop.port.in.ShopBannerImageDeleteUseCase;
import com.tastyhouse.application.shop.port.out.write.ShopDetailPersistencePort;

@Service
@Transactional
class ShopBannerImageDeleteService implements ShopBannerImageDeleteUseCase {

    private final ShopDetailPersistencePort shopDetailPersistencePort;

    public ShopBannerImageDeleteService(ShopDetailPersistencePort shopDetailPersistencePort) {
        this.shopDetailPersistencePort = shopDetailPersistencePort;
    }

    @Override
    public void deleteBannerImage(ShopBannerImageDeleteCommand command) {
        Long bannerImageId = command.bannerImageId();

        shopDetailPersistencePort.deleteBannerImageById(bannerImageId);
    }
}
