package com.tastyhouse.application.shop.service;

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

import com.tastyhouse.domain.region.model.AdminDong;
import com.tastyhouse.domain.region.vo.AdminDongId;
import com.tastyhouse.domain.shared.geo.GeoPoint;
import com.tastyhouse.domain.shared.geo.GeoPolygon;
import com.tastyhouse.domain.shared.geo.GeoPolygonTextCodec;
import com.tastyhouse.domain.shop.model.DeliveryAreaProjection;
import com.tastyhouse.domain.shop.model.ShopDeliveryAreaPolicy;
import com.tastyhouse.application.region.port.out.AdminDongCandidateResult;
import com.tastyhouse.application.region.port.out.AdminDongQueryPort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shop.port.in.GeoPointCommand;
import com.tastyhouse.application.shop.port.in.ShopDeliveryAreaPolygonPreviewQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaBlockedView;
import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaCandidateView;
import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaPolygonPreviewResult;
import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaQueryPort;
import com.tastyhouse.application.shop.port.out.ShopLocationResult;

@Service
@Transactional(readOnly = true)
class ShopDeliveryAreaPolygonPreviewQueryService implements ShopDeliveryAreaPolygonPreviewQueryUseCase {

    private static final BigDecimal CANDIDATE_BOX_MARGIN_DEGREES = new BigDecimal("0.05");

    private static final String BLOCKED_REASON_REGION_TIP = "REGION_TIP";

    private final AdminDongQueryPort adminDongQueryPort;
    private final ShopDeliveryAreaQueryPort shopDeliveryAreaQueryPort;

    public ShopDeliveryAreaPolygonPreviewQueryService(
        AdminDongQueryPort adminDongQueryPort,
        ShopDeliveryAreaQueryPort shopDeliveryAreaQueryPort
    ) {
        this.adminDongQueryPort = adminDongQueryPort;
        this.shopDeliveryAreaQueryPort = shopDeliveryAreaQueryPort;
    }

    @Override
    public ShopDeliveryAreaPolygonPreviewResult previewPolygon(
        Long ceoId,
        Long shopId,
        List<List<GeoPointCommand>> rings
    ) {
        ShopLocationResult shopLocation =
            ShopDeliveryAreaGeoMapper.requireCoordinates(shopDeliveryAreaQueryPort.findShopLocation(ceoId, shopId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.SHOP_ACCESS_DENIED)));
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
                GeoPolygonTextCodec.decodeRings(candidate.boundary())
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
