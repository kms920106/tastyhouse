package com.tastyhouse.application.shop.service;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.DomainErrorCode;
import com.tastyhouse.domain.exception.DomainException;
import com.tastyhouse.domain.region.model.AdminDong;
import com.tastyhouse.domain.region.vo.AdminDongId;
import com.tastyhouse.domain.shared.geo.GeoPoint;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.region.port.out.write.AdminDongLoadPort;
import com.tastyhouse.application.shop.port.in.ShopDeliveryAreaRadiusApplyCommand;
import com.tastyhouse.application.shop.port.in.ShopDeliveryAreaRadiusApplyUseCase;
import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaBulkResult;

@Service
@Transactional
class ShopDeliveryAreaRadiusApplyService implements ShopDeliveryAreaRadiusApplyUseCase {

    private final ShopDeliveryAreaRadiusService shopDeliveryAreaRadiusService;
    private final AdminDongLoadPort adminDongLoadPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopDeliveryAreaRadiusApplyService(
        ShopDeliveryAreaRadiusService shopDeliveryAreaRadiusService,
        AdminDongLoadPort adminDongLoadPort,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopDeliveryAreaRadiusService = shopDeliveryAreaRadiusService;
        this.adminDongLoadPort = adminDongLoadPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public ShopDeliveryAreaBulkResult applyRadius(ShopDeliveryAreaRadiusApplyCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        int radiusMeters = command.radiusMeters();
        boolean replace = command.replace();

        Shop shop = shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ShopId targetShopId = ShopId.of(shopId);
        ShopChangeActor actor = ShopChangeActor.ceo(ceoId);
        return toBulkResult(shopDeliveryAreaRadiusService.applyRadius(
            targetShopId,
            shopLocationOf(shop),
            radiusMeters,
            replace,
            this::resolveRegionNames,
            actor
        ));
    }

    private GeoPoint shopLocationOf(Shop shop) {
        if (shop.getLatitude() == null || shop.getLongitude() == null) {
            throw new DomainException(
                DomainErrorCode.SHOP_DELIVERY_AREA_RADIUS_EXCEEDED,
                "가게 좌표가 등록돼 있지 않아 배달지역을 설정할 수 없습니다."
            );
        }
        return GeoPoint.of(shop.getLatitude(), shop.getLongitude());
    }

    private List<String> resolveRegionNames(Collection<AdminDongId> adminDongIds) {
        return adminDongLoadPort.findAllByIds(adminDongIds).stream()
            .map(AdminDong::fullName)
            .toList();
    }

    private ShopDeliveryAreaBulkResult toBulkResult(ShopDeliveryAreaService.BulkResult result) {
        return new ShopDeliveryAreaBulkResult(
            result.requestedCount(),
            result.addedCount(),
            result.skippedCount(),
            result.totalCount()
        );
    }
}
