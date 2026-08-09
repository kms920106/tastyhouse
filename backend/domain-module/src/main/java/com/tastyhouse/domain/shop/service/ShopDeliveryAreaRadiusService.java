package com.tastyhouse.domain.shop.service;

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
import com.tastyhouse.domain.shop.model.ShopDeliveryArea;
import com.tastyhouse.domain.shop.repository.ShopDeliveryAreaRepository;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 반경으로 배달가능지역을 일괄 적용한다(도메인 서비스).
 *
 * <p>"가게에서 N km 이내의 행정동을 전부 열기"는 점주가 가장 자주 쓰는 설정 방식이라 도형을 그리지 않고도
 * 쓸 수 있어야 한다. 결과는 {@code MANUAL} 출처로 저장되므로 <b>도형을 나중에 저장해도 지워지지 않는다</b>
 * — 반경으로 깔아둔 기본 범위 위에 도형으로 세부 조정을 얹는 사용 방식을 위해서다.
 *
 * <p><b>후보를 write 포트({@link AdminDongRepository})로 읽는 이유</b>: 이 클래스는 명령 경로이고, 명령이
 * infra query DAO를 주입하면 CQRS 교차 주입 금지 규칙({@code commandServicesShouldNotDependOnQueryDaos})에
 * 걸린다. 여기서 필요한 조회는 표현용 투영이 아니라 <b>불변식 판정을 위한 도메인 로드</b>라 write 포트에
 * 남는 것이 규약에도 맞다.
 *
 * <p>거리 판정은 원 근사 폴리곤이 아니라 하버사인({@code GeoDistance}) 직선거리로 한다 — 근사 다각형은
 * 표시·환산용이고, "반경 안인가"는 직접 재는 쪽이 정확하다.
 */
public class ShopDeliveryAreaRadiusService {

    private final ShopDeliveryAreaRepository shopDeliveryAreaRepository;
    private final AdminDongRepository adminDongRepository;
    private final ShopDeliveryAreaService shopDeliveryAreaService;

    public ShopDeliveryAreaRadiusService(
        ShopDeliveryAreaRepository shopDeliveryAreaRepository,
        AdminDongRepository adminDongRepository,
        ShopDeliveryAreaService shopDeliveryAreaService
    ) {
        this.shopDeliveryAreaRepository = shopDeliveryAreaRepository;
        this.adminDongRepository = adminDongRepository;
        this.shopDeliveryAreaService = shopDeliveryAreaService;
    }

    /**
     * 반경 안에 드는 행정동을 배달가능지역으로 적용한다.
     *
     * @param replace {@code true}면 기존 {@code MANUAL} 행 중 반경 밖의 것을 닫고 교체한다. {@code false}면
     *                기존 설정 위에 더하기만 한다. 교체 시 닫히는 동에 배달팁 참조가 있으면 전체를 409로 막는다.
     */
    public ShopDeliveryAreaService.BulkResult applyRadius(
        ShopId shopId,
        GeoPoint shopLocation,
        int radiusMeters,
        boolean replace,
        Function<Collection<AdminDongId>, List<String>> adminDongNamesById
    ) {
        ShopDeliveryAreaPolicy.validateRadius(radiusMeters);

        Set<AdminDongId> withinRadius = findAdminDongIdsWithinRadius(shopLocation, radiusMeters);

        if (replace) {
            removeManualAreasOutside(shopId, withinRadius, adminDongNamesById);
        }

        return shopDeliveryAreaService.addAreas(shopId, withinRadius);
    }

    /**
     * 반경 안에 대표점이 드는 사용 중 행정동 식별자를 찾는다.
     *
     * <p>바운딩 박스로 후보를 좁힌 뒤 하버사인으로 정밀 판정하는 2단계다 — 박스는 인덱스를 타고, 원 판정은
     * 박스 모서리에 든 동을 걸러낸다.
     */
    public Set<AdminDongId> findAdminDongIdsWithinRadius(GeoPoint center, int radiusMeters) {
        // 원 근사 다각형의 bbox = 원의 bbox. 별도 각도 계산을 두지 않고 GeoCircle 하나만 신뢰한다.
        GeoBoundingBox candidateBox = GeoCircle
            .approximate(center, radiusMeters, ShopDeliveryAreaPolicy.CIRCLE_SEGMENTS)
            .boundingBox();

        return adminDongRepository.findAllWithinBoundingBox(candidateBox).stream()
            .filter(AdminDong::hasCenter)
            .filter(adminDong -> center.distanceMetersTo(adminDong.getCenter()) <= radiusMeters)
            .map(adminDong -> AdminDongId.of(adminDong.getId()))
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * 반경 밖으로 벗어난 기존 {@code MANUAL} 행을 닫는다. 배달팁 참조가 있으면 한 건도 지우지 않고 409.
     *
     * <p>{@code POLYGON} 행은 건드리지 않는다 — 도형은 별도의 편집 원본이므로 반경 적용이 도형 파생분을
     * 지우면 두 설정 방식이 서로를 덮어쓴다.
     */
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
