package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.region.vo.AdminDongId;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.in.ShopDeliveryAreaCreateCommand;
import com.tastyhouse.application.shop.port.in.ShopDeliveryAreaCreateUseCase;

@Service
@Transactional
class ShopDeliveryAreaCreateService implements ShopDeliveryAreaCreateUseCase {

    private final ShopDeliveryAreaService shopDeliveryAreaService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopDeliveryAreaCreateService(
        ShopDeliveryAreaService shopDeliveryAreaService,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopDeliveryAreaService = shopDeliveryAreaService;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public Long addDeliveryArea(ShopDeliveryAreaCreateCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Long adminDongId = command.adminDongId();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ShopId targetShopId = ShopId.of(shopId);
        AdminDongId targetAdminDongId = AdminDongId.of(adminDongId);
        ShopChangeActor actor = ShopChangeActor.ceo(ceoId);
        return shopDeliveryAreaService.addArea(targetShopId, targetAdminDongId, actor);
    }
}
