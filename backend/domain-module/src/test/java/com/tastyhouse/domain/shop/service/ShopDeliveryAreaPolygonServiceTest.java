package com.tastyhouse.domain.shop.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.region.model.AdminDong;
import com.tastyhouse.domain.region.repository.AdminDongRepository;
import com.tastyhouse.domain.region.repository.AdminDongSyncResult;
import com.tastyhouse.domain.region.vo.AdminDongId;
import com.tastyhouse.domain.shared.geo.GeoBoundingBox;
import com.tastyhouse.domain.shared.geo.GeoPoint;
import com.tastyhouse.domain.shared.geo.GeoPolygon;
import com.tastyhouse.domain.shared.geo.GeoRing;
import com.tastyhouse.domain.shop.model.DeliveryAreaSource;
import com.tastyhouse.domain.shop.model.ShopDeliveryArea;
import com.tastyhouse.domain.shop.model.ShopChangeActionType;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.model.ShopChangeHistory;
import com.tastyhouse.domain.shop.model.ShopChangeType;
import com.tastyhouse.domain.shop.model.ShopDeliveryAreaPolygon;
import com.tastyhouse.domain.shop.repository.ShopDeliveryAreaPolygonRepository;
import com.tastyhouse.domain.shop.repository.ShopDeliveryAreaRepository;
import com.tastyhouse.domain.shop.repository.ShopDeliveryTipRegionLookup;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 배달지역 도형 저장·삭제 오케스트레이션 단위 테스트.
 *
 * <p>고정하는 성질은 셋이다 — (1) 환산 0건 저장을 막는다(막지 않으면 "좁게 그렸더니 전 지역이 열리는"
 * 역전이 생긴다), (2) 도형 재저장이 <b>직접 등록분을 건드리지 않는다</b>, (3) 배달팁 참조로 막히면
 * <b>한 건도</b> 바뀌지 않는다.
 */
class ShopDeliveryAreaPolygonServiceTest {

    private static final ShopId SHOP_ID = ShopId.of(1L);
    private static final GeoPoint SHOP_LOCATION = GeoPoint.of(37.5, 127.0);
    private static final ShopChangeActor ACTOR = ShopChangeActor.ceo(9L);

    /** 가게를 감싸는 작은 사각형(약 1km). */
    private static final GeoPolygon POLYGON = GeoPolygon.of(List.of(GeoRing.of(List.of(
        GeoPoint.of(37.495, 126.995),
        GeoPoint.of(37.495, 127.005),
        GeoPoint.of(37.505, 127.005),
        GeoPoint.of(37.505, 126.995)
    ))));

    @Test
    @DisplayName("환산 결과가 0건이면 저장을 거부한다")
    void savePolygon_rejectsEmptyProjection() {
        Fixture fixture = new Fixture();
        // 후보 행정동을 등록하지 않아 환산 결과가 0건이 된다.

        assertThatThrownBy(fixture::savePolygon)
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getErrorCode())
            .isEqualTo(ErrorCode.SHOP_DELIVERY_AREA_EMPTY_PROJECTION);

        assertThat(fixture.polygonRepository.stored).isNull();
    }

    @Test
    @DisplayName("도형을 저장하면 환산된 행정동이 POLYGON 출처로 등록된다")
    void savePolygon_insertsProjectedAreas() {
        Fixture fixture = new Fixture();
        fixture.registerDong(10L, 37.500, 127.000);
        fixture.registerDong(11L, 37.502, 127.002);

        fixture.savePolygon();

        assertThat(fixture.areaRepository.findByShopIdAndSource(SHOP_ID, DeliveryAreaSource.POLYGON))
            .extracting(area -> area.getAdminDongId().value())
            .containsExactlyInAnyOrder(10L, 11L);
        assertThat(fixture.polygonRepository.stored).isNotNull();
        assertThat(fixture.polygonRepository.stored.getRingCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("도형을 다시 저장해도 직접 등록한 행정동은 보존된다")
    void savePolygon_preservesManualAreas() {
        Fixture fixture = new Fixture();
        fixture.registerDong(10L, 37.500, 127.000);
        fixture.registerDong(99L, 38.900, 128.900); // 도형 밖 — 직접 등록분
        fixture.areaRepository.save(ShopDeliveryArea.of(SHOP_ID, AdminDongId.of(99L), DeliveryAreaSource.MANUAL));

        fixture.savePolygon();

        assertThat(fixture.areaRepository.findByShopIdAndSource(SHOP_ID, DeliveryAreaSource.MANUAL))
            .extracting(area -> area.getAdminDongId().value())
            .containsExactly(99L);
        assertThat(fixture.areaRepository.findByShopIdAndSource(SHOP_ID, DeliveryAreaSource.POLYGON))
            .extracting(area -> area.getAdminDongId().value())
            .containsExactly(10L);
    }

    @Test
    @DisplayName("이미 직접 등록된 동은 파생 행으로 중복 등록하지 않는다(출처를 강등하지 않는다)")
    void savePolygon_skipsDongAlreadyRegisteredAsManual() {
        Fixture fixture = new Fixture();
        fixture.registerDong(10L, 37.500, 127.000);
        fixture.areaRepository.save(ShopDeliveryArea.of(SHOP_ID, AdminDongId.of(10L), DeliveryAreaSource.MANUAL));

        fixture.savePolygon();

        assertThat(fixture.areaRepository.findByShopId(SHOP_ID)).hasSize(1);
        assertThat(fixture.areaRepository.findByShopId(SHOP_ID).getFirst().getSource())
            .isEqualTo(DeliveryAreaSource.MANUAL);
    }

    @Test
    @DisplayName("닫히는 동에 배달팁 참조가 있으면 아무것도 바꾸지 않고 409로 막는다")
    void savePolygon_blocksWhenClosingDongIsReferencedByRegionTip() {
        Fixture fixture = new Fixture();
        fixture.registerDong(10L, 37.500, 127.000);
        fixture.registerDong(20L, 38.900, 128.900); // 새 환산 결과에 없어 닫히는 동
        fixture.areaRepository.save(ShopDeliveryArea.of(SHOP_ID, AdminDongId.of(20L), DeliveryAreaSource.POLYGON));
        fixture.regionLookup.addRegionTip(AdminDongId.of(20L));

        assertThatThrownBy(fixture::savePolygon)
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getErrorCode())
            .isEqualTo(ErrorCode.SHOP_DELIVERY_AREA_IN_USE);

        // 한 건도 바뀌지 않았다.
        assertThat(fixture.areaRepository.findByShopId(SHOP_ID))
            .extracting(area -> area.getAdminDongId().value())
            .containsExactly(20L);
        assertThat(fixture.polygonRepository.stored).isNull();
    }

    @Test
    @DisplayName("도형을 삭제하면 파생 행정동만 지우고 직접 등록분은 남긴다")
    void deletePolygon_removesOnlyDerivedAreas() {
        Fixture fixture = new Fixture();
        fixture.areaRepository.save(ShopDeliveryArea.of(SHOP_ID, AdminDongId.of(10L), DeliveryAreaSource.POLYGON));
        fixture.areaRepository.save(ShopDeliveryArea.of(SHOP_ID, AdminDongId.of(99L), DeliveryAreaSource.MANUAL));
        fixture.polygonRepository.stored = ShopDeliveryAreaPolygon.of(SHOP_ID, POLYGON, SHOP_LOCATION);

        fixture.service.deletePolygon(SHOP_ID, ids -> List.of(), ACTOR);

        assertThat(fixture.areaRepository.findByShopId(SHOP_ID))
            .extracting(area -> area.getAdminDongId().value())
            .containsExactly(99L);
        assertThat(fixture.polygonRepository.stored).isNull();
    }

    @Test
    @DisplayName("도형 삭제로 총 0건이 되어도 허용한다(배달지역 전체 해제는 정당한 의도)")
    void deletePolygon_allowsResultingInZeroAreas() {
        Fixture fixture = new Fixture();
        fixture.areaRepository.save(ShopDeliveryArea.of(SHOP_ID, AdminDongId.of(10L), DeliveryAreaSource.POLYGON));
        fixture.polygonRepository.stored = ShopDeliveryAreaPolygon.of(SHOP_ID, POLYGON, SHOP_LOCATION);

        fixture.service.deletePolygon(SHOP_ID, ids -> List.of(), ACTOR);

        assertThat(fixture.areaRepository.findByShopId(SHOP_ID)).isEmpty();
    }

    @Test
    @DisplayName("도형 저장은 환산 동 수와 무관하게 이력 1행만 남기고 도형 규모로 요약한다")
    void savePolygon_recordsSingleRow() {
        Fixture fixture = new Fixture();
        fixture.registerDong(10L, 37.5, 127.0);
        fixture.registerDong(11L, 37.501, 127.001);

        fixture.savePolygon();

        List<ShopChangeHistory> histories =
            fixture.historyRepository.savedOf(ShopChangeType.DELIVERY_AREA_POLYGON);
        assertThat(histories).hasSize(1);
        assertThat(histories.getFirst().getActionType()).isEqualTo(ShopChangeActionType.UPDATE);
        assertThat(histories.getFirst().getPreviousValue()).isEqualTo("미설정");
        assertThat(histories.getFirst().getNewValue())
            .startsWith("도형 " + POLYGON.ringCount() + "개, 꼭짓점 " + POLYGON.vertexCount() + "개, 행정동 ");
    }

    @Test
    @DisplayName("도형 저장은 파생 행정동 변화를 DELIVERY_AREA로 따로 남기지 않는다 — 점주 조작은 '도형 저장' 하나다")
    void savePolygon_doesNotRecordDeliveryAreaHistory() {
        Fixture fixture = new Fixture();
        fixture.registerDong(10L, 37.5, 127.0);

        fixture.savePolygon();

        assertThat(fixture.historyRepository.savedOf(ShopChangeType.DELIVERY_AREA)).isEmpty();
    }

    @Test
    @DisplayName("도형 삭제는 DELETE 한 행을 남기고 변경 전 도형 규모를 담는다")
    void deletePolygon_recordsDeleteRow() {
        Fixture fixture = new Fixture();
        fixture.registerDong(10L, 37.5, 127.0);
        fixture.savePolygon();

        fixture.service.deletePolygon(SHOP_ID, ids -> List.of(), ACTOR);

        List<ShopChangeHistory> histories =
            fixture.historyRepository.savedOf(ShopChangeType.DELIVERY_AREA_POLYGON);
        assertThat(histories).hasSize(2);
        assertThat(histories.get(1).getActionType()).isEqualTo(ShopChangeActionType.DELETE);
        assertThat(histories.get(1).getPreviousValue())
            .startsWith("도형 " + POLYGON.ringCount() + "개, 꼭짓점 " + POLYGON.vertexCount() + "개, 최원거리 ");
        assertThat(histories.get(1).getNewValue()).isNull();
    }

    @Test
    @DisplayName("저장된 도형이 없는 가게의 삭제는 이력을 남기지 않는다 — 일어나지 않은 변경을 기록하지 않는다")
    void deletePolygon_withoutStoredPolygon_recordsNothing() {
        Fixture fixture = new Fixture();

        fixture.service.deletePolygon(SHOP_ID, ids -> List.of(), ACTOR);

        assertThat(fixture.historyRepository.saved()).isEmpty();
    }

    /** 테스트 대상과 인메모리 fake 묶음. */
    private static final class Fixture {

        private final AdminDongRepositoryFake adminDongRepository = new AdminDongRepositoryFake();
        private final ShopDeliveryAreaRepositoryFake areaRepository = new ShopDeliveryAreaRepositoryFake();
        private final ShopDeliveryAreaPolygonRepositoryFake polygonRepository = new ShopDeliveryAreaPolygonRepositoryFake();
        private final ShopDeliveryTipRegionLookupFake regionLookup = new ShopDeliveryTipRegionLookupFake();
        private final RecordingShopChangeHistoryRepository historyRepository =
            new RecordingShopChangeHistoryRepository();
        private final ShopDeliveryAreaPolygonService service = new ShopDeliveryAreaPolygonService(
            polygonRepository,
            areaRepository,
            adminDongRepository,
            regionLookup,
            new ShopChangeHistoryRecorder(historyRepository)
        );

        void registerDong(long id, double latitude, double longitude) {
            adminDongRepository.add(id, GeoPoint.of(latitude, longitude));
        }

        void savePolygon() {
            service.savePolygon(SHOP_ID, POLYGON, SHOP_LOCATION, ids -> List.of(), ACTOR);
        }
    }

    private static final class AdminDongRepositoryFake implements AdminDongRepository {

        @Override
        public AdminDongSyncResult synchronize(List<AdminDong> adminDongs) {
            // 이 테스트들은 조회 경로만 검증한다. 동기화가 불리면 테스트가 잘못 짜인 것이다.
            throw new UnsupportedOperationException("동기화는 이 테스트의 대상이 아닙니다.");
        }

        private final Map<Long, AdminDong> adminDongs = new LinkedHashMap<>();

        void add(long id, GeoPoint center) {
            adminDongs.put(id, AdminDong.reconstitute(id, "code" + id, "시", "군", "동", true, center, List.of()));
        }

        @Override
        public Optional<AdminDong> findById(AdminDongId adminDongId) {
            return Optional.ofNullable(adminDongs.get(adminDongId.value()));
        }

        @Override
        public boolean existsById(AdminDongId adminDongId) {
            return adminDongs.containsKey(adminDongId.value());
        }

        @Override
        public Optional<AdminDong> findByDongNameMatch(String sidoName, String sigunguName, String dongName) {
            return Optional.empty();
        }

        @Override
        public List<AdminDong> findAllWithinBoundingBox(GeoBoundingBox boundingBox) {
            return adminDongs.values().stream()
                .filter(AdminDong::hasCenter)
                .filter(adminDong -> boundingBox.contains(adminDong.getCenter()))
                .toList();
        }

        @Override
        public List<AdminDong> findAllByIds(Collection<AdminDongId> adminDongIds) {
            return adminDongIds.stream()
                .map(adminDongId -> adminDongs.get(adminDongId.value()))
                .filter(java.util.Objects::nonNull)
                .toList();
        }

        @Override
        public Set<AdminDongId> filterExistingIds(Collection<AdminDongId> adminDongIds) {
            return adminDongIds.stream()
                .filter(adminDongId -> adminDongs.containsKey(adminDongId.value()))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        }
    }

    private static final class ShopDeliveryAreaRepositoryFake implements ShopDeliveryAreaRepository {

        private final Map<Long, ShopDeliveryArea> areas = new LinkedHashMap<>();
        private long sequence = 0L;

        @Override
        public List<ShopDeliveryArea> findByShopId(ShopId shopId) {
            return areas.values().stream().filter(area -> area.getShopId().equals(shopId)).toList();
        }

        @Override
        public Optional<ShopDeliveryArea> findById(Long deliveryAreaId) {
            return Optional.ofNullable(areas.get(deliveryAreaId));
        }

        @Override
        public boolean existsByShopIdAndAdminDongId(ShopId shopId, AdminDongId adminDongId) {
            return findByShopId(shopId).stream().anyMatch(area -> area.getAdminDongId().equals(adminDongId));
        }

        @Override
        public long countByShopId(ShopId shopId) {
            return findByShopId(shopId).size();
        }

        @Override
        public ShopDeliveryArea save(ShopDeliveryArea shopDeliveryArea) {
            long id = ++sequence;
            ShopDeliveryArea saved = ShopDeliveryArea.reconstitute(
                id, shopDeliveryArea.getShopId(), shopDeliveryArea.getAdminDongId(), shopDeliveryArea.getSource()
            );
            areas.put(id, saved);
            return saved;
        }

        @Override
        public List<ShopDeliveryArea> saveAll(List<ShopDeliveryArea> shopDeliveryAreas) {
            return shopDeliveryAreas.stream().map(this::save).toList();
        }

        @Override
        public List<ShopDeliveryArea> findByShopIdAndSource(ShopId shopId, DeliveryAreaSource source) {
            return findByShopId(shopId).stream().filter(area -> area.getSource() == source).toList();
        }

        @Override
        public void deleteByShopIdAndSource(ShopId shopId, DeliveryAreaSource source) {
            List<Long> targets = new ArrayList<>();
            findByShopIdAndSource(shopId, source).forEach(area -> targets.add(area.getId()));
            targets.forEach(areas::remove);
        }

        @Override
        public Set<AdminDongId> findAdminDongIdsByShopId(ShopId shopId) {
            return findByShopId(shopId).stream()
                .map(ShopDeliveryArea::getAdminDongId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        }

        @Override
        public void deleteById(Long deliveryAreaId) {
            areas.remove(deliveryAreaId);
        }
    }

    private static final class ShopDeliveryAreaPolygonRepositoryFake implements ShopDeliveryAreaPolygonRepository {

        private ShopDeliveryAreaPolygon stored;

        @Override
        public Optional<ShopDeliveryAreaPolygon> findByShopId(ShopId shopId) {
            return Optional.ofNullable(stored);
        }

        @Override
        public ShopDeliveryAreaPolygon save(ShopDeliveryAreaPolygon shopDeliveryAreaPolygon) {
            stored = shopDeliveryAreaPolygon;
            return shopDeliveryAreaPolygon;
        }

        @Override
        public void deleteByShopId(ShopId shopId) {
            stored = null;
        }
    }

    private static final class ShopDeliveryTipRegionLookupFake implements ShopDeliveryTipRegionLookup {

        private final Set<AdminDongId> referenced = new LinkedHashSet<>();

        void addRegionTip(AdminDongId adminDongId) {
            referenced.add(adminDongId);
        }

        @Override
        public boolean existsRegionTipByShopIdAndAdminDongId(ShopId shopId, AdminDongId adminDongId) {
            return referenced.contains(adminDongId);
        }

        @Override
        public Set<AdminDongId> findRegionTipAdminDongIds(ShopId shopId) {
            return referenced;
        }
    }
}
