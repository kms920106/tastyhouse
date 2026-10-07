package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.application.shop.port.in.ShopAmenityAssignUseCase;
import com.tastyhouse.application.shop.port.in.ShopAmenityManagementAssignCommand;

@Service
@Transactional
class ShopAmenityAssignService implements ShopAmenityAssignUseCase {

    private final ShopConvenienceInfoService shopConvenienceInfoService;

    public ShopAmenityAssignService(ShopConvenienceInfoService shopConvenienceInfoService) {
        this.shopConvenienceInfoService = shopConvenienceInfoService;
    }

    @Override
    public Long assignAmenity(ShopAmenityManagementAssignCommand command) {
        Long adminId = command.adminId();
        Long id = command.shopId();
        Long amenityCategoryId = command.amenityCategoryId();

        ShopChangeActor actor = ShopChangeActor.admin(adminId);
        return shopConvenienceInfoService.assignAmenity(id, amenityCategoryId, actor);
    }
}
