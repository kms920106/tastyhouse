package com.tastyhouse.application.shop.service;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.region.model.AdminDong;
import com.tastyhouse.domain.region.vo.AdminDongId;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.region.port.out.write.AdminDongLoadPort;
import com.tastyhouse.application.shop.port.in.ShopDeliveryAreaPolygonDeleteCommand;
import com.tastyhouse.application.shop.port.in.ShopDeliveryAreaPolygonDeleteUseCase;

@Service
@Transactional
class ShopDeliveryAreaPolygonDeleteService implements ShopDeliveryAreaPolygonDeleteUseCase {

    private final ShopDeliveryAreaPolygonService shopDeliveryAreaPolygonService;
    private final AdminDongLoadPort adminDongLoadPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopDeliveryAreaPolygonDeleteService(
        ShopDeliveryAreaPolygonService shopDeliveryAreaPolygonService,
        AdminDongLoadPort adminDongLoadPort,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopDeliveryAreaPolygonService = shopDeliveryAreaPolygonService;
        this.adminDongLoadPort = adminDongLoadPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public void deletePolygon(ShopDeliveryAreaPolygonDeleteCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ShopId targetShopId = ShopId.of(shopId);
        ShopChangeActor actor = ShopChangeActor.ceo(ceoId);
        shopDeliveryAreaPolygonService.deletePolygon(targetShopId, this::resolveRegionNames, actor);
    }

    private List<String> resolveRegionNames(Collection<AdminDongId> adminDongIds) {
        return adminDongLoadPort.findAllByIds(adminDongIds).stream()
            .map(AdminDong::fullName)
            .toList();
    }
}
