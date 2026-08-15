package com.tastyhouse.domain.shop.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.region.model.AdminDong;
import com.tastyhouse.domain.region.repository.AdminDongRepository;
import com.tastyhouse.domain.region.repository.AdminDongSyncResult;
import com.tastyhouse.domain.region.vo.AdminDongId;
import com.tastyhouse.domain.shared.geo.GeoBoundingBox;
import com.tastyhouse.domain.shared.geo.GeoPoint;
import com.tastyhouse.domain.shop.model.DeliveryAreaSource;
import com.tastyhouse.domain.shop.model.ShopChangeActionType;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.model.ShopChangeHistory;
import com.tastyhouse.domain.shop.model.ShopChangeType;
import com.tastyhouse.domain.shop.model.ShopDeliveryArea;
import com.tastyhouse.domain.shop.repository.ShopDeliveryAreaRepository;
import com.tastyhouse.domain.shop.repository.ShopDeliveryTipRegionLookup;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 반경 일괄 적용 도메인 서비스의 <b>변경이력</b> 단위 테스트.
 *
 * <p>검증의 핵심은 "반경 적용 한 번에 이력이 하나만, 그리고 {@code DELIVERY_AREA_RADIUS}로만 남는가"다 —
 * 이 서비스는 실제 행 추가를 {@code ShopDeliveryAreaService}에 위임하는데 그 경로가 이력까지 남기면
 * 점주가 한 조작(반경 설정) 하나에 {@code DELIVERY_AREA}까지 두 종류가 기록된다. 그 회귀는 컴파일로는
 * 드러나지 않으므로 테스트로 못박는다.
 *
 * <p>순수 POJO라 Spring 컨텍스트·JPA 없이 fake 포트로 검증한다(domain-module에는 Mockito 의존이 없다).
 */
class ShopDeliveryAreaRadiusServiceTest {

    private static final ShopId SHOP_ID = ShopId.of(1L);
    private static final GeoPoint SHOP_LOCATION = GeoPoint.of(37.5, 127.0);
    private static final ShopChangeActor ACTOR = ShopChangeActor.ceo(9L);

    private final AdminDongRepositoryFake adminDongRepository = new AdminDongRepositoryFake();
    private final ShopDeliveryAreaRepositoryFake areaRepository = new ShopDeliveryAreaRepositoryFake();
    private final RecordingShopChangeHistoryRepository historyRepository =
        new RecordingShopChangeHistoryRepository();
    private final ShopChangeHistoryRecorder recorder = new ShopChangeHistoryRecorder(historyRepository);
    private final ShopDeliveryAreaService deliveryAreaService = new ShopDeliveryAreaService(
        areaRepository, adminDongRepository, new ShopDeliveryTipRegionLookupFake(), recorder
    );
    private final ShopDeliveryAreaRadiusService service = new ShopDeliveryAreaRadiusService(
        areaRepository, adminDongRepository, deliveryAreaService, recorder
    );

    @Test
    @DisplayName("반경 적용은 열린 동 수와 무관하게 DELIVERY_AREA_RADIUS 이력 1행만 남긴다")
    void applyRadius_recordsSingleRadiusRow() {
        adminDongRepository.add(10L, GeoPoint.of(37.501, 127.001));
        adminDongRepository.add(11L, GeoPoint.of(37.502, 127.002));

        service.applyRadius(SHOP_ID, SHOP_LOCATION, 3500, false, ids -> List.of(), ACTOR);

        List<ShopChangeHistory> histories = historyRepository.saved();
        assertThat(histories).hasSize(1);
        assertThat(histories.getFirst().getChangeType()).isEqualTo(ShopChangeType.DELIVERY_AREA_RADIUS);
        assertThat(histories.getFirst().getActionType()).isEqualTo(ShopChangeActionType.UPDATE);
    }

    @Test
    @DisplayName("반경 적용은 DELIVERY_AREA를 남기지 않는다 — 점주 조작은 '반경 설정' 하나다")
    void applyRadius_doesNotRecordDeliveryAreaHistory() {
        adminDongRepository.add(10L, GeoPoint.of(37.501, 127.001));

        service.applyRadius(SHOP_ID, SHOP_LOCATION, 3500, false, ids -> List.of(), ACTOR);

        assertThat(historyRepository.savedOf(ShopChangeType.DELIVERY_AREA)).isEmpty();
    }

    @Test
    @DisplayName("이력에는 적용 반경·교체 여부·반영 후 총 동 수를 담고, 변경 전 값은 없다(반경은 저장되지 않는다)")
    void applyRadius_summarizesRadiusAndResult() {
        adminDongRepository.add(10L, GeoPoint.of(37.501, 127.001));

        service.applyRadius(SHOP_ID, SHOP_LOCATION, 3500, true, ids -> List.of(), ACTOR);

        ShopChangeHistory history = historyRepository.saved().getFirst();
        assertThat(history.getPreviousValue()).isNull();
        assertThat(history.getNewValue()).isEqualTo("3.5km (교체 적용, 배달가능지역 1곳)");
    }

    private static final class AdminDongRepositoryFake implements AdminDongRepository {

        private final Map<Long, AdminDong> adminDongs = new LinkedHashMap<>();

        void add(long id, GeoPoint center) {
            adminDongs.put(id, AdminDong.reconstitute(id, "code" + id, "시", "군", "동" + id, true, center, List.of()));
        }

        @Override
        public AdminDongSyncResult synchronize(List<AdminDong> newAdminDongs) {
            throw new UnsupportedOperationException("동기화는 이 테스트의 대상이 아닙니다.");
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
                .filter(Objects::nonNull)
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

    private static final class ShopDeliveryTipRegionLookupFake implements ShopDeliveryTipRegionLookup {

        @Override
        public boolean existsRegionTipByShopIdAndAdminDongId(ShopId shopId, AdminDongId adminDongId) {
            return false;
        }

        @Override
        public Set<AdminDongId> findRegionTipAdminDongIds(ShopId shopId) {
            return Set.of();
        }
    }
}
