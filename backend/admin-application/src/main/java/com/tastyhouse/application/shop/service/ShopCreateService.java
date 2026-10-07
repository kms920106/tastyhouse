package com.tastyhouse.application.shop.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.application.shop.port.in.ShopCreateCommand;
import com.tastyhouse.application.shop.port.in.ShopCreateUseCase;

@Service
@Transactional
class ShopCreateService implements ShopCreateUseCase {

    private final ShopLifecycleService shopLifecycleService;

    public ShopCreateService(ShopLifecycleService shopLifecycleService) {
        this.shopLifecycleService = shopLifecycleService;
    }

    @Override
    public Long createShop(ShopCreateCommand command) {
        Long adminId = command.adminId();
        Long ceoId = command.ceoId();
        Long stationId = command.stationId();
        String name = command.name();
        BigDecimal latitude = command.latitude();
        BigDecimal longitude = command.longitude();
        String roadAddress = command.roadAddress();
        String lotAddress = command.lotAddress();
        String phoneNumber = command.phoneNumber();
        Long thumbnailImageFileId = command.thumbnailImageFileId();

        Shop shop = shopLifecycleService.createShop(
            adminId, ceoId, stationId, name, latitude, longitude, roadAddress, lotAddress, phoneNumber,
            thumbnailImageFileId
        );
        return shop.getId();
    }
}
