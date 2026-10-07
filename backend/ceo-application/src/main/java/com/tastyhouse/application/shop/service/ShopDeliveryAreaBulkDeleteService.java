package com.tastyhouse.application.shop.service;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.region.model.AdminDong;
import com.tastyhouse.domain.region.vo.AdminDongId;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.region.port.out.write.AdminDongPersistencePort;
import com.tastyhouse.application.shop.port.in.ShopDeliveryAreaBulkDeleteCommand;
import com.tastyhouse.application.shop.port.in.ShopDeliveryAreaBulkDeleteUseCase;
import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaBulkDeleteResult;

@Service
@Transactional
class ShopDeliveryAreaBulkDeleteService implements ShopDeliveryAreaBulkDeleteUseCase {

    private final ShopDeliveryAreaService shopDeliveryAreaService;
    private final AdminDongPersistencePort adminDongPersistencePort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopDeliveryAreaBulkDeleteService(
        ShopDeliveryAreaService shopDeliveryAreaService,
        AdminDongPersistencePort adminDongPersistencePort,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopDeliveryAreaService = shopDeliveryAreaService;
        this.adminDongPersistencePort = adminDongPersistencePort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public ShopDeliveryAreaBulkDeleteResult removeDeliveryAreas(ShopDeliveryAreaBulkDeleteCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        List<Long> adminDongIds = command.adminDongIds();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ShopId targetShopId = ShopId.of(shopId);
        ShopChangeActor actor = ShopChangeActor.ceo(ceoId);
        ShopDeliveryAreaService.BulkResult result = shopDeliveryAreaService.removeAreas(
            targetShopId, toAdminDongIds(adminDongIds), this::resolveRegionNames, actor
        );
        return new ShopDeliveryAreaBulkDeleteResult(
            result.requestedCount() - result.skippedCount(),
            result.totalCount()
        );
    }

    private List<String> resolveRegionNames(Collection<AdminDongId> adminDongIds) {
        return adminDongPersistencePort.findAllByIds(adminDongIds).stream()
            .map(AdminDong::fullName)
            .toList();
    }

    private static List<AdminDongId> toAdminDongIds(List<Long> adminDongIds) {
        return adminDongIds.stream().map(AdminDongId::of).toList();
    }
}
