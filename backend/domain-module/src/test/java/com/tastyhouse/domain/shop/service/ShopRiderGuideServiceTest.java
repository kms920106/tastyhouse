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

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.shop.model.ProhibitedWord;
import com.tastyhouse.domain.shop.model.RiderGuideActionType;
import com.tastyhouse.domain.shop.model.RiderGuideActorType;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.model.ShopChangeActionType;
import com.tastyhouse.domain.shop.model.ShopChangeActorType;
import com.tastyhouse.domain.shop.model.ShopChangeHistory;
import com.tastyhouse.domain.shop.model.ShopChangeType;
import com.tastyhouse.domain.shop.model.ShopRiderGuide;
import com.tastyhouse.domain.shop.model.ShopRiderGuideHistory;
import com.tastyhouse.domain.shop.repository.ProhibitedWordRepository;
import com.tastyhouse.domain.shop.repository.ShopRiderGuideRepository;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 라이더 안내 오케스트레이션 단위 테스트. 리포지토리 포트를 fake로 대체해 Spring/DB 없이
 * 불변식 순서와 이력 기록을 검증한다.
 */
class ShopRiderGuideServiceTest {

    private static final Long OPEN_SHOP_ID = 1L;
    private static final Long CLOSED_SHOP_ID = 2L;
    private static final Long MISSING_SHOP_ID = 99L;

    private FakeShopRiderGuideRepository shopRiderGuideRepository;
    private RecordingShopChangeHistoryRepository shopChangeHistoryRepository;
    private ShopRiderGuideService shopRiderGuideService;

    /**
     * 라이더 안내 write 포트를 대신하는 in-memory fake. 저장된 행과 이력을 그대로 들여다볼 수 있게 한다.
     */
    private static class FakeShopRiderGuideRepository implements ShopRiderGuideRepository {

        private final Map<Long, ShopRiderGuide> guides = new HashMap<>();
        private final List<ShopRiderGuideHistory> histories = new ArrayList<>();
        private long historySequence = 0L;

        @Override
        public Optional<ShopRiderGuide> findByShopId(ShopId shopId) {
            return Optional.ofNullable(guides.get(shopId.value()));
        }

        @Override
        public ShopRiderGuide save(ShopRiderGuide riderGuide) {
            guides.put(riderGuide.getShopId().value(), riderGuide);
            return riderGuide;
        }

        @Override
        public ShopRiderGuideHistory saveHistory(ShopRiderGuideHistory history) {
            ShopRiderGuideHistory saved = ShopRiderGuideHistory.reconstitute(
                ++historySequence, history.getShopId(), history.getActorType(), history.getActorId(),
                history.getActionType(), history.getPreviousVisitGuide(), history.getNewVisitGuide(),
                history.getReason(), null
            );
            histories.add(saved);
            return saved;
        }
    }

    /**
     * 가게 write 포트를 대신하는 fake. 영업 중 가게와 폐업 가게만 담는다.
     */
    private static class FakeShopRepository implements com.tastyhouse.domain.shop.repository.ShopRepository {

        private final Map<Long, Shop> shops = new HashMap<>();

        FakeShopRepository() {
            shops.put(OPEN_SHOP_ID, shop(OPEN_SHOP_ID, false));
            shops.put(CLOSED_SHOP_ID, shop(CLOSED_SHOP_ID, true));
        }

        private static Shop shop(Long id, boolean permanentlyClosed) {
            return Shop.reconstitute(
                id, null, null, "맛있는 분식",
                BigDecimal.valueOf(37.497942), BigDecimal.valueOf(127.027621), 4.5,
                "서울시 송파구 위례성대로 10", "서울시 송파구 방이동 44-1", "02-1234-5678",
                null, null, permanentlyClosed, false, false, 10000, false, false, null, null
            );
        }

        @Override
        public Optional<Shop> findById(ShopId shopId) {
            return Optional.ofNullable(shops.get(shopId.value()));
        }

        @Override
        public Optional<Shop> findVisibleById(ShopId shopId) {
            return findById(shopId).filter(shop -> !shop.isPermanentlyClosed());
        }

        @Override
        public Shop save(Shop shop) {
            shops.put(shop.getShopId().value(), shop);
            return shop;
        }
    }

    private static class FakeProhibitedWordRepository implements ProhibitedWordRepository {

        @Override
        public List<ProhibitedWord> findAll() {
            return List.of(ProhibitedWord.reconstitute(1L, "전화주문", "전화 주문 유도"));
        }
    }

    @BeforeEach
    void setUp() {
        shopRiderGuideRepository = new FakeShopRiderGuideRepository();
        shopChangeHistoryRepository = new RecordingShopChangeHistoryRepository();
        shopRiderGuideService = new ShopRiderGuideService(
            shopRiderGuideRepository,
            new FakeShopRepository(),
            new ShopRiderGuideValidator(new ProhibitedWordValidator(new FakeProhibitedWordRepository())),
            new ShopChangeHistoryRecorder(shopChangeHistoryRepository)
        );
    }

    @Test
    @DisplayName("문구를 처음 등록하면 행이 생기고 UPDATE 이력이 남는다")
    void updateVisitGuide_createsGuideAndHistory() {
        shopRiderGuideService.updateVisitGuide(
            OPEN_SHOP_ID, "OO 약국 상가 왼쪽 문으로 들어오시면 됩니다.", RiderGuideActorType.CEO, 7L
        );

        ShopRiderGuide saved = shopRiderGuideRepository.findByShopId(ShopId.of(OPEN_SHOP_ID)).orElseThrow();
        assertThat(saved.getVisitGuide()).isEqualTo("OO 약국 상가 왼쪽 문으로 들어오시면 됩니다.");

        assertThat(shopRiderGuideRepository.histories).hasSize(1);
        ShopRiderGuideHistory history = shopRiderGuideRepository.histories.getFirst();
        assertThat(history.getActionType()).isEqualTo(RiderGuideActionType.UPDATE);
        assertThat(history.getActorType()).isEqualTo(RiderGuideActorType.CEO);
        assertThat(history.getActorId()).isEqualTo(7L);
        assertThat(history.getPreviousVisitGuide()).isNull();
        assertThat(history.getNewVisitGuide()).isEqualTo("OO 약국 상가 왼쪽 문으로 들어오시면 됩니다.");
        assertThat(history.getReason()).isNull();
    }

    @Test
    @DisplayName("이력에는 변경 전 문구가 덮이기 전 값으로 기록된다")
    void updateVisitGuide_recordsPreviousGuide() {
        shopRiderGuideService.updateVisitGuide(OPEN_SHOP_ID, "이전 문구", RiderGuideActorType.CEO, 7L);
        shopRiderGuideService.updateVisitGuide(OPEN_SHOP_ID, "새 문구", RiderGuideActorType.CEO, 7L);

        ShopRiderGuideHistory latest = shopRiderGuideRepository.histories.get(1);
        assertThat(latest.getPreviousVisitGuide()).isEqualTo("이전 문구");
        assertThat(latest.getNewVisitGuide()).isEqualTo("새 문구");
    }

    @Test
    @DisplayName("빈 문구를 등록하면 문구가 비워진다(빈 값 = 삭제)")
    void updateVisitGuide_clearsGuide_whenBlank() {
        shopRiderGuideService.updateVisitGuide(OPEN_SHOP_ID, "등록된 문구", RiderGuideActorType.CEO, 7L);

        shopRiderGuideService.updateVisitGuide(OPEN_SHOP_ID, "", RiderGuideActorType.CEO, 7L);

        ShopRiderGuide saved = shopRiderGuideRepository.findByShopId(ShopId.of(OPEN_SHOP_ID)).orElseThrow();
        assertThat(saved.getVisitGuide()).isNull();
    }

    @Test
    @DisplayName("폐업 가게는 문구를 수정할 수 없다")
    void updateVisitGuide_throwsException_whenShopPermanentlyClosed() {
        assertThatThrownBy(() -> shopRiderGuideService.updateVisitGuide(
            CLOSED_SHOP_ID, "안내 문구", RiderGuideActorType.CEO, 7L
        ))
            .isInstanceOf(BusinessException.class)
            .extracting(exception -> ((BusinessException) exception).getErrorCode())
            .isEqualTo(ErrorCode.SHOP_ALREADY_PERMANENTLY_CLOSED);
    }

    @Test
    @DisplayName("등록 기준 위반 문구는 저장되지 않고 이력도 남지 않는다")
    void updateVisitGuide_doesNotPersist_whenViolatingGuideline() {
        assertThatThrownBy(() -> shopRiderGuideService.updateVisitGuide(
            OPEN_SHOP_ID, "18인치 피자는 자동차 라이더만 수행 부탁드립니다.", RiderGuideActorType.CEO, 7L
        ))
            .isInstanceOf(BusinessException.class);

        assertThat(shopRiderGuideRepository.findByShopId(ShopId.of(OPEN_SHOP_ID))).isEmpty();
        assertThat(shopRiderGuideRepository.histories).isEmpty();
    }

    @Test
    @DisplayName("관리자 삭제 조치는 문구만 비우고 픽업 위치는 유지한다")
    void deleteVisitGuide_clearsGuideAndKeepsPickupLocation() {
        shopRiderGuideService.updateVisitGuide(OPEN_SHOP_ID, "부적합 문구", RiderGuideActorType.CEO, 7L);
        shopRiderGuideService.updatePickupLocation(
            OPEN_SHOP_ID, "서울시 강남구 테헤란로 1", null, "지하 1층 후문",
            BigDecimal.valueOf(37.497942), BigDecimal.valueOf(127.027621),
            RiderGuideActorType.CEO, 7L
        );

        shopRiderGuideService.deleteVisitGuide(OPEN_SHOP_ID, 3L, "가게 방문과 관련 없는 문구입니다.");

        ShopRiderGuide saved = shopRiderGuideRepository.findByShopId(ShopId.of(OPEN_SHOP_ID)).orElseThrow();
        assertThat(saved.getVisitGuide()).isNull();
        assertThat(saved.hasPickupLocation()).isTrue();

        ShopRiderGuideHistory latest = shopRiderGuideRepository.histories.getLast();
        assertThat(latest.getActionType()).isEqualTo(RiderGuideActionType.DELETION);
        assertThat(latest.getPreviousVisitGuide()).isEqualTo("부적합 문구");
        assertThat(latest.getNewVisitGuide()).isNull();
        assertThat(latest.getReason()).isEqualTo("가게 방문과 관련 없는 문구입니다.");
    }

    @Test
    @DisplayName("존재하지 않는 가게에 삭제 조치하면 SHOP_NOT_FOUND로 구분해 알린다")
    void deleteVisitGuide_throwsShopNotFound_whenShopMissing() {
        assertThatThrownBy(() -> shopRiderGuideService.deleteVisitGuide(MISSING_SHOP_ID, 3L, "사유"))
            .isInstanceOf(ResourceNotFoundException.class)
            .extracting(exception -> ((ResourceNotFoundException) exception).getErrorCode())
            .isEqualTo(ErrorCode.SHOP_NOT_FOUND);
    }

    @Test
    @DisplayName("문구가 없는 가게에 삭제 조치하면 SHOP_RIDER_VISIT_GUIDE_NOT_FOUND를 던진다")
    void deleteVisitGuide_throwsGuideNotFound_whenGuideAbsent() {
        assertThatThrownBy(() -> shopRiderGuideService.deleteVisitGuide(OPEN_SHOP_ID, 3L, "사유"))
            .isInstanceOf(ResourceNotFoundException.class)
            .extracting(exception -> ((ResourceNotFoundException) exception).getErrorCode())
            .isEqualTo(ErrorCode.SHOP_RIDER_VISIT_GUIDE_NOT_FOUND);
    }

    @Test
    @DisplayName("수정 요청은 문구를 그대로 두고 이력만 남기며 이력 ID를 반환한다")
    void requestRevision_keepsGuideAndRecordsHistory() {
        shopRiderGuideService.updateVisitGuide(OPEN_SHOP_ID, "검토 대상 문구", RiderGuideActorType.CEO, 7L);

        Long historyId = shopRiderGuideService.requestRevision(OPEN_SHOP_ID, 3L, "위치 안내로 수정해 주세요.");

        assertThat(historyId).isNotNull();

        ShopRiderGuide saved = shopRiderGuideRepository.findByShopId(ShopId.of(OPEN_SHOP_ID)).orElseThrow();
        assertThat(saved.getVisitGuide()).isEqualTo("검토 대상 문구");

        ShopRiderGuideHistory latest = shopRiderGuideRepository.histories.getLast();
        assertThat(latest.getActionType()).isEqualTo(RiderGuideActionType.REVISION_REQUEST);
        assertThat(latest.getNewVisitGuide()).isEqualTo("검토 대상 문구");
        assertThat(latest.getReason()).isEqualTo("위치 안내로 수정해 주세요.");
    }

    @Test
    @DisplayName("픽업 위치 초기화는 행이 없으면 빈 행을 만들지 않는다")
    void clearPickupLocation_doesNotCreateRow_whenNeverRegistered() {
        shopRiderGuideService.clearPickupLocation(OPEN_SHOP_ID, RiderGuideActorType.CEO, 7L);

        assertThat(shopRiderGuideRepository.findByShopId(ShopId.of(OPEN_SHOP_ID))).isEmpty();
    }

    @Test
    @DisplayName("픽업 위치 초기화는 픽업 필드만 비우고 문구는 유지한다")
    void clearPickupLocation_clearsOnlyPickupFields() {
        shopRiderGuideService.updateVisitGuide(OPEN_SHOP_ID, "유지될 문구", RiderGuideActorType.CEO, 7L);
        shopRiderGuideService.updatePickupLocation(
            OPEN_SHOP_ID, "서울시 강남구 테헤란로 1", null, null,
            BigDecimal.valueOf(37.497942), BigDecimal.valueOf(127.027621),
            RiderGuideActorType.CEO, 7L
        );

        shopRiderGuideService.clearPickupLocation(OPEN_SHOP_ID, RiderGuideActorType.CEO, 7L);

        ShopRiderGuide saved = shopRiderGuideRepository.findByShopId(ShopId.of(OPEN_SHOP_ID)).orElseThrow();
        assertThat(saved.hasPickupLocation()).isFalse();
        assertThat(saved.getVisitGuide()).isEqualTo("유지될 문구");
    }

    @Test
    @DisplayName("이미 미설정 상태에서 픽업 위치를 초기화해도 예외가 없다(멱등)")
    void clearPickupLocation_isIdempotent() {
        shopRiderGuideService.updateVisitGuide(OPEN_SHOP_ID, "문구", RiderGuideActorType.CEO, 7L);

        assertThatCode(() -> {
            shopRiderGuideService.clearPickupLocation(OPEN_SHOP_ID, RiderGuideActorType.CEO, 7L);
            shopRiderGuideService.clearPickupLocation(OPEN_SHOP_ID, RiderGuideActorType.CEO, 7L);
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("폐업 가게는 픽업 위치를 초기화할 수 없다")
    void clearPickupLocation_throwsException_whenShopPermanentlyClosed() {
        assertThatThrownBy(() -> shopRiderGuideService.clearPickupLocation(CLOSED_SHOP_ID, RiderGuideActorType.CEO, 7L))
            .isInstanceOf(BusinessException.class)
            .extracting(exception -> ((BusinessException) exception).getErrorCode())
            .isEqualTo(ErrorCode.SHOP_ALREADY_PERMANENTLY_CLOSED);
    }

    @Test
    @DisplayName("픽업 위치 등록은 검수 이력을 남기지 않는다(검수 대상은 안내 문구)")
    void updatePickupLocation_doesNotRecordHistory() {
        shopRiderGuideService.updatePickupLocation(
            OPEN_SHOP_ID, "서울시 강남구 테헤란로 1", null, null,
            BigDecimal.valueOf(37.497942), BigDecimal.valueOf(127.027621),
            RiderGuideActorType.CEO, 7L
        );

        assertThat(shopRiderGuideRepository.histories).isEmpty();
    }

    @Test
    @DisplayName("점주가 안내 문구를 바꾸면 RIDER_VISIT_GUIDE 변경이력이 정확히 1건 남는다")
    void updateVisitGuide_recordsExactlyOneShopChangeHistory_whenActorIsCeo() {
        shopRiderGuideService.updateVisitGuide(OPEN_SHOP_ID, "정문 옆 계단으로 올라와 주세요", RiderGuideActorType.CEO, 7L);

        assertThat(shopChangeHistoryRepository.savedOf(ShopChangeType.RIDER_VISIT_GUIDE)).hasSize(1);
        ShopChangeHistory history = shopChangeHistoryRepository.savedOf(ShopChangeType.RIDER_VISIT_GUIDE).getFirst();
        assertThat(history.getActionType()).isEqualTo(ShopChangeActionType.UPDATE);
        assertThat(history.getActorType()).isEqualTo(ShopChangeActorType.CEO);
        assertThat(history.getActorId()).isEqualTo(7L);
        assertThat(history.getPreviousValue()).isEqualTo("미설정");
        assertThat(history.getNewValue()).isEqualTo("정문 옆 계단으로 올라와 주세요");
    }

    @Test
    @DisplayName("관리자 검수 조치는 SHOP_CHANGE_HISTORY에 남지 않는다(점주가 한 변경이 아니다)")
    void adminReviewActions_doNotRecordShopChangeHistory() {
        shopRiderGuideService.updateVisitGuide(OPEN_SHOP_ID, "부적합 문구", RiderGuideActorType.ADMIN, 3L);
        shopRiderGuideService.requestRevision(OPEN_SHOP_ID, 3L, "위치 안내로 수정해 주세요.");
        shopRiderGuideService.deleteVisitGuide(OPEN_SHOP_ID, 3L, "가게 방문과 관련 없는 문구입니다.");
        shopRiderGuideService.updatePickupLocation(
            OPEN_SHOP_ID, "서울시 강남구 테헤란로 1", null, null,
            BigDecimal.valueOf(37.497942), BigDecimal.valueOf(127.027621),
            RiderGuideActorType.ADMIN, 3L
        );

        assertThat(shopChangeHistoryRepository.saved()).isEmpty();
    }

    @Test
    @DisplayName("점주가 픽업 위치를 등록·해제하면 RIDER_PICKUP_LOCATION 이력이 각각 1건씩 남는다")
    void pickupLocation_recordsOneShopChangeHistoryPerChange_whenActorIsCeo() {
        shopRiderGuideService.updatePickupLocation(
            OPEN_SHOP_ID, "서울시 강남구 테헤란로 1", null, "지하 1층 후문",
            BigDecimal.valueOf(37.497942), BigDecimal.valueOf(127.027621),
            RiderGuideActorType.CEO, 7L
        );
        shopRiderGuideService.clearPickupLocation(OPEN_SHOP_ID, RiderGuideActorType.CEO, 7L);

        List<ShopChangeHistory> histories =
            shopChangeHistoryRepository.savedOf(ShopChangeType.RIDER_PICKUP_LOCATION);
        assertThat(histories).hasSize(2);
        assertThat(histories.getFirst().getActionType()).isEqualTo(ShopChangeActionType.UPDATE);
        assertThat(histories.getFirst().getPreviousValue()).isEqualTo("미설정");
        assertThat(histories.getFirst().getNewValue()).isEqualTo("서울시 강남구 테헤란로 1 (지하 1층 후문)");
        assertThat(histories.get(1).getActionType()).isEqualTo(ShopChangeActionType.DELETE);
        assertThat(histories.get(1).getPreviousValue()).isEqualTo("서울시 강남구 테헤란로 1 (지하 1층 후문)");
        assertThat(histories.get(1).getNewValue()).isNull();
    }
}
