package com.tastyhouse.application.shop.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.tastyhouse.domain.region.model.AdminDong;
import com.tastyhouse.domain.region.vo.AdminDongId;
import com.tastyhouse.domain.shared.geo.GeoBoundingBox;
import com.tastyhouse.domain.shared.geo.GeoPoint;
import com.tastyhouse.domain.shared.geo.GeoPolygon;
import com.tastyhouse.domain.shop.model.DeliveryAreaProjection;
import com.tastyhouse.domain.shop.model.DeliveryAreaSource;
import com.tastyhouse.domain.shop.model.ShopChangeActionType;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.model.ShopChangeType;
import com.tastyhouse.domain.shop.model.ShopChangeValueFormatter;
import com.tastyhouse.domain.shop.model.ShopDeliveryArea;
import com.tastyhouse.domain.shop.model.ShopDeliveryAreaPolicy;
import com.tastyhouse.domain.shop.model.ShopDeliveryAreaPolygon;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.region.port.out.write.AdminDongLoadPort;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.CeoErrorCode;
import com.tastyhouse.application.shop.port.out.write.ShopDeliveryAreaLoadPort;
import com.tastyhouse.application.shop.port.out.write.ShopDeliveryAreaPolygonLoadPort;
import com.tastyhouse.application.shop.port.out.write.ShopDeliveryAreaPolygonSavePort;
import com.tastyhouse.application.shop.port.out.write.ShopDeliveryAreaSavePort;
import com.tastyhouse.application.shop.port.out.write.ShopDeliveryTipRegionLoadPort;

@Service
public class ShopDeliveryAreaPolygonService {

    private static final BigDecimal CANDIDATE_BOX_MARGIN_DEGREES = new BigDecimal("0.05");

    private final ShopDeliveryAreaPolygonLoadPort shopDeliveryAreaPolygonLoadPort;
    private final ShopDeliveryAreaPolygonSavePort shopDeliveryAreaPolygonSavePort;
    private final ShopDeliveryAreaLoadPort shopDeliveryAreaLoadPort;
    private final ShopDeliveryAreaSavePort shopDeliveryAreaSavePort;
    private final AdminDongLoadPort adminDongLoadPort;
    private final ShopDeliveryTipRegionLoadPort shopDeliveryTipRegionLoadPort;
    private final ShopChangeHistoryRecorder shopChangeHistoryRecorder;

    public ShopDeliveryAreaPolygonService(
        ShopDeliveryAreaPolygonLoadPort shopDeliveryAreaPolygonLoadPort,
        ShopDeliveryAreaPolygonSavePort shopDeliveryAreaPolygonSavePort,
        ShopDeliveryAreaLoadPort shopDeliveryAreaLoadPort,
        ShopDeliveryAreaSavePort shopDeliveryAreaSavePort,
        AdminDongLoadPort adminDongLoadPort,
        ShopDeliveryTipRegionLoadPort shopDeliveryTipRegionLoadPort,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder
    ) {
        this.shopDeliveryAreaPolygonLoadPort = shopDeliveryAreaPolygonLoadPort;
        this.shopDeliveryAreaPolygonSavePort = shopDeliveryAreaPolygonSavePort;
        this.shopDeliveryAreaLoadPort = shopDeliveryAreaLoadPort;
        this.shopDeliveryAreaSavePort = shopDeliveryAreaSavePort;
        this.adminDongLoadPort = adminDongLoadPort;
        this.shopDeliveryTipRegionLoadPort = shopDeliveryTipRegionLoadPort;
        this.shopChangeHistoryRecorder = shopChangeHistoryRecorder;
    }

    public void savePolygon(
        ShopId shopId,
        GeoPolygon polygon,
        GeoPoint shopLocation,
        Function<Collection<AdminDongId>, List<String>> adminDongNamesById,
        ShopChangeActor actor
    ) {
        ShopDeliveryAreaPolicy.validateShape(polygon);
        ShopDeliveryAreaPolicy.validateWithinMaxRadius(polygon, shopLocation);

        DeliveryAreaProjection.Result projection = project(polygon);
        if (projection.isEmpty()) {
            throw new ApplicationException(CeoErrorCode.SHOP_DELIVERY_AREA_EMPTY_PROJECTION);
        }

        Set<AdminDongId> manualDongIds = adminDongIdsOf(DeliveryAreaSource.MANUAL, shopId);
        Set<AdminDongId> currentPolygonDongIds = adminDongIdsOf(DeliveryAreaSource.POLYGON, shopId);
        Set<AdminDongId> projected = new LinkedHashSet<>(projection.adminDongIds());

        Set<AdminDongId> toInsert = projected.stream()
            .filter(adminDongId -> !manualDongIds.contains(adminDongId))
            .collect(Collectors.toCollection(LinkedHashSet::new));

        ShopDeliveryAreaPolicy.validateTotalCount(manualDongIds.size() + toInsert.size());

        Set<AdminDongId> closing = currentPolygonDongIds.stream()
            .filter(adminDongId -> !projected.contains(adminDongId))
            .collect(Collectors.toCollection(LinkedHashSet::new));
        validateNotReferencedByRegionTip(shopId, closing, adminDongNamesById);

        String previousValue = describePolygon(
            shopDeliveryAreaPolygonLoadPort.findByShopId(shopId).orElse(null)
        );

        shopDeliveryAreaSavePort.deleteByShopIdAndSource(shopId, DeliveryAreaSource.POLYGON);
        if (!toInsert.isEmpty()) {
            shopDeliveryAreaSavePort.saveAll(
                toInsert.stream()
                    .map(adminDongId -> ShopDeliveryArea.of(shopId, adminDongId, DeliveryAreaSource.POLYGON))
                    .toList()
            );
        }

        upsertPolygon(shopId, polygon, shopLocation);

        shopChangeHistoryRecorder.record(
            shopId,
            ShopChangeType.DELIVERY_AREA_POLYGON,
            ShopChangeActionType.UPDATE,
            actor,
            previousValue,
            describeShape(polygon, projected.size())
        );
    }

    public void deletePolygon(
        ShopId shopId,
        Function<Collection<AdminDongId>, List<String>> adminDongNamesById,
        ShopChangeActor actor
    ) {
        Set<AdminDongId> polygonDongIds = adminDongIdsOf(DeliveryAreaSource.POLYGON, shopId);
        validateNotReferencedByRegionTip(shopId, polygonDongIds, adminDongNamesById);

        ShopDeliveryAreaPolygon stored = shopDeliveryAreaPolygonLoadPort.findByShopId(shopId).orElse(null);
        if (stored == null) {
            shopDeliveryAreaSavePort.deleteByShopIdAndSource(shopId, DeliveryAreaSource.POLYGON);
            return;
        }
        String previousValue = describePolygon(stored);

        shopDeliveryAreaSavePort.deleteByShopIdAndSource(shopId, DeliveryAreaSource.POLYGON);
        shopDeliveryAreaPolygonSavePort.deleteByShopId(shopId);

        shopChangeHistoryRecorder.record(
            shopId,
            ShopChangeType.DELIVERY_AREA_POLYGON,
            ShopChangeActionType.DELETE,
            actor,
            previousValue,
            null
        );
    }

    private String describePolygon(ShopDeliveryAreaPolygon storedPolygon) {
        if (storedPolygon == null) {
            return ShopChangeValueFormatter.unset();
        }
        return "도형 " + storedPolygon.getRingCount() + "개, 꼭짓점 " + storedPolygon.getVertexCount()
            + "개, 최원거리 " + ShopChangeValueFormatter.distanceKm(toKilometers(storedPolygon.getMaxRadiusMeters()));
    }

    private String describeShape(GeoPolygon polygon, int projectedDongCount) {
        return "도형 " + polygon.ringCount() + "개, 꼭짓점 " + polygon.vertexCount()
            + "개, 행정동 " + projectedDongCount + "곳";
    }

    private BigDecimal toKilometers(int meters) {
        return BigDecimal.valueOf(meters).divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP);
    }

    public DeliveryAreaProjection.Result project(GeoPolygon polygon) {
        GeoBoundingBox candidateBox = polygon.boundingBox().expand(CANDIDATE_BOX_MARGIN_DEGREES);
        List<AdminDong> candidates = adminDongLoadPort.findAllActiveWithinBoundingBox(candidateBox);
        return DeliveryAreaProjection.project(polygon, candidates);
    }

    private void upsertPolygon(ShopId shopId, GeoPolygon polygon, GeoPoint shopLocation) {
        ShopDeliveryAreaPolygon stored = shopDeliveryAreaPolygonLoadPort.findByShopId(shopId)
            .orElse(null);

        if (stored == null) {
            shopDeliveryAreaPolygonSavePort.save(ShopDeliveryAreaPolygon.of(shopId, polygon, shopLocation));
            return;
        }
        stored.replace(polygon, shopLocation);
        shopDeliveryAreaPolygonSavePort.save(stored);
    }

    private Set<AdminDongId> adminDongIdsOf(DeliveryAreaSource source, ShopId shopId) {
        return shopDeliveryAreaLoadPort.findByShopIdAndSource(shopId, source).stream()
            .map(ShopDeliveryArea::getAdminDongId)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private void validateNotReferencedByRegionTip(
        ShopId shopId,
        Collection<AdminDongId> closing,
        Function<Collection<AdminDongId>, List<String>> adminDongNamesById
    ) {
        if (closing.isEmpty()) {
            return;
        }

        Set<AdminDongId> referenced = shopDeliveryTipRegionLoadPort.findRegionTipAdminDongIds(shopId);
        List<AdminDongId> blocked = closing.stream()
            .filter(referenced::contains)
            .toList();
        if (blocked.isEmpty()) {
            return;
        }

        List<String> blockedNames = adminDongNamesById == null ? List.of() : adminDongNamesById.apply(blocked);
        String message = CeoErrorCode.SHOP_DELIVERY_AREA_IN_USE.getDefaultMessage();
        if (!blockedNames.isEmpty()) {
            message = message + ": " + String.join(", ", blockedNames);
        }
        throw new ApplicationException(CeoErrorCode.SHOP_DELIVERY_AREA_IN_USE, message);
    }
}
