package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopDeliveryAreaRadiusQueryUseCase;
import com.tastyhouse.domain.shop.service.ShopDeliveryAreaPolicy;
import com.tastyhouse.application.region.port.out.AdminDongCandidateResult;
import com.tastyhouse.application.region.port.out.AdminDongQueryPort;
import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaQueryPort;
import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaCandidateView;
import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaRadiusPreviewResult;
import com.tastyhouse.application.shop.port.out.ShopLocationResult;
import com.tastyhouse.domain.shared.geo.GeoCircle;
import com.tastyhouse.domain.shared.geo.GeoPoint;

/**
 * 반경 배달지역 미리보기 서비스(CQRS query 측).
 *
 * <p>저장하지 않고 "이 반경이면 어느 동이 열리는지"만 보여준다. 배달지역은 주문 접수 범위를 직접 바꾸므로,
 * 점주가 결과를 모른 채 적용하지 않도록 미리보기를 먼저 제공한다.
 *
 * <p><b>write 포트를 주입하지 않는다</b>({@code queryServicesShouldNotDependOnWritePorts}) — 조회
 * 트랜잭션에서 쓰기 경로가 열리는 것을 구조적으로 막기 위해 infra query DAO만 쓴다. 소유권 검증도
 * {@code ShopOwnershipValidator} 대신 <b>가게 좌표 조회에 {@code ceoId} 조건을 함께 걸어</b> 수행한다
 * (그 검증기는 내부에 write 포트를 들고 있다).
 *
 * <p>거리 판정은 {@code GeoDistance} 하버사인이다. {@code ShopSearchQueryDao}의
 * {@code METERS_PER_DEGREE = 111000.0} 사각 근사를 재사용하지 않는 이유는, 위경도 양쪽에 같은 값을 써서
 * 위도 37.5°에서 동서 방향이 약 21% 좁기 때문이다 — 200m에서는 무해하지만 7km에서는 약 1.6km가 어긋난다.
 */
@Service
@CeoApp
@Transactional(readOnly = true)
public class ShopDeliveryAreaRadiusQueryService implements ShopDeliveryAreaRadiusQueryUseCase {

    private final AdminDongQueryPort adminDongQueryPort;
    private final ShopDeliveryAreaQueryPort shopDeliveryAreaQueryPort;

    public ShopDeliveryAreaRadiusQueryService(
        AdminDongQueryPort adminDongQueryPort,
        ShopDeliveryAreaQueryPort shopDeliveryAreaQueryPort
    ) {
        this.adminDongQueryPort = adminDongQueryPort;
        this.shopDeliveryAreaQueryPort = shopDeliveryAreaQueryPort;
    }

    @Override
    public ShopDeliveryAreaRadiusPreviewResult previewRadius(Long ceoId, Long shopId, int radiusMeters) {
        ShopDeliveryAreaPolicy.validateRadius(radiusMeters);

        ShopLocationResult shopLocation = shopDeliveryAreaQueryPort.findShopLocation(ceoId, shopId);
        GeoPoint center = GeoPoint.of(shopLocation.latitude(), shopLocation.longitude());

        // 원 근사 다각형은 표시·bbox 계산용이고, "반경 안인가"는 하버사인으로 직접 잰다.
        var circle = GeoCircle.approximate(center, radiusMeters, ShopDeliveryAreaPolicy.CIRCLE_SEGMENTS);
        var candidateBox = circle.boundingBox();

        List<AdminDongCandidateResult> candidates = adminDongQueryPort.findCandidatesWithinBoundingBox(
            candidateBox.minLatitude(),
            candidateBox.maxLatitude(),
            candidateBox.minLongitude(),
            candidateBox.maxLongitude()
        );

        Set<Long> registered = shopDeliveryAreaQueryPort.findAdminDongIds(shopId);

        List<ShopDeliveryAreaCandidateView> withinRadius = candidates.stream()
            .filter(candidate -> hasCenter(candidate) && isWithinRadius(center, candidate, radiusMeters))
            .map(candidate -> toCandidateView(candidate, registered))
            .toList();

        int unresolvedCount = (int) candidates.stream().filter(candidate -> !hasCenter(candidate)).count();

        return new ShopDeliveryAreaRadiusPreviewResult(
            shopLocation.latitude(),
            shopLocation.longitude(),
            radiusMeters,
            ShopDeliveryAreaPolicy.MAX_DELIVERY_RADIUS_METERS,
            ShopDeliveryAreaPolicy.DEFAULT_EXPOSURE_RADIUS_METERS,
            ShopDeliveryAreaGeoMapper.toPointViews(circle),
            withinRadius,
            withinRadius.size(),
            unresolvedCount
        );
    }

    private static boolean hasCenter(AdminDongCandidateResult candidate) {
        return candidate.centerLatitude() != null && candidate.centerLongitude() != null;
    }

    private static boolean isWithinRadius(GeoPoint center, AdminDongCandidateResult candidate, int radiusMeters) {
        GeoPoint candidateCenter = GeoPoint.of(candidate.centerLatitude(), candidate.centerLongitude());
        return center.distanceMetersTo(candidateCenter) <= radiusMeters;
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
