package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.GeoPointCommand;
import com.tastyhouse.application.shop.port.in.ShopDeliveryAreaPolygonQueryUseCase;
import com.tastyhouse.domain.region.model.AdminDong;
import com.tastyhouse.domain.region.vo.AdminDongId;
import com.tastyhouse.domain.shop.service.DeliveryAreaProjection;
import com.tastyhouse.domain.shop.service.ShopDeliveryAreaPolicy;
import com.tastyhouse.application.region.port.out.AdminDongCandidateResult;
import com.tastyhouse.application.region.port.out.AdminDongQueryPort;
import com.tastyhouse.application.shared.port.out.GeoRingsQueryPort;
import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaBlockedView;
import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaCandidateView;
import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaPolygonPreviewResult;
import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaPolygonResult;
import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaPolygonViewResult;
import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaQueryPort;
import com.tastyhouse.application.shop.port.out.ShopLocationResult;
import com.tastyhouse.domain.shared.geo.GeoPoint;
import com.tastyhouse.domain.shared.geo.GeoPolygon;

@Service
@CeoApp
@Transactional(readOnly = true)
public class ShopDeliveryAreaPolygonQueryService implements ShopDeliveryAreaPolygonQueryUseCase {

    private static final BigDecimal CANDIDATE_BOX_MARGIN_DEGREES = new BigDecimal("0.05");

    private static final String BLOCKED_REASON_REGION_TIP = "REGION_TIP";

    private final AdminDongQueryPort adminDongQueryPort;
    private final ShopDeliveryAreaQueryPort shopDeliveryAreaQueryPort;
    private final GeoRingsQueryPort geoRingsPort;

    public ShopDeliveryAreaPolygonQueryService(
        AdminDongQueryPort adminDongQueryPort,
        ShopDeliveryAreaQueryPort shopDeliveryAreaQueryPort,
        GeoRingsQueryPort geoRingsPort
    ) {
        this.adminDongQueryPort = adminDongQueryPort;
        this.shopDeliveryAreaQueryPort = shopDeliveryAreaQueryPort;
        this.geoRingsPort = geoRingsPort;
    }

    @Override
    public ShopDeliveryAreaPolygonViewResult getPolygon(Long ceoId, Long shopId) {
        ShopLocationResult shopLocation = shopDeliveryAreaQueryPort.findShopLocation(ceoId, shopId);
        ShopDeliveryAreaPolygonResult stored = shopDeliveryAreaQueryPort.findPolygon(shopId).orElse(null);

        if (stored == null) {
            return new ShopDeliveryAreaPolygonViewResult(
                false, null, null, null,
                shopLocation.latitude(), shopLocation.longitude(),
                0, null,
                ShopDeliveryAreaPolicy.MAX_DELIVERY_RADIUS_METERS,
                ShopDeliveryAreaPolicy.DEFAULT_EXPOSURE_RADIUS_METERS,
                null, null, 0, null
            );
        }

        GeoPolygon polygon = geoRingsPort.resolvePolygon(stored.rings());
        GeoPoint storedCenter = GeoPoint.of(stored.centerLatitude(), stored.centerLongitude());
        GeoPoint currentLocation = GeoPoint.of(shopLocation.latitude(), shopLocation.longitude());

        return new ShopDeliveryAreaPolygonViewResult(
            true,
            ShopDeliveryAreaGeoMapper.toRingViews(polygon),
            stored.centerLatitude(),
            stored.centerLongitude(),
            shopLocation.latitude(),
            shopLocation.longitude(),
            (int) Math.round(storedCenter.distanceMetersTo(currentLocation)),
            stored.maxRadiusMeters(),
            ShopDeliveryAreaPolicy.MAX_DELIVERY_RADIUS_METERS,
            ShopDeliveryAreaPolicy.DEFAULT_EXPOSURE_RADIUS_METERS,
            stored.ringCount(),
            stored.vertexCount(),
            shopDeliveryAreaQueryPort.findAdminDongIdsBySource(shopId, "POLYGON").size(),
            stored.updatedAt()
        );
    }

    @Override
    public ShopDeliveryAreaPolygonPreviewResult previewPolygon(
        Long ceoId,
        Long shopId,
        List<List<GeoPointCommand>> rings
    ) {
        ShopLocationResult shopLocation = shopDeliveryAreaQueryPort.findShopLocation(ceoId, shopId);
        GeoPolygon polygon = ShopDeliveryAreaGeoMapper.toPolygon(rings);
        ShopDeliveryAreaPolicy.validateShape(polygon);

        GeoPoint center = GeoPoint.of(shopLocation.latitude(), shopLocation.longitude());
        int maxRadiusMeters = (int) Math.ceil(polygon.maxDistanceMetersFrom(center));

        List<AdminDongCandidateResult> candidates = loadCandidates(polygon);
        Map<Long, AdminDongCandidateResult> candidateById = new LinkedHashMap<>();
        candidates.forEach(candidate -> candidateById.put(candidate.adminDongId(), candidate));

        DeliveryAreaProjection.Result projection = DeliveryAreaProjection.project(polygon, toDomainCandidates(candidates));
        Set<Long> projected = projection.adminDongIds().stream()
            .map(AdminDongId::value)
            .collect(Collectors.toCollection(LinkedHashSet::new));

        Set<Long> registered = shopDeliveryAreaQueryPort.findAdminDongIds(shopId);
        Set<Long> currentPolygonDongs = shopDeliveryAreaQueryPort.findAdminDongIdsBySource(shopId, "POLYGON");
        Set<Long> regionTipDongs = shopDeliveryAreaQueryPort.findRegionTipAdminDongIds(shopId);

        List<ShopDeliveryAreaCandidateView> projectedViews = projected.stream()
            .map(candidateById::get)
            .filter(Objects::nonNull)
            .map(candidate -> toCandidateView(candidate, registered))
            .toList();

        List<ShopDeliveryAreaCandidateView> added = projectedViews.stream()
            .filter(candidate -> !registered.contains(candidate.adminDongId()))
            .toList();

        List<Long> closing = currentPolygonDongs.stream()
            .filter(adminDongId -> !projected.contains(adminDongId))
            .toList();

        return new ShopDeliveryAreaPolygonPreviewResult(
            maxRadiusMeters,
            !ShopDeliveryAreaPolicy.exceedsMaxRadius(maxRadiusMeters),
            projectedViews,
            added,
            toRemovedViews(closing),
            toBlockedViews(closing, regionTipDongs),
            projection.unresolvedCount()
        );
    }

    private List<AdminDongCandidateResult> loadCandidates(GeoPolygon polygon) {
        var candidateBox = polygon.boundingBox().expand(CANDIDATE_BOX_MARGIN_DEGREES);
        return adminDongQueryPort.findCandidatesWithinBoundingBox(
            candidateBox.minLatitude(),
            candidateBox.maxLatitude(),
            candidateBox.minLongitude(),
            candidateBox.maxLongitude()
        );
    }

    private List<AdminDong> toDomainCandidates(List<AdminDongCandidateResult> candidates) {
        List<AdminDong> domainCandidates = new ArrayList<>(candidates.size());
        for (AdminDongCandidateResult candidate : candidates) {
            GeoPoint candidateCenter = candidate.centerLatitude() == null || candidate.centerLongitude() == null
                ? null
                : GeoPoint.of(candidate.centerLatitude(), candidate.centerLongitude());

            domainCandidates.add(AdminDong.reconstitute(
                candidate.adminDongId(),
                null,
                null,
                null,
                null,
                true,
                candidateCenter,
                geoRingsPort.resolveRings(candidate.boundary())
            ));
        }
        return domainCandidates;
    }

    private List<ShopDeliveryAreaCandidateView> toRemovedViews(List<Long> closing) {
        if (closing.isEmpty()) {
            return List.of();
        }

        return adminDongQueryPort.findBoundariesByIds(closing).stream()
            .map(dto -> new ShopDeliveryAreaCandidateView(
                dto.adminDongId(),
                dto.regionName(),
                dto.centerLatitude(),
                dto.centerLongitude(),
                true
            ))
            .toList();
    }

    private List<ShopDeliveryAreaBlockedView> toBlockedViews(List<Long> closing, Set<Long> regionTipDongs) {
        List<Long> blocked = closing.stream().filter(regionTipDongs::contains).toList();
        if (blocked.isEmpty()) {
            return List.of();
        }

        return adminDongQueryPort.findBoundariesByIds(blocked).stream()
            .map(dto -> new ShopDeliveryAreaBlockedView(
                dto.adminDongId(),
                dto.regionName(),
                BLOCKED_REASON_REGION_TIP
            ))
            .toList();
    }

    private ShopDeliveryAreaCandidateView toCandidateView(AdminDongCandidateResult dto, Set<Long> registered) {
        return new ShopDeliveryAreaCandidateView(
            dto.adminDongId(),
            dto.regionName(),
            dto.centerLatitude(),
            dto.centerLongitude(),
            registered.contains(dto.adminDongId())
        );
    }
}
