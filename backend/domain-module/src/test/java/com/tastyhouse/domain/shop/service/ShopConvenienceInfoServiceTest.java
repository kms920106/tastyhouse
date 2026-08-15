package com.tastyhouse.domain.shop.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.shop.model.Amenity;
import com.tastyhouse.domain.shop.model.OrderMethod;
import com.tastyhouse.domain.shop.model.ProhibitedWord;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.model.ShopAmenity;
import com.tastyhouse.domain.shop.model.ShopAmenityCategory;
import com.tastyhouse.domain.shop.model.ShopBannerImage;
import com.tastyhouse.domain.shop.model.ShopBreakTime;
import com.tastyhouse.domain.shop.model.ShopBusinessHour;
import com.tastyhouse.domain.shop.model.ShopChangeActionType;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.model.ShopChangeActorType;
import com.tastyhouse.domain.shop.model.ShopChangeHistory;
import com.tastyhouse.domain.shop.model.ShopChangeType;
import com.tastyhouse.domain.shop.model.ShopClosedDay;
import com.tastyhouse.domain.shop.model.ShopConvenienceInfo;
import com.tastyhouse.domain.shop.model.ShopFoodType;
import com.tastyhouse.domain.shop.model.ShopFoodTypeCategory;
import com.tastyhouse.domain.shop.model.ShopOrderMethod;
import com.tastyhouse.domain.shop.model.ShopOwnerMessageHistory;
import com.tastyhouse.domain.shop.model.ShopPhotoCategory;
import com.tastyhouse.domain.shop.model.ShopPhotoCategoryImage;
import com.tastyhouse.domain.shop.repository.ProhibitedWordRepository;
import com.tastyhouse.domain.shop.repository.ShopConvenienceInfoRepository;
import com.tastyhouse.domain.shop.repository.ShopDetailRepository;
import com.tastyhouse.domain.shop.repository.ShopRepository;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 편의정보·편의시설 변경이력 기록 회귀 테스트.
 *
 * <p>이력 기록이 조용히 빠지는 결함(설정은 저장되는데 이력이 없는 부류)을 막는 것이 목적이다. 편의정보는
 * 한 화면 저장이므로 <b>필드 수와 무관하게 1행</b>, 편의시설은 화면에서 하나씩 켜고 끄므로 <b>조작당 1행</b>이다.
 */
class ShopConvenienceInfoServiceTest {

    private static final Long SHOP_ID = 1L;
    private static final Long PARKING_CATEGORY_ID = 11L;

    private RecordingShopChangeHistoryRepository shopChangeHistoryRepository;
    private ShopConvenienceInfoService shopConvenienceInfoService;

    /** 편의정보 write 포트 fake. 가게당 1건 upsert 시맨틱을 그대로 흉내낸다. */
    private static final class FakeShopConvenienceInfoRepository implements ShopConvenienceInfoRepository {

        private final Map<Long, ShopConvenienceInfo> infos = new HashMap<>();

        @Override
        public Optional<ShopConvenienceInfo> findByShopId(Long shopId) {
            return Optional.ofNullable(infos.get(shopId));
        }

        @Override
        public ShopConvenienceInfo save(ShopConvenienceInfo shopConvenienceInfo) {
            infos.put(shopConvenienceInfo.getShopId().value(), shopConvenienceInfo);
            return shopConvenienceInfo;
        }
    }

    /**
     * 이 테스트가 쓰는 편의시설 3개 경로만 구현하고, 나머지는 호출되면 즉시 실패시켜 의도치 않은 의존을 드러낸다.
     */
    private static final class FakeShopDetailRepository implements ShopDetailRepository {

        private final Map<Long, ShopAmenityCategory> categories = new HashMap<>();
        private long sequence = 0L;

        FakeShopDetailRepository() {
            categories.put(PARKING_CATEGORY_ID, ShopAmenityCategory.reconstitute(
                PARKING_CATEGORY_ID, Amenity.PARKING, "주차 가능", null, null, 1, true
            ));
        }

        @Override
        public Optional<ShopAmenityCategory> findAmenityCategoryById(Long id) {
            return Optional.ofNullable(categories.get(id));
        }

        @Override
        public ShopAmenity saveAmenity(ShopAmenity amenity) {
            return ShopAmenity.reconstitute(
                ++sequence, amenity.getShopId(), amenity.getShopAmenityCategoryId()
            );
        }

        @Override
        public void deleteAmenityByShopIdAndCategoryId(Long shopId, Long shopAmenityCategoryId) {
            // 이력 기록만 검증하는 테스트이므로 삭제는 no-op으로 둔다.
        }

        @Override
        public ShopAmenityCategory saveAmenityCategory(ShopAmenityCategory amenityCategory) {
            throw unsupported();
        }

        @Override
        public Optional<ShopFoodTypeCategory> findFoodTypeCategoryById(Long id) {
            throw unsupported();
        }

        @Override
        public ShopFoodTypeCategory saveFoodTypeCategory(ShopFoodTypeCategory foodTypeCategory) {
            throw unsupported();
        }

        @Override
        public ShopFoodType saveFoodType(ShopFoodType foodType) {
            throw unsupported();
        }

        @Override
        public void deleteFoodTypeByShopIdAndCategoryId(Long shopId, Long shopFoodTypeCategoryId) {
            throw unsupported();
        }

        @Override
        public List<ShopBusinessHour> findBusinessHoursByShopId(Long shopId) {
            throw unsupported();
        }

        @Override
        public Optional<ShopBusinessHour> findBusinessHourById(Long id) {
            throw unsupported();
        }

        @Override
        public ShopBusinessHour saveBusinessHour(ShopBusinessHour businessHour) {
            throw unsupported();
        }

        @Override
        public void deleteBusinessHourById(Long id) {
            throw unsupported();
        }

        @Override
        public List<ShopBreakTime> findBreakTimesByShopId(Long shopId) {
            throw unsupported();
        }

        @Override
        public Optional<ShopBreakTime> findBreakTimeById(Long id) {
            throw unsupported();
        }

        @Override
        public ShopBreakTime saveBreakTime(ShopBreakTime breakTime) {
            throw unsupported();
        }

        @Override
        public void deleteBreakTimeById(Long id) {
            throw unsupported();
        }

        @Override
        public List<ShopClosedDay> findClosedDaysByShopId(Long shopId) {
            throw unsupported();
        }

        @Override
        public Optional<ShopClosedDay> findClosedDayById(Long id) {
            throw unsupported();
        }

        @Override
        public ShopClosedDay saveClosedDay(ShopClosedDay closedDay) {
            throw unsupported();
        }

        @Override
        public void deleteClosedDayById(Long id) {
            throw unsupported();
        }

        @Override
        public List<ShopOrderMethod> findOrderMethodsByShopId(Long shopId) {
            throw unsupported();
        }

        @Override
        public ShopOrderMethod saveOrderMethod(ShopOrderMethod orderMethod) {
            throw unsupported();
        }

        @Override
        public void deleteOrderMethodByShopIdAndOrderMethod(Long shopId, OrderMethod orderMethod) {
            throw unsupported();
        }

        @Override
        public ShopBannerImage saveBannerImage(ShopBannerImage bannerImage) {
            throw unsupported();
        }

        @Override
        public void deleteBannerImageById(Long id) {
            throw unsupported();
        }

        @Override
        public Optional<ShopPhotoCategory> findPhotoCategoryById(Long id) {
            throw unsupported();
        }

        @Override
        public ShopPhotoCategory savePhotoCategory(ShopPhotoCategory photoCategory) {
            throw unsupported();
        }

        @Override
        public void deletePhotoCategoryById(Long id) {
            throw unsupported();
        }

        @Override
        public Optional<ShopPhotoCategoryImage> findPhotoCategoryImageById(Long id) {
            throw unsupported();
        }

        @Override
        public ShopPhotoCategoryImage savePhotoCategoryImage(ShopPhotoCategoryImage photoCategoryImage) {
            throw unsupported();
        }

        @Override
        public void deletePhotoCategoryImageById(Long id) {
            throw unsupported();
        }

        @Override
        public void saveOwnerMessage(ShopOwnerMessageHistory ownerMessageHistory) {
            throw unsupported();
        }

        @Override
        public Optional<ShopOwnerMessageHistory> findLatestOwnerMessage(Long shopId) {
            throw unsupported();
        }

        private UnsupportedOperationException unsupported() {
            return new UnsupportedOperationException("이 테스트는 이 경로를 쓰지 않는다");
        }
    }

    /** 표시 위치 반경 검증이 읽는 가게 좌표만 제공하는 fake. */
    private static final class FakeShopRepository implements ShopRepository {

        private final Map<Long, Shop> shops = new HashMap<>();

        FakeShopRepository() {
            shops.put(SHOP_ID, Shop.reconstitute(
                SHOP_ID, null, null, "맛있는 분식",
                BigDecimal.valueOf(37.497942), BigDecimal.valueOf(127.027621), 4.5,
                "서울시 송파구 위례성대로 10", "서울시 송파구 방이동 44-1", "02-1234-5678",
                null, null, false, false, false, 10000, false, null, null
            ));
        }

        @Override
        public Optional<Shop> findById(ShopId shopId) {
            return Optional.ofNullable(shops.get(shopId.value()));
        }

        @Override
        public Optional<Shop> findVisibleById(ShopId shopId) {
            return findById(shopId);
        }

        @Override
        public Shop save(Shop shop) {
            shops.put(shop.getShopId().value(), shop);
            return shop;
        }
    }

    private static final class FakeProhibitedWordRepository implements ProhibitedWordRepository {

        @Override
        public List<ProhibitedWord> findAll() {
            return List.of(ProhibitedWord.reconstitute(1L, "전화주문", "전화 주문 유도"));
        }
    }

    @BeforeEach
    void setUp() {
        shopChangeHistoryRepository = new RecordingShopChangeHistoryRepository();
        shopConvenienceInfoService = new ShopConvenienceInfoService(
            new FakeShopConvenienceInfoRepository(),
            new FakeShopRepository(),
            new FakeShopDetailRepository(),
            new ProhibitedWordValidator(new FakeProhibitedWordRepository()),
            new ShopChangeHistoryRecorder(shopChangeHistoryRepository)
        );
    }

    @Test
    @DisplayName("편의정보를 저장하면 필드 수와 무관하게 CONVENIENCE_INFO 이력이 정확히 1건 남는다")
    void upsertConvenienceInfo_recordsExactlyOneHistory() {
        shopConvenienceInfoService.upsertConvenienceInfo(
            SHOP_ID, true, true, false, false, "정문 옆 골목으로 들어오세요", null, null,
            ShopChangeActor.ceo(7L)
        );

        assertThat(shopChangeHistoryRepository.savedOf(ShopChangeType.CONVENIENCE_INFO)).hasSize(1);
        ShopChangeHistory history =
            shopChangeHistoryRepository.savedOf(ShopChangeType.CONVENIENCE_INFO).getFirst();
        assertThat(history.getActionType()).isEqualTo(ShopChangeActionType.UPDATE);
        assertThat(history.getActorType()).isEqualTo(ShopChangeActorType.CEO);
        assertThat(history.getActorId()).isEqualTo(7L);
        assertThat(history.getPreviousValue()).isEqualTo("없음");
        assertThat(history.getNewValue()).isEqualTo(
            "주차: 가능(유료)\n발렛: 불가\n찾아오는길: 정문 옆 골목으로 들어오세요\n표시위치: 미설정"
        );
    }

    @Test
    @DisplayName("두 번째 저장의 변경 전 값은 갱신되기 전 스냅샷이다")
    void upsertConvenienceInfo_capturesPreviousSnapshotBeforeUpdate() {
        shopConvenienceInfoService.upsertConvenienceInfo(
            SHOP_ID, false, false, false, false, "이전 안내", null, null, ShopChangeActor.ceo(7L)
        );
        shopConvenienceInfoService.upsertConvenienceInfo(
            SHOP_ID, true, false, false, false, "새 안내", null, null, ShopChangeActor.ceo(7L)
        );

        List<ShopChangeHistory> histories =
            shopChangeHistoryRepository.savedOf(ShopChangeType.CONVENIENCE_INFO);
        assertThat(histories).hasSize(2);
        assertThat(histories.get(1).getPreviousValue()).contains("주차: 불가", "찾아오는길: 이전 안내");
        assertThat(histories.get(1).getNewValue()).contains("주차: 가능(무료)", "찾아오는길: 새 안내");
    }

    @Test
    @DisplayName("편의시설 배정·해제는 조작당 AMENITY 이력을 1건씩 남긴다")
    void amenity_recordsOneHistoryPerOperation() {
        shopConvenienceInfoService.assignAmenity(SHOP_ID, PARKING_CATEGORY_ID, ShopChangeActor.ceo(7L));
        shopConvenienceInfoService.unassignAmenity(SHOP_ID, PARKING_CATEGORY_ID, ShopChangeActor.admin(3L));

        List<ShopChangeHistory> histories = shopChangeHistoryRepository.savedOf(ShopChangeType.AMENITY);
        assertThat(histories).hasSize(2);

        assertThat(histories.getFirst().getActionType()).isEqualTo(ShopChangeActionType.CREATE);
        assertThat(histories.getFirst().getPreviousValue()).isNull();
        assertThat(histories.getFirst().getNewValue()).isEqualTo("주차 가능");

        assertThat(histories.get(1).getActionType()).isEqualTo(ShopChangeActionType.DELETE);
        assertThat(histories.get(1).getActorType()).isEqualTo(ShopChangeActorType.ADMIN);
        assertThat(histories.get(1).getActorId()).isEqualTo(3L);
        assertThat(histories.get(1).getPreviousValue()).isEqualTo("주차 가능");
        assertThat(histories.get(1).getNewValue()).isNull();
    }
}
