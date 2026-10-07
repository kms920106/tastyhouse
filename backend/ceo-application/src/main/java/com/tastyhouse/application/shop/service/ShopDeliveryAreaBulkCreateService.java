package com.tastyhouse.application.shop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.region.vo.AdminDongId;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.in.ShopDeliveryAreaBulkCreateCommand;
import com.tastyhouse.application.shop.port.in.ShopDeliveryAreaBulkCreateUseCase;
import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaBulkResult;

@Service
@Transactional
class ShopDeliveryAreaBulkCreateService implements ShopDeliveryAreaBulkCreateUseCase {

    private final ShopDeliveryAreaService shopDeliveryAreaService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopDeliveryAreaBulkCreateService(
        ShopDeliveryAreaService shopDeliveryAreaService,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopDeliveryAreaService = shopDeliveryAreaService;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public ShopDeliveryAreaBulkResult addDeliveryAreas(ShopDeliveryAreaBulkCreateCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        List<Long> adminDongIds = command.adminDongIds();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ShopId targetShopId = ShopId.of(shopId);
        ShopChangeActor actor = ShopChangeActor.ceo(ceoId);
        return toBulkResult(shopDeliveryAreaService.addAreas(targetShopId, toAdminDongIds(adminDongIds), actor));
    }

    private ShopDeliveryAreaBulkResult toBulkResult(ShopDeliveryAreaService.BulkResult result) {
        return new ShopDeliveryAreaBulkResult(
            result.requestedCount(),
            result.addedCount(),
            result.skippedCount(),
            result.totalCount()
        );
    }

    private static List<AdminDongId> toAdminDongIds(List<Long> adminDongIds) {
        return adminDongIds.stream().map(AdminDongId::of).toList();
    }
}
