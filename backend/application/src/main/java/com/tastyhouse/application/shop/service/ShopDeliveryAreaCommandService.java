package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.region.model.AdminDong;
import com.tastyhouse.domain.region.repository.AdminDongRepository;
import com.tastyhouse.domain.region.vo.AdminDongId;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.model.ShopDeliveryArea;
import com.tastyhouse.domain.shop.repository.ShopDeliveryAreaRepository;
import com.tastyhouse.domain.shop.service.ShopDeliveryAreaPolygonService;
import com.tastyhouse.domain.shop.service.ShopDeliveryAreaRadiusService;
import com.tastyhouse.domain.shop.service.ShopDeliveryAreaService;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.shared.geo.GeoPoint;
import com.tastyhouse.domain.shared.geo.GeoPolygon;
import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaBulkDeleteResult;
import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaBulkResult;
import com.tastyhouse.application.shop.port.in.ShopDeliveryAreaBulkCreateCommand;
import com.tastyhouse.application.shop.port.in.ShopDeliveryAreaBulkDeleteCommand;
import com.tastyhouse.application.shop.port.in.ShopDeliveryAreaCommandUseCase;
import com.tastyhouse.application.shop.port.in.ShopDeliveryAreaCreateCommand;
import com.tastyhouse.application.shop.port.in.ShopDeliveryAreaDeleteCommand;
import com.tastyhouse.application.shop.port.in.ShopDeliveryAreaPolygonDeleteCommand;
import com.tastyhouse.application.shop.port.in.ShopDeliveryAreaPolygonSaveCommand;
import com.tastyhouse.application.shop.port.in.ShopDeliveryAreaRadiusApplyCommand;

@Service
@CeoApp
@Transactional
public class ShopDeliveryAreaCommandService implements ShopDeliveryAreaCommandUseCase {

    private final ShopDeliveryAreaService shopDeliveryAreaService;
    private final ShopDeliveryAreaPolygonService shopDeliveryAreaPolygonService;
    private final ShopDeliveryAreaRadiusService shopDeliveryAreaRadiusService;
    private final ShopDeliveryAreaRepository shopDeliveryAreaRepository;
    private final AdminDongRepository adminDongRepository;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopDeliveryAreaCommandService(
        ShopDeliveryAreaService shopDeliveryAreaService,
        ShopDeliveryAreaPolygonService shopDeliveryAreaPolygonService,
        ShopDeliveryAreaRadiusService shopDeliveryAreaRadiusService,
        ShopDeliveryAreaRepository shopDeliveryAreaRepository,
        AdminDongRepository adminDongRepository,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopDeliveryAreaService = shopDeliveryAreaService;
        this.shopDeliveryAreaPolygonService = shopDeliveryAreaPolygonService;
        this.shopDeliveryAreaRadiusService = shopDeliveryAreaRadiusService;
        this.shopDeliveryAreaRepository = shopDeliveryAreaRepository;
        this.adminDongRepository = adminDongRepository;
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

    @Override
    public void removeDeliveryArea(ShopDeliveryAreaDeleteCommand command) {
        Long ceoId = command.ceoId();
        Long deliveryAreaId = command.deliveryAreaId();

        ShopDeliveryArea deliveryArea = shopDeliveryAreaRepository.findById(deliveryAreaId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_DELIVERY_AREA_NOT_FOUND));
        shopOwnershipValidator.validateOwnership(ceoId, deliveryArea.getShopId().value());

        ShopChangeActor actor = ShopChangeActor.ceo(ceoId);
        shopDeliveryAreaService.removeArea(deliveryAreaId, actor);
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

    @Override
    public void deletePolygon(ShopDeliveryAreaPolygonDeleteCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ShopId targetShopId = ShopId.of(shopId);
        ShopChangeActor actor = ShopChangeActor.ceo(ceoId);
        shopDeliveryAreaPolygonService.deletePolygon(targetShopId, this::resolveRegionNames, actor);
    }

    private GeoPoint shopLocationOf(Shop shop) {
        if (shop.getLatitude() == null || shop.getLongitude() == null) {
            throw new BusinessException(
                ErrorCode.SHOP_DELIVERY_AREA_RADIUS_EXCEEDED,
                "가게 좌표가 등록돼 있지 않아 배달지역을 설정할 수 없습니다."
            );
        }
        return GeoPoint.of(shop.getLatitude(), shop.getLongitude());
    }

    private List<String> resolveRegionNames(Collection<AdminDongId> adminDongIds) {
        return adminDongRepository.findAllByIds(adminDongIds).stream()
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

    private static List<AdminDongId> toAdminDongIds(List<Long> adminDongIds) {
        return adminDongIds.stream().map(AdminDongId::of).toList();
    }
}
