package com.tastyhouse.domain.shop.domain.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.shop.model.DayType;
import com.tastyhouse.domain.shop.model.OrderMethod;
import com.tastyhouse.domain.shop.model.OrderUnavailableReason;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.model.ShopAmenity;
import com.tastyhouse.domain.shop.model.ShopAmenityCategory;
import com.tastyhouse.domain.shop.model.ShopBannerImage;
import com.tastyhouse.domain.shop.model.ShopBreakTime;
import com.tastyhouse.domain.shop.model.ShopBusinessHour;
import com.tastyhouse.domain.shop.model.ShopClosedDay;
import com.tastyhouse.domain.shop.model.ShopFoodType;
import com.tastyhouse.domain.shop.model.ShopFoodTypeCategory;
import com.tastyhouse.domain.shop.model.ShopOrderMethod;
import com.tastyhouse.domain.shop.model.ShopOwnerMessageHistory;
import com.tastyhouse.domain.shop.model.ShopPhotoCategory;
import com.tastyhouse.domain.shop.model.ShopPhotoCategoryImage;
import com.tastyhouse.domain.shop.model.ShopSuspension;
import com.tastyhouse.domain.shop.model.ShopTemporaryClosure;
import com.tastyhouse.domain.shop.model.SuspensionReason;
import com.tastyhouse.domain.shop.repository.ShopDetailRepository;
import com.tastyhouse.domain.shop.repository.ShopRepository;
import com.tastyhouse.domain.shop.repository.ShopSuspensionRepository;
import com.tastyhouse.domain.shop.repository.ShopTemporaryClosureRepository;
import com.tastyhouse.domain.shop.service.ShopOperatingStatusCalculator;
import com.tastyhouse.domain.shop.service.ShopOperatingStatusService;
import com.tastyhouse.domain.shop.service.ShopOrderAvailabilityService;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.shop.vo.StationId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 주문 접수 게이트 단위 테스트 — 주문 접수·예약 생성이 공유하는 검증 3종을 검증한다.
 *
 * <p>순수 POJO이므로 Spring 컨텍스트·JPA 없이 write 포트를 손으로 만든 fake로 대체한다
 * (domain-module에는 Mockito 의존이 없다).
 *
 * <p>{@code OrderPlacementService}·{@code ReservationBookingService}를 직접 검증하지 않는 이유는
 * 그 서비스들이 각각 17개·5개 리포지토리를 주입받는 반면 <b>검증 규칙 자체는 전부 이 서비스에 있기
 * 때문</b>이다 — 게이트를 여기서 검증하면 같은 규칙을 두 벌 검증하지 않아도 된다.
 */
class ShopOrderAvailabilityServiceTest {

    private static final ShopId SHOP_ID = ShopId.of(1L);

    /** 2026-07-27(월) 정오 — 아래 fake의 영업시간(09:00~22:00) 안. */
    private static final LocalDateTime MONDAY_NOON = LocalDateTime.of(2026, 7, 27, 12, 0);

    /**
     * 검증 대상 가게. 호출부가 로드해 넘기는 구조이므로 fixture와 호출이 같은 인스턴스를 쓴다.
     */
    private final Shop shop = openShop();

    @Test
    @DisplayName("영업중 + 배정된 유형 + 중지 없음이면 통과한다")
    void validateOrderable_passes_whenOpenAndAssignedAndNotSuspended() {
        ShopOrderAvailabilityService service = service(shop, List.of(OrderMethod.DELIVERY), List.of());

        assertThatCode(() -> service.validateOrderable(shop, OrderMethod.DELIVERY, MONDAY_NOON))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("영업시간 밖이면 SHOP_NOT_ORDERABLE로 거부한다")
    void validateOrderable_rejects_whenShopNotOrderable() {
        ShopOrderAvailabilityService service = service(shop, List.of(OrderMethod.DELIVERY), List.of());

        // 23:00은 영업시간(09:00~22:00) 밖
        assertThatThrownBy(() -> service.validateOrderable(shop, OrderMethod.DELIVERY, MONDAY_NOON.withHour(23)))
            .isInstanceOf(BusinessException.class)
            .extracting(exception -> ((BusinessException) exception).getErrorCode())
            .isEqualTo(ErrorCode.SHOP_NOT_ORDERABLE);
    }

    @Test
    @DisplayName("전체 대상 임시중지면 SHOP_NOT_ORDERABLE로 거부한다")
    void validateOrderable_rejects_whenShopWideSuspension() {
        ShopOrderAvailabilityService service =
            service(shop, List.of(OrderMethod.DELIVERY), List.of(suspension(null)));

        assertThatThrownBy(() -> service.validateOrderable(shop, OrderMethod.DELIVERY, MONDAY_NOON))
            .isInstanceOf(BusinessException.class)
            .extracting(exception -> ((BusinessException) exception).getErrorCode())
            .isEqualTo(ErrorCode.SHOP_NOT_ORDERABLE);
    }

    @Test
    @DisplayName("배정되지 않은 주문유형이면 SHOP_ORDER_METHOD_NOT_SUPPORTED로 거부한다")
    void validateOrderable_rejects_whenOrderMethodNotAssigned() {
        ShopOrderAvailabilityService service = service(shop, List.of(OrderMethod.TAKEOUT), List.of());

        assertThatThrownBy(() -> service.validateOrderable(shop, OrderMethod.DELIVERY, MONDAY_NOON))
            .isInstanceOf(BusinessException.class)
            .extracting(exception -> ((BusinessException) exception).getErrorCode())
            .isEqualTo(ErrorCode.SHOP_ORDER_METHOD_NOT_SUPPORTED);
    }

    @Test
    @DisplayName("그 유형만 임시중지면 SHOP_ORDER_METHOD_SUSPENDED로 거부한다")
    void validateOrderable_rejects_whenOrderMethodSuspended() {
        ShopOrderAvailabilityService service = service(
            shop, List.of(OrderMethod.DELIVERY, OrderMethod.TAKEOUT), List.of(suspension(OrderMethod.DELIVERY))
        );

        assertThatThrownBy(() -> service.validateOrderable(shop, OrderMethod.DELIVERY, MONDAY_NOON))
            .isInstanceOf(BusinessException.class)
            .extracting(exception -> ((BusinessException) exception).getErrorCode())
            .isEqualTo(ErrorCode.SHOP_ORDER_METHOD_SUSPENDED);
    }

    @Test
    @DisplayName("배달만 임시중지된 가게에 포장 주문은 통과한다 — 결함 A 수정의 핵심 시나리오")
    void validateOrderable_passes_forTakeout_whenOnlyDeliverySuspended() {
        ShopOrderAvailabilityService service = service(
            shop, List.of(OrderMethod.DELIVERY, OrderMethod.TAKEOUT), List.of(suspension(OrderMethod.DELIVERY))
        );

        assertThatCode(() -> service.validateOrderable(shop, OrderMethod.TAKEOUT, MONDAY_NOON))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("검증 순서상 미배정이 유형별 중지보다 먼저 걸린다")
    void validateOrderable_reportsNotSupported_beforeSuspended() {
        // DELIVERY가 미배정이면서 동시에 중지된 상태 — 사유는 미배정이 먼저다
        ShopOrderAvailabilityService service =
            service(shop, List.of(OrderMethod.TAKEOUT), List.of(suspension(OrderMethod.DELIVERY)));

        assertThatThrownBy(() -> service.validateOrderable(shop, OrderMethod.DELIVERY, MONDAY_NOON))
            .isInstanceOf(BusinessException.class)
            .extracting(exception -> ((BusinessException) exception).getErrorCode())
            .isEqualTo(ErrorCode.SHOP_ORDER_METHOD_NOT_SUPPORTED);
    }

    @Test
    @DisplayName("예약 슬롯 시각이 영업시간 안이면 지금이 영업시간 밖이어도 통과한다")
    void validateOrderable_usesGivenTime_notNow() {
        ShopOrderAvailabilityService service = service(shop, List.of(OrderMethod.RESERVATION), List.of());

        // 판정 기준을 미래 슬롯 시각(영업시간 안)으로 주면 통과한다 —
        // 예약이 "지금"이 아니라 슬롯 시각으로 판정된다는 근거.
        assertThatCode(() -> service.validateOrderable(shop, OrderMethod.RESERVATION, MONDAY_NOON))
            .doesNotThrowAnyException();
        assertThatThrownBy(() -> service.validateOrderable(shop, OrderMethod.RESERVATION, MONDAY_NOON.withHour(5)))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("유형별 중지 거절 메시지에는 그 판정의 사유가 함께 담긴다")
    void validateOrderable_includesReason_whenOrderMethodSuspended() {
        ShopOrderAvailabilityService service = service(
            shop, List.of(OrderMethod.DELIVERY, OrderMethod.TAKEOUT), List.of(suspension(OrderMethod.DELIVERY))
        );

        assertThatThrownBy(() -> service.validateOrderable(shop, OrderMethod.DELIVERY, MONDAY_NOON))
            .hasMessageContaining(OrderUnavailableReason.SUSPENDED.getDisplayName());
    }

    @Test
    @DisplayName("게이트는 넘겨받은 가게로만 판정한다 — 내부에서 다시 읽어 폐업·노출정지 가게를 되살리지 않는다")
    void validateOrderable_judgesPassedShop_withoutReloading() {
        // 리포지토리에는 정상 가게가 있지만 호출부가 노출정지 가게를 넘긴 상황.
        // 게이트가 내부에서 findById로 다시 읽는다면 정상 가게로 판정해 통과해 버린다.
        ShopOrderAvailabilityService service = service(shop, List.of(OrderMethod.DELIVERY), List.of());
        Shop hiddenShop = Shop.reconstitute(
            1L, null, StationId.of(1L), "가게", BigDecimal.valueOf(37.5), BigDecimal.valueOf(127.0),
            4.5, "도로명", "지번", "02-000-0000", null, null,
            false, true, false, 0, false, LocalDateTime.now(), LocalDateTime.now()
        );

        assertThatThrownBy(() -> service.validateOrderable(hiddenShop, OrderMethod.DELIVERY, MONDAY_NOON))
            .isInstanceOf(BusinessException.class)
            .extracting(exception -> ((BusinessException) exception).getErrorCode())
            .isEqualTo(ErrorCode.SHOP_NOT_ORDERABLE);
    }

    // ------------------------------------------------------------------ fixtures

    private ShopOrderAvailabilityService service(
        Shop shop,
        List<OrderMethod> assignedOrderMethods,
        List<ShopSuspension> suspensions
    ) {
        ShopDetailRepository shopDetailRepository = new ShopDetailRepositoryFake(assignedOrderMethods);
        ShopOperatingStatusService operatingStatusService = new ShopOperatingStatusService(
            new ShopRepositoryFake(shop),
            shopDetailRepository,
            new ShopTemporaryClosureRepositoryFake(),
            new ShopSuspensionRepositoryFake(suspensions),
            new ShopOperatingStatusCalculator()
        );
        return new ShopOrderAvailabilityService(operatingStatusService, shopDetailRepository);
    }

    private Shop openShop() {
        return Shop.reconstitute(
            1L, null, StationId.of(1L), "가게", BigDecimal.valueOf(37.5), BigDecimal.valueOf(127.0),
            4.5, "도로명", "지번", "02-000-0000", null, null,
            false, false, false, 0, false, LocalDateTime.now(), LocalDateTime.now()
        );
    }

    /** MONDAY_NOON을 포함하는 활성 임시중지. */
    private ShopSuspension suspension(OrderMethod orderMethod) {
        return ShopSuspension.reconstitute(
            1L, SHOP_ID, SuspensionReason.SHOP_CIRCUMSTANCE, orderMethod,
            MONDAY_NOON.minusHours(1), MONDAY_NOON.plusHours(1), null, null, null
        );
    }

    private static final class ShopRepositoryFake implements ShopRepository {

        private final Shop shop;

        private ShopRepositoryFake(Shop shop) {
            this.shop = shop;
        }

        @Override
        public Optional<Shop> findById(ShopId id) {
            return Optional.of(shop);
        }

        @Override
        public Optional<Shop> findVisibleById(ShopId id) {
            return shop.isPermanentlyClosed() || shop.isHidden() ? Optional.empty() : Optional.of(shop);
        }

        @Override
        public Shop save(Shop shop) {
            throw new UnsupportedOperationException("이 테스트는 저장 경로를 쓰지 않는다");
        }
    }

    private static final class ShopSuspensionRepositoryFake implements ShopSuspensionRepository {

        private final List<ShopSuspension> suspensions;

        private ShopSuspensionRepositoryFake(List<ShopSuspension> suspensions) {
            this.suspensions = suspensions;
        }

        @Override
        public ShopSuspension save(ShopSuspension shopSuspension) {
            throw new UnsupportedOperationException("이 테스트는 저장 경로를 쓰지 않는다");
        }

        @Override
        public List<ShopSuspension> findByShopId(Long shopId) {
            return suspensions;
        }

        @Override
        public Optional<ShopSuspension> findById(Long id) {
            return Optional.empty();
        }
    }

    private static final class ShopTemporaryClosureRepositoryFake implements ShopTemporaryClosureRepository {

        @Override
        public ShopTemporaryClosure save(ShopTemporaryClosure shopTemporaryClosure) {
            throw new UnsupportedOperationException("이 테스트는 저장 경로를 쓰지 않는다");
        }

        @Override
        public List<ShopTemporaryClosure> findByShopId(Long shopId) {
            return List.of();
        }

        @Override
        public Optional<ShopTemporaryClosure> findById(Long id) {
            return Optional.empty();
        }

        @Override
        public void deleteById(Long id) {
            throw new UnsupportedOperationException("이 테스트는 삭제 경로를 쓰지 않는다");
        }
    }

    /**
     * 영업시간(매일 09:00~22:00)과 주문유형 배정만 돌려주는 fake. 나머지 조회는 빈 목록이고,
     * 이 테스트가 쓰지 않는 write 경로는 호출되면 즉시 실패시켜 의도치 않은 의존을 드러낸다.
     */
    private static final class ShopDetailRepositoryFake implements ShopDetailRepository {

        private final List<OrderMethod> assignedOrderMethods;

        private ShopDetailRepositoryFake(List<OrderMethod> assignedOrderMethods) {
            this.assignedOrderMethods = assignedOrderMethods;
        }

        @Override
        public List<ShopBusinessHour> findBusinessHoursByShopId(Long shopId) {
            return List.of(ShopBusinessHour.reconstitute(
                1L, SHOP_ID, DayType.DAILY, LocalTime.of(9, 0), LocalTime.of(22, 0), false, false
            ));
        }

        @Override
        public List<ShopBreakTime> findBreakTimesByShopId(Long shopId) {
            return List.of();
        }

        @Override
        public List<ShopClosedDay> findClosedDaysByShopId(Long shopId) {
            return List.of();
        }

        @Override
        public List<ShopOrderMethod> findOrderMethodsByShopId(Long shopId) {
            List<ShopOrderMethod> assigned = new java.util.ArrayList<>();
            long sequence = 0L;
            for (OrderMethod orderMethod : assignedOrderMethods) {
                assigned.add(ShopOrderMethod.reconstitute(++sequence, SHOP_ID, orderMethod));
            }
            return List.copyOf(assigned);
        }

        @Override
        public Optional<ShopAmenityCategory> findAmenityCategoryById(Long id) {
            return Optional.empty();
        }

        @Override
        public ShopAmenityCategory saveAmenityCategory(ShopAmenityCategory amenityCategory) {
            throw unsupported();
        }

        @Override
        public Optional<ShopFoodTypeCategory> findFoodTypeCategoryById(Long id) {
            return Optional.empty();
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
        public Optional<ShopBusinessHour> findBusinessHourById(Long id) {
            return Optional.empty();
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
        public Optional<ShopBreakTime> findBreakTimeById(Long id) {
            return Optional.empty();
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
        public ShopClosedDay saveClosedDay(ShopClosedDay closedDay) {
            throw unsupported();
        }

        @Override
        public void deleteClosedDayById(Long id) {
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
            return Optional.empty();
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
            return Optional.empty();
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

        private UnsupportedOperationException unsupported() {
            return new UnsupportedOperationException("이 테스트는 이 경로를 쓰지 않는다");
        }
    }
}
