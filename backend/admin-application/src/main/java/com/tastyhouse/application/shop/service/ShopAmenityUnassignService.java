package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.application.shop.port.in.ShopAmenityManagementUnassignCommand;
import com.tastyhouse.application.shop.port.in.ShopAmenityUnassignUseCase;

@Service
@Transactional
class ShopAmenityUnassignService implements ShopAmenityUnassignUseCase {

    private final ShopConvenienceInfoService shopConvenienceInfoService;

    public ShopAmenityUnassignService(ShopConvenienceInfoService shopConvenienceInfoService) {
        this.shopConvenienceInfoService = shopConvenienceInfoService;
    }

    @Override
    public void unassignAmenity(ShopAmenityManagementUnassignCommand command) {
        Long adminId = command.adminId();
        Long id = command.shopId();
        Long amenityCategoryId = command.amenityCategoryId();

        ShopChangeActor actor = ShopChangeActor.admin(adminId);
        shopConvenienceInfoService.unassignAmenity(id, amenityCategoryId, actor);
    }
}
