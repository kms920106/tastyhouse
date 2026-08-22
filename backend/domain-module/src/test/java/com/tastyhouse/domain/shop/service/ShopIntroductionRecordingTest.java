package com.tastyhouse.domain.shop.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.shop.model.ProhibitedWord;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.model.ShopAmenity;
import com.tastyhouse.domain.shop.model.ShopAmenityCategory;
import com.tastyhouse.domain.shop.model.ShopBannerImage;
import com.tastyhouse.domain.shop.model.ShopBookmark;
import com.tastyhouse.domain.shop.model.ShopBreakTime;
import com.tastyhouse.domain.shop.model.ShopBusinessHour;
import com.tastyhouse.domain.shop.model.ShopChangeActionType;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.model.ShopChangeActorType;
import com.tastyhouse.domain.shop.model.ShopChangeHistory;
import com.tastyhouse.domain.shop.model.ShopChangeType;
import com.tastyhouse.domain.shop.model.ShopClosedDay;
import com.tastyhouse.domain.shop.model.ShopFoodType;
import com.tastyhouse.domain.shop.model.ShopFoodTypeCategory;
import com.tastyhouse.domain.shop.model.ShopOrderMethod;
import com.tastyhouse.domain.shop.model.ShopOwnerMessageHistory;
import com.tastyhouse.domain.shop.model.ShopPhotoCategory;
import com.tastyhouse.domain.shop.model.ShopPhotoCategoryImage;
import com.tastyhouse.domain.shop.repository.ProhibitedWordRepository;
import com.tastyhouse.domain.shop.repository.ShopBookmarkRepository;
import com.tastyhouse.domain.shop.repository.ShopDetailRepository;
import com.tastyhouse.domain.shop.repository.ShopImageChangeRequestRepository;
import com.tastyhouse.domain.shop.repository.ShopRepository;
import com.tastyhouse.domain.shop.model.ShopImageChangeRequest;
import com.tastyhouse.domain.shop.model.ShopImageType;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 사장님 한마디({@code INTRODUCTION}) 변경이력 기록 회귀 테스트.
 *
 * <p>사장님 한마디는 append-only 이력이라 저장 자체는 늘 성공하므로, 변경이력 기록이 빠져도 화면상
 * 아무 증상이 없다 — 그래서 이 테스트가 필요하다. 저장 1회당 1행이며 변경 전 값은 저장 전 최신 문구다.
 */
class ShopIntroductionRecordingTest {

    private static final Long SHOP_ID = 1L;

    private RecordingShopChangeHistoryRepository shopChangeHistoryRepository;
    private ShopLifecycleService shopLifecycleService;

    /**
     * 사장님 한마디 append-only 이력만 구현하고, 나머지는 호출되면 즉시 실패시켜 의도치 않은 의존을 드러낸다.
     */
    private static final class FakeShopDetailRepository implements ShopDetailRepository {

        private final List<ShopOwnerMessageHistory> ownerMessages = new ArrayList<>();
        private long sequence = 0L;

        @Override
        public void saveOwnerMessage(ShopOwnerMessageHistory ownerMessageHistory) {
            ownerMessages.add(ShopOwnerMessageHistory.reconstitute(
                ++sequence, ownerMessageHistory.getShopId(), ownerMessageHistory.getMessage(), null
            ));
        }

        @Override
        public Optional<ShopOwnerMessageHistory> findLatestOwnerMessage(Long shopId) {
            return ownerMessages.stream()
                .filter(message -> message.getShopId().equals(ShopId.of(shopId)))
                .reduce((first, second) -> second);
        }

        @Override
        public Optional<ShopAmenityCategory> findAmenityCategoryById(Long id) {
            throw unsupported();
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
        public ShopAmenity saveAmenity(ShopAmenity amenity) {
            throw unsupported();
        }

        @Override
        public void deleteAmenityByShopIdAndCategoryId(Long shopId, Long shopAmenityCategoryId) {
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

        private UnsupportedOperationException unsupported() {
            return new UnsupportedOperationException("이 테스트는 이 경로를 쓰지 않는다");
        }
    }

    private static final class FakeShopRepository implements ShopRepository {

        private final Map<Long, Shop> shops = new HashMap<>();

        FakeShopRepository() {
            shops.put(SHOP_ID, Shop.reconstitute(
                SHOP_ID, null, null, "맛있는 분식",
                BigDecimal.valueOf(37.497942), BigDecimal.valueOf(127.027621), 4.5,
                "서울시 송파구 위례성대로 10", "서울시 송파구 방이동 44-1", "02-1234-5678",
                null, null, false, false, false, 10000, false, false, null, null
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

    private static final class FakeShopBookmarkRepository implements ShopBookmarkRepository {

        @Override
        public boolean existsByShopIdAndMemberId(Long shopId, MemberId memberId) {
            throw new UnsupportedOperationException("이 테스트는 이 경로를 쓰지 않는다");
        }

        @Override
        public void deleteByShopIdAndMemberId(Long shopId, MemberId memberId) {
            throw new UnsupportedOperationException("이 테스트는 이 경로를 쓰지 않는다");
        }

        @Override
        public ShopBookmark save(ShopBookmark shopBookmark) {
            throw new UnsupportedOperationException("이 테스트는 이 경로를 쓰지 않는다");
        }
    }

    private static final class FakeShopImageChangeRequestRepository implements ShopImageChangeRequestRepository {

        @Override
        public ShopImageChangeRequest save(ShopImageChangeRequest shopImageChangeRequest) {
            throw new UnsupportedOperationException("이 테스트는 이 경로를 쓰지 않는다");
        }

        @Override
        public Optional<ShopImageChangeRequest> findById(Long id) {
            return Optional.empty();
        }

        @Override
        public boolean existsByShopIdAndImageTypeAndStatus(Long shopId, ShopImageType imageType, ApprovalStatus status) {
            return false;
        }

        @Override
        public boolean existsByShopIdAndStatus(Long shopId, ApprovalStatus status) {
            return false;
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
        ShopChangeHistoryRecorder recorder = new ShopChangeHistoryRecorder(shopChangeHistoryRepository);
        shopLifecycleService = new ShopLifecycleService(
            new FakeShopRepository(),
            new FakeShopDetailRepository(),
            new FakeShopBookmarkRepository(),
            id -> true,
            new ShopImageApprovalService(
                new FakeShopImageChangeRequestRepository(),
                new FakeShopRepository(),
                recorder,
                new ShopRequestIndexRecorder(new RecordingShopRequestIndexRepository())
            ),
            new ProhibitedWordValidator(new FakeProhibitedWordRepository()),
            recorder,
            new ShopCeoAssignmentRecorder(new RecordingShopCeoAssignmentHistoryRepository())
        );
    }

    @Test
    @DisplayName("사장님 한마디를 등록하면 INTRODUCTION 이력이 정확히 1건 남는다")
    void createOwnerMessage_recordsExactlyOneHistory() {
        shopLifecycleService.createOwnerMessage(SHOP_ID, "언제나 정성을 다하겠습니다", ShopChangeActor.ceo(7L));

        assertThat(shopChangeHistoryRepository.savedOf(ShopChangeType.INTRODUCTION)).hasSize(1);
        ShopChangeHistory history = shopChangeHistoryRepository.savedOf(ShopChangeType.INTRODUCTION).getFirst();
        assertThat(history.getActionType()).isEqualTo(ShopChangeActionType.UPDATE);
        assertThat(history.getActorType()).isEqualTo(ShopChangeActorType.CEO);
        assertThat(history.getActorId()).isEqualTo(7L);
        assertThat(history.getPreviousValue()).isEqualTo("미설정");
        assertThat(history.getNewValue()).isEqualTo("언제나 정성을 다하겠습니다");
    }

    @Test
    @DisplayName("변경 전 값은 저장 전 최신 문구이며 원문 그대로 담긴다(자르지 않는다)")
    void createOwnerMessage_usesLatestMessageAsPreviousValue() {
        String longMessage = "가".repeat(500);
        shopLifecycleService.createOwnerMessage(SHOP_ID, "이전 한마디", ShopChangeActor.ceo(7L));
        shopLifecycleService.createOwnerMessage(SHOP_ID, longMessage, ShopChangeActor.ceo(7L));

        List<ShopChangeHistory> histories = shopChangeHistoryRepository.savedOf(ShopChangeType.INTRODUCTION);
        assertThat(histories).hasSize(2);
        assertThat(histories.get(1).getPreviousValue()).isEqualTo("이전 한마디");
        assertThat(histories.get(1).getNewValue()).isEqualTo(longMessage);
    }
}
