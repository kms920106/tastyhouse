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
import com.tastyhouse.domain.shared.geo.GeoPolygon;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.region.port.out.write.AdminDongPersistencePort;
import com.tastyhouse.application.shop.port.in.ShopDeliveryAreaPolygonSaveCommand;
import com.tastyhouse.application.shop.port.in.ShopDeliveryAreaPolygonSaveUseCase;

@Service
@Transactional
class ShopDeliveryAreaPolygonSaveService implements ShopDeliveryAreaPolygonSaveUseCase {

    private final ShopDeliveryAreaPolygonService shopDeliveryAreaPolygonService;
    private final AdminDongPersistencePort adminDongPersistencePort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopDeliveryAreaPolygonSaveService(
        ShopDeliveryAreaPolygonService shopDeliveryAreaPolygonService,
        AdminDongPersistencePort adminDongPersistencePort,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopDeliveryAreaPolygonService = shopDeliveryAreaPolygonService;
        this.adminDongPersistencePort = adminDongPersistencePort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public void savePolygon(ShopDeliveryAreaPolygonSaveCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();

        Shop shop = shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ShopId targetShopId = ShopId.of(shopId);
        GeoPolygon polygon = ShopDeliveryAreaGeoMapper.toPolygon(command.rings());
        ShopChangeActor actor = ShopChangeActor.ceo(ceoId);
        shopDeliveryAreaPolygonService.savePolygon(
            targetShopId,
            polygon,
            shopLocationOf(shop),
            this::resolveRegionNames,
            actor
        );
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
        return adminDongPersistencePort.findAllByIds(adminDongIds).stream()
            .map(AdminDong::fullName)
            .toList();
    }
}
