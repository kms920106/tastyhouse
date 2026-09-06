package com.tastyhouse.domain.shop.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.region.model.AdminDong;
import com.tastyhouse.domain.region.repository.AdminDongRepository;
import com.tastyhouse.domain.region.vo.AdminDongId;
import com.tastyhouse.domain.shared.geo.GeoBoundingBox;
import com.tastyhouse.domain.shared.geo.GeoPoint;
import com.tastyhouse.domain.shared.geo.GeoPolygon;
import com.tastyhouse.domain.shop.model.DeliveryAreaSource;
import com.tastyhouse.domain.shop.model.ShopChangeActionType;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.model.ShopChangeType;
import com.tastyhouse.domain.shop.model.ShopDeliveryArea;
import com.tastyhouse.domain.shop.model.ShopDeliveryAreaPolygon;
import com.tastyhouse.domain.shop.repository.ShopDeliveryAreaPolygonRepository;
import com.tastyhouse.domain.shop.repository.ShopDeliveryAreaRepository;
import com.tastyhouse.domain.shop.repository.ShopDeliveryTipRegionLookup;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 배달지역 도형 저장·삭제 오케스트레이션(도메인 서비스).
 *
 * <p>도형 저장은 <b>두 테이블을 함께 바꾸는</b> 연산이다 — 도형 원본({@code SHOP_DELIVERY_AREA_POLYGON})과
 * 그것을 환산한 행정동 집합({@code SHOP_DELIVERY_AREA})이 항상 같은 트랜잭션에서 일치해야 한다. 이
 * 일관성이 깨지면 "저장은 됐는데 주문은 계속 거절되는" 상태가 생기므로 순서와 검증을 한 곳에 모은다.
 *
 * <p><b>환산을 비동기로 미루지 않는 이유</b>: 미루면 도형은 저장됐지만 행정동 집합이 아직 비어 있는 창이
 * 생긴다. 그 창에서 {@code countByShopId == 0}이 되면 주문 접수의 지역 검사가 <b>통째로 비활성</b>되어
 * (미설정=전 지역 허용) 배달 불가 지역 주문이 그대로 접수된다. 후보가 7km 내로 국한돼 수십~수백 건이라
 * 트랜잭션 안에서 처리해도 밀리초 단위다.
 *
 * <p><b>변경이력({@code DELIVERY_AREA_POLYGON})도 이 서비스가 남긴다.</b> 도형은 전체 교체이므로 저장·삭제
 * 각 1회당 1행이며, 변경 전 도형은 덮어쓰기 전에 읽어야만 얻을 수 있다. 파생된 행정동 행 변화를
 * {@code DELIVERY_AREA}로 따로 남기지 않는 이유는 점주가 한 조작이 "도형 저장" 하나이기 때문이다
 * ({@link ShopDeliveryAreaRadiusService}와 같은 판단).
 *
 * <p>{@code @Service}/{@code @Transactional} 없는 순수 POJO이며, 빈 등록과 트랜잭션 경계는 바깥 계층이
 * 담당한다. 점주 소유권 검증은 ceo-api의 {@code ShopOwnershipValidator} 책임이라 여기서 다루지 않는다.
 */
public class ShopDeliveryAreaPolygonService {

    /**
     * 후보 프리필터 바운딩 박스를 넓히는 각도(약 5.5km).
     *
     * <p>대표점은 동의 중심 부근이므로, 도형 경계에 걸친 큰 동은 대표점이 도형 bbox 밖에 있을 수 있다.
     * 여유를 주지 않으면 그런 동이 후보 단계에서 탈락해 <b>2차 규칙(경계 샘플)까지 가보지도 못한다.</b>
     */
    private static final BigDecimal CANDIDATE_BOX_MARGIN_DEGREES = new BigDecimal("0.05");

    private final ShopDeliveryAreaPolygonRepository shopDeliveryAreaPolygonRepository;
    private final ShopDeliveryAreaRepository shopDeliveryAreaRepository;
    private final AdminDongRepository adminDongRepository;
    private final ShopDeliveryTipRegionLookup shopDeliveryTipRegionLookup;
    private final ShopChangeHistoryRecorder shopChangeHistoryRecorder;

    public ShopDeliveryAreaPolygonService(
        ShopDeliveryAreaPolygonRepository shopDeliveryAreaPolygonRepository,
        ShopDeliveryAreaRepository shopDeliveryAreaRepository,
        AdminDongRepository adminDongRepository,
        ShopDeliveryTipRegionLookup shopDeliveryTipRegionLookup,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder
    ) {
        this.shopDeliveryAreaPolygonRepository = shopDeliveryAreaPolygonRepository;
        this.shopDeliveryAreaRepository = shopDeliveryAreaRepository;
        this.adminDongRepository = adminDongRepository;
        this.shopDeliveryTipRegionLookup = shopDeliveryTipRegionLookup;
        this.shopChangeHistoryRecorder = shopChangeHistoryRecorder;
    }

    /**
     * 도형을 저장하고 그 자리에서 행정동 집합으로 환산한다(전체 교체).
     *
     * <p>검증·반영 순서는 <b>되돌릴 수 없는 쓰기를 마지막에</b> 두도록 짜여 있다. 앞선 어떤 단계에서
     * 예외가 나도 트랜잭션이 롤백되므로 부분 반영이 남지 않는다.
     *
     * @param shopId             대상 가게
     * @param polygon            저장할 도형
     * @param shopLocation       현재 가게 좌표(7km 상한의 기준점이자 저장될 스냅샷)
     * @param adminDongNamesById 차단 메시지에 이름을 나열하기 위한 표시명 조회 함수
     */
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
            // 0건 저장을 허용하면 "미설정 = 전 지역 허용" 규칙과 만나 좁게 그릴수록 넓게 열리는 역전이 생긴다.
            throw new BusinessException(ErrorCode.SHOP_DELIVERY_AREA_EMPTY_PROJECTION);
        }

        Set<AdminDongId> manualDongIds = adminDongIdsOf(DeliveryAreaSource.MANUAL, shopId);
        Set<AdminDongId> currentPolygonDongIds = adminDongIdsOf(DeliveryAreaSource.POLYGON, shopId);
        Set<AdminDongId> projected = new LinkedHashSet<>(projection.adminDongIds());

        // MANUAL로 이미 열린 동은 파생 행으로 다시 넣지 않는다(유니크 제약 위반이자 출처 강등).
        Set<AdminDongId> toInsert = projected.stream()
            .filter(adminDongId -> !manualDongIds.contains(adminDongId))
            .collect(Collectors.toCollection(LinkedHashSet::new));

        ShopDeliveryAreaPolicy.validateTotalCount(manualDongIds.size() + toInsert.size());

        // 이번 저장으로 닫히는 동(기존 POLYGON 행 중 새 환산 결과에 없는 것)에 배달팁 참조가 있으면 전체 롤백.
        Set<AdminDongId> closing = currentPolygonDongIds.stream()
            .filter(adminDongId -> !projected.contains(adminDongId))
            .collect(Collectors.toCollection(LinkedHashSet::new));
        validateNotReferencedByRegionTip(shopId, closing, adminDongNamesById);

        // 이력의 변경 전 값은 도형을 덮어쓰기 전에만 얻을 수 있다.
        String previousValue = describePolygon(
            shopDeliveryAreaPolygonRepository.findByShopId(shopId).orElse(null)
        );

        shopDeliveryAreaRepository.deleteByShopIdAndSource(shopId, DeliveryAreaSource.POLYGON);
        if (!toInsert.isEmpty()) {
            shopDeliveryAreaRepository.saveAll(
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

    /**
     * 도형과 그로부터 파생된 행정동 행을 삭제한다. {@code MANUAL} 행은 남긴다.
     *
     * <p>총 개수가 0이 되어도 막지 않는다 — 배달지역 전체 해제는 정당한 의도이며, "미설정 = 전 지역 허용"의
     * 함의는 프론트가 저장 전에 경고로 알린다. 저장 경로에서 0건을 막는 것과 방향이 다른데, 그쪽은
     * <b>좁히려는 의도가 정반대 결과를 내는</b> 사고를 막는 것이라 성격이 다르다.
     */
    public void deletePolygon(
        ShopId shopId,
        Function<Collection<AdminDongId>, List<String>> adminDongNamesById,
        ShopChangeActor actor
    ) {
        Set<AdminDongId> polygonDongIds = adminDongIdsOf(DeliveryAreaSource.POLYGON, shopId);
        validateNotReferencedByRegionTip(shopId, polygonDongIds, adminDongNamesById);

        ShopDeliveryAreaPolygon stored = shopDeliveryAreaPolygonRepository.findByShopId(shopId).orElse(null);
        if (stored == null) {
            // 저장된 도형이 없으면 지울 것도 없다 — 파생 행도 함께 없으므로 이력을 남기지 않는다.
            // 남기면 일어나지 않은 변경이 이력에 생긴다({@code clearDistanceTip}과 같은 판단).
            shopDeliveryAreaRepository.deleteByShopIdAndSource(shopId, DeliveryAreaSource.POLYGON);
            return;
        }
        String previousValue = describePolygon(stored);

        shopDeliveryAreaRepository.deleteByShopIdAndSource(shopId, DeliveryAreaSource.POLYGON);
        shopDeliveryAreaPolygonRepository.deleteByShopId(shopId);

        shopChangeHistoryRecorder.record(
            shopId,
            ShopChangeType.DELIVERY_AREA_POLYGON,
            ShopChangeActionType.DELETE,
            actor,
            previousValue,
            null
        );
    }

    /**
     * 저장된 도형을 한 줄로 요약한다(예: {@code "도형 4개, 꼭짓점 27개, 최원거리 3.4km"}). 없으면 "미설정".
     *
     * <p>정점 좌표를 나열하지 않는 이유는 이력이 읽히는 목적이 "언제 무엇을 바꿨는가"이지 도형 복원이
     * 아니기 때문이다 — 수천 좌표를 담으면 한 행이 화면을 다 덮고, 사람이 두 도형을 비교할 수도 없다.
     * 링·정점 수와 최원거리는 애그리거트가 이미 비정규화해 들고 있는 값이라 추가 계산도 없다.
     */
    private String describePolygon(ShopDeliveryAreaPolygon storedPolygon) {
        if (storedPolygon == null) {
            return ShopChangeValueFormatter.unset();
        }
        return "도형 " + storedPolygon.getRingCount() + "개, 꼭짓점 " + storedPolygon.getVertexCount()
            + "개, 최원거리 " + ShopChangeValueFormatter.distanceKm(toKilometers(storedPolygon.getMaxRadiusMeters()));
    }

    /**
     * 이번에 저장하는 도형을 한 줄로 요약한다(예: {@code "도형 4개, 꼭짓점 27개, 행정동 12곳"}).
     *
     * <p>변경 전 요약({@link #describePolygon})과 항목이 다른 것은 의도된 것이다 — 저장 시점에는 환산 결과
     * (열리는 행정동 수)가 그 저장의 핵심 결과이고, 저장된 도형에는 그 값이 남지 않아 나중에 되살릴 수 없다.
     */
    private String describeShape(GeoPolygon polygon, int projectedDongCount) {
        return "도형 " + polygon.ringCount() + "개, 꼭짓점 " + polygon.vertexCount()
            + "개, 행정동 " + projectedDongCount + "곳";
    }

    /**
     * 미터를 km로 환산한다. 소수점 아래 표기 정리는 {@code distanceKm}이 담당한다(3400m → 3.4km).
     */
    private BigDecimal toKilometers(int meters) {
        return BigDecimal.valueOf(meters).divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP);
    }

    /**
     * 도형을 행정동으로 환산한다(저장하지 않음). 저장 경로와 미리보기가 <b>같은</b> 후보 선정·판정을 쓰도록
     * 이 메서드를 공유한다 — 미리보기가 보여준 결과와 실제 저장 결과가 갈리면 기능의 의미가 없다.
     */
    public DeliveryAreaProjection.Result project(GeoPolygon polygon) {
        GeoBoundingBox candidateBox = polygon.boundingBox().expand(CANDIDATE_BOX_MARGIN_DEGREES);
        List<AdminDong> candidates = adminDongRepository.findAllWithinBoundingBox(candidateBox);
        return DeliveryAreaProjection.project(polygon, candidates);
    }

    private void upsertPolygon(ShopId shopId, GeoPolygon polygon, GeoPoint shopLocation) {
        ShopDeliveryAreaPolygon stored = shopDeliveryAreaPolygonRepository.findByShopId(shopId)
            .orElse(null);

        if (stored == null) {
            shopDeliveryAreaPolygonRepository.save(ShopDeliveryAreaPolygon.of(shopId, polygon, shopLocation));
            return;
        }
        stored.replace(polygon, shopLocation);
        shopDeliveryAreaPolygonRepository.save(stored);
    }

    private Set<AdminDongId> adminDongIdsOf(DeliveryAreaSource source, ShopId shopId) {
        return shopDeliveryAreaRepository.findByShopIdAndSource(shopId, source).stream()
            .map(ShopDeliveryArea::getAdminDongId)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * 닫히는 행정동이 지역별 배달팁에 참조되고 있으면 {@code SHOP_DELIVERY_AREA_IN_USE}(409)로 막는다.
     * 참조 집합을 한 번에 읽어 교집합을 확인하므로 부분 삭제가 발생하지 않는다.
     */
    private void validateNotReferencedByRegionTip(
        ShopId shopId,
        Collection<AdminDongId> closing,
        Function<Collection<AdminDongId>, List<String>> adminDongNamesById
    ) {
        if (closing.isEmpty()) {
            return;
        }

        Set<AdminDongId> referenced = shopDeliveryTipRegionLookup.findRegionTipAdminDongIds(shopId);
        List<AdminDongId> blocked = closing.stream()
            .filter(referenced::contains)
            .toList();
        if (blocked.isEmpty()) {
            return;
        }

        List<String> blockedNames = adminDongNamesById == null ? List.of() : adminDongNamesById.apply(blocked);
        String message = ErrorCode.SHOP_DELIVERY_AREA_IN_USE.getDefaultMessage();
        if (!blockedNames.isEmpty()) {
            message = message + ": " + String.join(", ", blockedNames);
        }
        throw new BusinessException(ErrorCode.SHOP_DELIVERY_AREA_IN_USE, message);
    }
}
