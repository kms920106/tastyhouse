package com.tastyhouse.application.shop.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.in.ShopUpdateCommand;
import com.tastyhouse.application.shop.port.in.ShopUpdateUseCase;

@Service
@Transactional
class ShopUpdateService implements ShopUpdateUseCase {

    private final ShopLifecycleService shopLifecycleService;

    public ShopUpdateService(ShopLifecycleService shopLifecycleService) {
        this.shopLifecycleService = shopLifecycleService;
    }

    @Override
    public void updateShop(ShopUpdateCommand command) {
        Long id = command.shopId();
        Long stationId = command.stationId();
        String name = command.name();
        BigDecimal latitude = command.latitude();
        BigDecimal longitude = command.longitude();
        String roadAddress = command.roadAddress();
        String lotAddress = command.lotAddress();
        String phoneNumber = command.phoneNumber();
        Long thumbnailImageFileId = command.thumbnailImageFileId();

        ShopId shopId = ShopId.of(id);
        shopLifecycleService.updateShop(
            shopId, stationId, name, latitude, longitude, roadAddress, lotAddress, phoneNumber, thumbnailImageFileId
        );
    }
}
