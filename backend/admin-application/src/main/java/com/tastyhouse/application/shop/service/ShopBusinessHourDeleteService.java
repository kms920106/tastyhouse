package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.application.shop.port.in.ShopBusinessHourDeleteUseCase;
import com.tastyhouse.application.shop.port.in.ShopBusinessHourManagementDeleteCommand;

@Service
@Transactional
class ShopBusinessHourDeleteService implements ShopBusinessHourDeleteUseCase {

    private final ShopBusinessHourService shopBusinessHourService;

    public ShopBusinessHourDeleteService(ShopBusinessHourService shopBusinessHourService) {
        this.shopBusinessHourService = shopBusinessHourService;
    }

    @Override
    public void deleteBusinessHour(ShopBusinessHourManagementDeleteCommand command) {
        Long adminId = command.adminId();
        Long businessHourId = command.businessHourId();

        ShopChangeActor actor = ShopChangeActor.admin(adminId);
        shopBusinessHourService.deleteBusinessHour(businessHourId, actor);
    }
}
