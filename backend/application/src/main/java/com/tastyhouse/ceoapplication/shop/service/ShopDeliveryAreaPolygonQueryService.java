package com.tastyhouse.ceoapplication.shop.service;

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

import com.tastyhouse.ceoapplication.shop.port.in.GeoPointCommand;
import com.tastyhouse.ceoapplication.shop.port.in.ShopDeliveryAreaPolygonQueryUseCase;
import com.tastyhouse.domain.region.model.AdminDong;
import com.tastyhouse.domain.region.vo.AdminDongId;
import com.tastyhouse.domain.shop.service.DeliveryAreaProjection;
import com.tastyhouse.domain.shop.service.ShopDeliveryAreaPolicy;
import com.tastyhouse.application.region.port.out.AdminDongCandidateResult;
import com.tastyhouse.application.region.port.out.AdminDongQueryPort;
import com.tastyhouse.application.shared.port.out.GeoRingsPort;
import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaBlockedView;
import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaCandidateView;
import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaPolygonPreviewResult;
import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaPolygonResult;
import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaPolygonViewResult;
import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaQueryPort;
import com.tastyhouse.application.shop.port.out.ShopLocationResult;
import com.tastyhouse.domain.shared.geo.GeoPoint;
import com.tastyhouse.domain.shared.geo.GeoPolygon;

/**
 * 배달지역 도형 조회·미리보기 서비스(CQRS query 측).
 *
 * <p>미리보기는 HTTP 메서드가 {@code POST}지만(도형이 URL에 들어갈 수 없다) <b>의미는 조회</b>이므로
 * {@code @Transactional(readOnly = true)}인 이 클래스에 둔다. 저장하지 않고 "저장하면 무엇이 열리고
 * 무엇이 닫히는지"만 계산한다.
 *
 * <p>환산 판정은 도메인의 {@link DeliveryAreaProjection}을 그대로 쓴다 — 미리보기와 실제 저장이 다른
 * 알고리즘을 쓰면 보여준 결과와 저장 결과가 갈려 기능의 의미가 없어진다.
 *
 * <p>write 포트를 주입하지 않으며({@code queryServicesShouldNotDependOnWritePorts}), 소유권은 좌표 조회에
 * {@code ceoId} 조건을 함께 걸어 강제한다.
 */
@Service
@Transactional(readOnly = true)
public class ShopDeliveryAreaPolygonQueryService implements ShopDeliveryAreaPolygonQueryUseCase {

    /** 후보 프리필터 박스를 넓히는 각도(약 5.5km). 도메인 저장 경로와 같은 값이어야 결과가 일치한다. */
    private static final BigDecimal CANDIDATE_BOX_MARGIN_DEGREES = new BigDecimal("0.05");

    private static final String BLOCKED_REASON_REGION_TIP = "REGION_TIP";

    private final AdminDongQueryPort adminDongQueryPort;
    private final ShopDeliveryAreaQueryPort shopDeliveryAreaQueryPort;
    private final GeoRingsPort geoRingsPort;

    public ShopDeliveryAreaPolygonQueryService(
        AdminDongQueryPort adminDongQueryPort,
        ShopDeliveryAreaQueryPort shopDeliveryAreaQueryPort,
        GeoRingsPort geoRingsPort
    ) {
        this.adminDongQueryPort = adminDongQueryPort;
        this.shopDeliveryAreaQueryPort = shopDeliveryAreaQueryPort;
        this.geoRingsPort = geoRingsPort;
    }

    /**
     * 저장된 도형을 조회한다. <b>미설정은 404가 아니라 {@code exists: false}인 200</b>이다 — 도형 없이
     * 행정동만 직접 등록한 가게가 정상적으로 존재한다.
     */
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

    /**
     * 도형을 환산해 결과를 미리 보여준다(저장하지 않음).
     *
     * <p>{@code blockedAdminDongs}를 함께 계산하므로 점주는 저장에서 409를 맞기 전에 배달팁을 정리할 수
     * 있다 — 저장이 실패한 뒤에야 원인을 알려주면 도형을 다시 그려야 한다고 오해하기 쉽다.
     */
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

        // 닫히는 동 = 기존 도형 파생 행 중 새 환산 결과에 없는 것. 직접 등록분은 도형 저장이 건드리지 않는다.
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

    /**
     * 조회 결과를 도메인 환산이 요구하는 형태로 승격한다. 저장 경로가 write 포트로 읽는 것과 같은 판정을
     * 하도록 같은 도메인 타입으로 맞춘다.
     */
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
