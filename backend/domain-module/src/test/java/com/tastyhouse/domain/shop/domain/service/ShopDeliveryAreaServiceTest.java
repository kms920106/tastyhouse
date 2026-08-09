package com.tastyhouse.domain.shop.domain.service;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import com.tastyhouse.domain.shared.geo.GeoBoundingBox;
import com.tastyhouse.domain.shop.model.DeliveryAreaSource;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.region.model.AdminDong;
import com.tastyhouse.domain.region.repository.AdminDongRepository;
import com.tastyhouse.domain.region.repository.AdminDongSyncResult;
import com.tastyhouse.domain.region.vo.AdminDongId;
import com.tastyhouse.domain.shop.model.ShopDeliveryArea;
import com.tastyhouse.domain.shop.repository.ShopDeliveryAreaRepository;
import com.tastyhouse.domain.shop.repository.ShopDeliveryTipRegionLookup;
import com.tastyhouse.domain.shop.service.ShopDeliveryAreaService;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 가게 배달가능지역 도메인 서비스 단위 테스트.
 *
 * <p>순수 POJO이므로 Spring 컨텍스트·JPA 없이 write 포트와 조회 포트를 손으로 만든 fake로 대체해 검증한다
 * (domain-module에는 Mockito 의존이 없다).
 */
class ShopDeliveryAreaServiceTest {

    private static final ShopId SHOP_ID = ShopId.of(1L);
    private static final AdminDongId ADMIN_DONG_ID = AdminDongId.of(100L);

    @Nested
    @DisplayName("addArea")
    class AddArea {

        @Test
        @DisplayName("행정동이 마스터에 없으면 ADMIN_DONG_NOT_FOUND로 거부한다")
        void addArea_rejectsUnknownAdminDong() {
            ShopDeliveryAreaService service = service(new AdminDongRepositoryFake(), new ShopDeliveryAreaRepositoryFake(), new ShopDeliveryTipRegionLookupFake());

            assertThatThrownBy(() -> service.addArea(SHOP_ID, ADMIN_DONG_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(ErrorCode.ADMIN_DONG_NOT_FOUND);
        }

        @Test
        @DisplayName("같은 가게에 같은 행정동을 다시 등록하면 SHOP_DELIVERY_AREA_DUPLICATED로 거부한다")
        void addArea_rejectsDuplicate() {
            ShopDeliveryAreaRepositoryFake areaRepository = new ShopDeliveryAreaRepositoryFake();
            areaRepository.save(ShopDeliveryArea.of(SHOP_ID, ADMIN_DONG_ID));
            ShopDeliveryAreaService service = service(adminDongRepositoryWith(ADMIN_DONG_ID), areaRepository, new ShopDeliveryTipRegionLookupFake());

            assertThatThrownBy(() -> service.addArea(SHOP_ID, ADMIN_DONG_ID))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(ErrorCode.SHOP_DELIVERY_AREA_DUPLICATED);
        }

        @Test
        @DisplayName("행정동이 실재하고 중복이 아니면 저장 후 생성된 식별자를 반환한다")
        void addArea_savesAndReturnsId() {
            ShopDeliveryAreaRepositoryFake areaRepository = new ShopDeliveryAreaRepositoryFake();
            ShopDeliveryAreaService service = service(adminDongRepositoryWith(ADMIN_DONG_ID), areaRepository, new ShopDeliveryTipRegionLookupFake());

            Long deliveryAreaId = service.addArea(SHOP_ID, ADMIN_DONG_ID);

            assertThat(deliveryAreaId).isNotNull();
            assertThat(areaRepository.findByShopId(SHOP_ID)).hasSize(1);
            assertThat(areaRepository.findById(deliveryAreaId)).isPresent();
            assertThat(areaRepository.findById(deliveryAreaId).orElseThrow().getAdminDongId()).isEqualTo(ADMIN_DONG_ID);
        }
    }

    @Nested
    @DisplayName("removeArea")
    class RemoveArea {

        @Test
        @DisplayName("존재하지 않는 배달가능지역은 SHOP_DELIVERY_AREA_NOT_FOUND로 거부한다")
        void removeArea_rejectsMissingArea() {
            ShopDeliveryAreaService service = service(adminDongRepositoryWith(ADMIN_DONG_ID), new ShopDeliveryAreaRepositoryFake(), new ShopDeliveryTipRegionLookupFake());

            assertThatThrownBy(() -> service.removeArea(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(ErrorCode.SHOP_DELIVERY_AREA_NOT_FOUND);
        }

        @Test
        @DisplayName("그 행정동을 참조하는 지역별 배달팁이 있으면 SHOP_DELIVERY_AREA_IN_USE로 삭제를 차단한다")
        void removeArea_rejectsWhenReferencedByRegionTip() {
            ShopDeliveryAreaRepositoryFake areaRepository = new ShopDeliveryAreaRepositoryFake();
            Long deliveryAreaId = areaRepository.save(ShopDeliveryArea.of(SHOP_ID, ADMIN_DONG_ID)).getId();
            ShopDeliveryTipRegionLookupFake regionLookup = new ShopDeliveryTipRegionLookupFake();
            regionLookup.addRegionTip(SHOP_ID, ADMIN_DONG_ID);
            ShopDeliveryAreaService service = service(adminDongRepositoryWith(ADMIN_DONG_ID), areaRepository, regionLookup);

            assertThatThrownBy(() -> service.removeArea(deliveryAreaId))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(ErrorCode.SHOP_DELIVERY_AREA_IN_USE);
            assertThat(areaRepository.findById(deliveryAreaId)).isPresent();
        }

        @Test
        @DisplayName("다른 가게가 같은 행정동에 지역별 배달팁을 뒀어도 내 배달가능지역 삭제는 막지 않는다")
        void removeArea_ignoresOtherShopRegionTip() {
            ShopDeliveryAreaRepositoryFake areaRepository = new ShopDeliveryAreaRepositoryFake();
            Long deliveryAreaId = areaRepository.save(ShopDeliveryArea.of(SHOP_ID, ADMIN_DONG_ID)).getId();
            ShopDeliveryTipRegionLookupFake regionLookup = new ShopDeliveryTipRegionLookupFake();
            regionLookup.addRegionTip(ShopId.of(2L), ADMIN_DONG_ID);
            ShopDeliveryAreaService service = service(adminDongRepositoryWith(ADMIN_DONG_ID), areaRepository, regionLookup);

            service.removeArea(deliveryAreaId);

            assertThat(areaRepository.findById(deliveryAreaId)).isEmpty();
        }

        @Test
        @DisplayName("참조하는 지역별 배달팁이 없으면 삭제한다")
        void removeArea_deletesWhenNotReferenced() {
            ShopDeliveryAreaRepositoryFake areaRepository = new ShopDeliveryAreaRepositoryFake();
            Long deliveryAreaId = areaRepository.save(ShopDeliveryArea.of(SHOP_ID, ADMIN_DONG_ID)).getId();
            ShopDeliveryAreaService service = service(adminDongRepositoryWith(ADMIN_DONG_ID), areaRepository, new ShopDeliveryTipRegionLookupFake());

            service.removeArea(deliveryAreaId);

            assertThat(areaRepository.findByShopId(SHOP_ID)).isEmpty();
        }
    }

    private static ShopDeliveryAreaService service(
        AdminDongRepository adminDongRepository,
        ShopDeliveryAreaRepository shopDeliveryAreaRepository,
        ShopDeliveryTipRegionLookup shopDeliveryTipRegionLookup
    ) {
        return new ShopDeliveryAreaService(shopDeliveryAreaRepository, adminDongRepository, shopDeliveryTipRegionLookup);
    }

    private static AdminDongRepositoryFake adminDongRepositoryWith(AdminDongId... adminDongIds) {
        AdminDongRepositoryFake fake = new AdminDongRepositoryFake();
        for (AdminDongId adminDongId : adminDongIds) {
            fake.add(adminDongId);
        }
        return fake;
    }

    private static final class AdminDongRepositoryFake implements AdminDongRepository {

        @Override
        public AdminDongSyncResult synchronize(List<AdminDong> adminDongs) {
            // 이 테스트들은 조회 경로만 검증한다. 동기화가 불리면 테스트가 잘못 짜인 것이다.
            throw new UnsupportedOperationException("동기화는 이 테스트의 대상이 아닙니다.");
        }

        private final Map<Long, AdminDong> adminDongs = new LinkedHashMap<>();

        void add(AdminDongId adminDongId) {
            adminDongs.put(
                adminDongId.value(),
                AdminDong.reconstitute(adminDongId.value(), "1168053100", "서울특별시", "강남구", "역삼1동", true, null, List.of())
            );
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
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        }

        @Override
        public Optional<AdminDong> findByDongNameMatch(String sidoName, String sigunguName, String dongName) {
            return adminDongs.values().stream()
                .filter(adminDong -> adminDong.getSidoName().equals(sidoName)
                    && adminDong.getSigunguName().equals(sigunguName)
                    && adminDong.getDongName().equals(dongName))
                .findFirst();
        }
    }

    private static final class ShopDeliveryAreaRepositoryFake implements ShopDeliveryAreaRepository {

        private final Map<Long, ShopDeliveryArea> areas = new LinkedHashMap<>();
        private long sequence = 0L;

        @Override
        public List<ShopDeliveryArea> findByShopId(ShopId shopId) {
            return areas.values().stream()
                .filter(area -> area.getShopId().equals(shopId))
                .toList();
        }

        @Override
        public Optional<ShopDeliveryArea> findById(Long deliveryAreaId) {
            return Optional.ofNullable(areas.get(deliveryAreaId));
        }

        @Override
        public boolean existsByShopIdAndAdminDongId(ShopId shopId, AdminDongId adminDongId) {
            return areas.values().stream()
                .anyMatch(area -> area.getShopId().equals(shopId) && area.getAdminDongId().equals(adminDongId));
        }

        @Override
        public long countByShopId(ShopId shopId) {
            return findByShopId(shopId).size();
        }

        @Override
        public ShopDeliveryArea save(ShopDeliveryArea shopDeliveryArea) {
            long id = ++sequence;
            ShopDeliveryArea saved = ShopDeliveryArea.reconstitute(id, shopDeliveryArea.getShopId(), shopDeliveryArea.getAdminDongId(), shopDeliveryArea.getSource());
            areas.put(id, saved);
            return saved;
        }

        @Override
        public List<ShopDeliveryArea> saveAll(List<ShopDeliveryArea> shopDeliveryAreas) {
            return shopDeliveryAreas.stream().map(this::save).toList();
        }

        @Override
        public List<ShopDeliveryArea> findByShopIdAndSource(ShopId shopId, DeliveryAreaSource source) {
            return findByShopId(shopId).stream()
                .filter(area -> area.getSource() == source)
                .toList();
        }

        @Override
        public void deleteByShopIdAndSource(ShopId shopId, DeliveryAreaSource source) {
            findByShopIdAndSource(shopId, source).forEach(area -> areas.remove(area.getId()));
        }

        @Override
        public Set<AdminDongId> findAdminDongIdsByShopId(ShopId shopId) {
            return findByShopId(shopId).stream()
                .map(ShopDeliveryArea::getAdminDongId)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        }

        @Override
        public void deleteById(Long deliveryAreaId) {
            areas.remove(deliveryAreaId);
        }
    }

    private static final class ShopDeliveryTipRegionLookupFake implements ShopDeliveryTipRegionLookup {

        private final List<String> regionTipKeys = new ArrayList<>();

        // adminDongId를 현재 테스트가 한 값으로만 넘기지만 파라미터를 없애지 않는다 — 이 fake는
        // (shopId, adminDongId) 복합키를 저장하고 findRegionTipAdminDongIds가 그 dong을 되읽으므로,
        // 상수로 굳히면 동을 두 개 이상 쓰는 경우를 표현할 수 없고 그 조회의 검증력이 사라진다.
        @SuppressWarnings("SameParameterValue")
        void addRegionTip(ShopId shopId, AdminDongId adminDongId) {
            regionTipKeys.add(key(shopId, adminDongId));
        }

        @Override
        public boolean existsRegionTipByShopIdAndAdminDongId(ShopId shopId, AdminDongId adminDongId) {
            return regionTipKeys.contains(key(shopId, adminDongId));
        }

        @Override
        public Set<AdminDongId> findRegionTipAdminDongIds(ShopId shopId) {
            Set<AdminDongId> referenced = new LinkedHashSet<>();
            for (String regionTipKey : regionTipKeys) {
                String[] parts = regionTipKey.split(":");
                if (Long.parseLong(parts[0]) == shopId.value()) {
                    referenced.add(AdminDongId.of(Long.parseLong(parts[1])));
                }
            }
            return referenced;
        }

        private static String key(ShopId shopId, AdminDongId adminDongId) {
            return shopId.value() + ":" + adminDongId.value();
        }
    }
}
