package com.tastyhouse.domain.shop.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.tastyhouse.domain.region.model.AdminDong;
import com.tastyhouse.domain.region.repository.AdminDongRepository;
import com.tastyhouse.domain.region.vo.AdminDongId;
import com.tastyhouse.domain.shared.geo.GeoBoundingBox;
import com.tastyhouse.domain.shared.geo.GeoCircle;
import com.tastyhouse.domain.shared.geo.GeoPoint;
import com.tastyhouse.domain.shop.model.DeliveryAreaSource;
import com.tastyhouse.domain.shop.model.ShopChangeActionType;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.model.ShopChangeType;
import com.tastyhouse.domain.shop.model.ShopDeliveryArea;
import com.tastyhouse.domain.shop.repository.ShopDeliveryAreaRepository;
import com.tastyhouse.domain.shop.vo.ShopId;

public class ShopDeliveryAreaRadiusService {
    private final ShopDeliveryAreaRepository shopDeliveryAreaRepository;
    private final AdminDongRepository adminDongRepository;
    private final ShopDeliveryAreaService shopDeliveryAreaService;
    private final ShopChangeHistoryRecorder shopChangeHistoryRecorder;

    public ShopDeliveryAreaRadiusService(
        ShopDeliveryAreaRepository shopDeliveryAreaRepository,
        AdminDongRepository adminDongRepository,
        ShopDeliveryAreaService shopDeliveryAreaService,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder
    ) {
        this.shopDeliveryAreaRepository = shopDeliveryAreaRepository;
        this.adminDongRepository = adminDongRepository;
        this.shopDeliveryAreaService = shopDeliveryAreaService;
        this.shopChangeHistoryRecorder = shopChangeHistoryRecorder;
    }

    public ShopDeliveryAreaService.BulkResult applyRadius(
        ShopId shopId,
        GeoPoint shopLocation,
        int radiusMeters,
        boolean replace,
        Function<Collection<AdminDongId>, List<String>> adminDongNamesById,
        ShopChangeActor actor
    ) {
        ShopDeliveryAreaPolicy.validateRadius(radiusMeters);

        Set<AdminDongId> withinRadius = findAdminDongIdsWithinRadius(shopLocation, radiusMeters);

        if (replace) {
            removeManualAreasOutside(shopId, withinRadius, adminDongNamesById);
        }

        ShopDeliveryAreaService.BulkResult result =
            shopDeliveryAreaService.addAreasWithoutHistory(shopId, withinRadius);

        shopChangeHistoryRecorder.record(
            shopId,
            ShopChangeType.DELIVERY_AREA_RADIUS,
            ShopChangeActionType.UPDATE,
            actor,
            null,
            describeRadius(radiusMeters, replace, result.totalCount())
        );
        return result;
    }

    private String describeRadius(int radiusMeters, boolean replace, int totalCount) {
        return ShopChangeValueFormatter.distanceKm(toKilometers(radiusMeters))
            + " (" + (replace ? "교체 적용" : "추가 적용") + ", 배달가능지역 " + totalCount + "곳)";
    }

    private BigDecimal toKilometers(int meters) {
        return BigDecimal.valueOf(meters).divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP);
    }

    public Set<AdminDongId> findAdminDongIdsWithinRadius(GeoPoint center, int radiusMeters) {
        GeoBoundingBox candidateBox = GeoCircle
            .approximate(center, radiusMeters, ShopDeliveryAreaPolicy.CIRCLE_SEGMENTS)
            .boundingBox();

        return adminDongRepository.findAllWithinBoundingBox(candidateBox).stream()
            .filter(AdminDong::hasCenter)
            .filter(adminDong -> center.distanceMetersTo(adminDong.getCenter()) <= radiusMeters)
            .map(adminDong -> AdminDongId.of(adminDong.getId()))
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private void removeManualAreasOutside(
        ShopId shopId,
        Set<AdminDongId> keep,
        Function<Collection<AdminDongId>, List<String>> adminDongNamesById
    ) {
        List<ShopDeliveryArea> closing = shopDeliveryAreaRepository
            .findByShopIdAndSource(shopId, DeliveryAreaSource.MANUAL).stream()
            .filter(area -> !keep.contains(area.getAdminDongId()))
            .toList();
        if (closing.isEmpty()) {
            return;
        }

        shopDeliveryAreaService.validateNotReferencedByRegionTip(
            shopId,
            closing.stream().map(ShopDeliveryArea::getAdminDongId).toList(),
            adminDongNamesById
        );

        closing.forEach(area -> shopDeliveryAreaRepository.deleteById(area.getId()));
    }
}
