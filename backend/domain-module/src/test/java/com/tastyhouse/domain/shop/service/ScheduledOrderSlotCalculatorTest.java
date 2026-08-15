package com.tastyhouse.domain.shop.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.shop.model.ClosedDayType;
import com.tastyhouse.domain.shop.model.DayType;
import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.shop.model.ScheduledOrderSlot;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.model.ShopBreakTime;
import com.tastyhouse.domain.shop.model.ShopBusinessHour;
import com.tastyhouse.domain.shop.model.ShopClosedDay;
import com.tastyhouse.domain.shop.model.ShopOrderMethod;
import com.tastyhouse.domain.shop.model.ShopSuspension;
import com.tastyhouse.domain.shop.model.ShopTemporaryClosure;
import com.tastyhouse.domain.shop.model.SuspensionReason;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.shop.vo.StationId;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 예약주문 슬롯 계산기 순수 단위 테스트. Spring/JPA 컨텍스트 없이 리드타임·경계·영업 판정 재사용을 검증한다.
 */
class ScheduledOrderSlotCalculatorTest {

    private final ScheduledOrderSlotCalculator calculator =
        new ScheduledOrderSlotCalculator(new ShopOperatingStatusCalculator());

    /** 2026-07-27은 월요일. */
    private static final LocalDate MONDAY = LocalDate.of(2026, 7, 27);

    private static final ShopId SHOP_ID = ShopId.of(1L);

    private Shop shop(boolean scheduledOrderEnabled) {
        return Shop.reconstitute(
            1L, null, StationId.of(1L), "가게", BigDecimal.valueOf(37.5), BigDecimal.valueOf(127.0),
            4.5, "도로명", "지번", "02-000-0000", null, null,
            false, false, false, 0, scheduledOrderEnabled, LocalDateTime.now(), LocalDateTime.now()
        );
    }

    private ShopBusinessHour hours(LocalTime open, LocalTime close) {
        return ShopBusinessHour.reconstitute(1L, SHOP_ID, DayType.DAILY, open, close, false, false);
    }

    private ShopBusinessHour hours24() {
        return ShopBusinessHour.reconstitute(1L, SHOP_ID, DayType.DAILY, null, null, false, true);
    }

    private LocalDateTime at(int hour, int minute) {
        return LocalDateTime.of(MONDAY, LocalTime.of(hour, minute));
    }

    /** 배달·포장 둘 다 배정된 가게 — 대부분의 케이스가 쓰는 기본 배정. */
    private List<ShopOrderMethod> allOrderMethodsAssigned() {
        return List.of(
            ShopOrderMethod.reconstitute(1L, SHOP_ID, OrderMethod.DELIVERY),
            ShopOrderMethod.reconstitute(2L, SHOP_ID, OrderMethod.TAKEOUT)
        );
    }

    private List<ScheduledOrderSlot> calculate(
        Shop shop,
        OrderMethod orderMethod,
        LocalDateTime now,
        List<ShopBusinessHour> businessHours,
        List<ShopBreakTime> breakTimes,
        List<ShopClosedDay> closedDays,
        List<ShopTemporaryClosure> temporaryClosures,
        List<ShopSuspension> suspensions
    ) {
        return calculate(shop, orderMethod, now, businessHours, breakTimes, closedDays, temporaryClosures,
            suspensions, allOrderMethodsAssigned());
    }

    private List<ScheduledOrderSlot> calculate(
        Shop shop,
        OrderMethod orderMethod,
        LocalDateTime now,
        List<ShopBusinessHour> businessHours,
        List<ShopBreakTime> breakTimes,
        List<ShopClosedDay> closedDays,
        List<ShopTemporaryClosure> temporaryClosures,
        List<ShopSuspension> suspensions,
        List<ShopOrderMethod> shopOrderMethods
    ) {
        return calculator.calculate(ScheduledOrderSlotContext.of(
            shop, orderMethod, now, businessHours, breakTimes, closedDays, temporaryClosures, suspensions,
            shopOrderMethods
        ));
    }

    /** 09:00~22:00 영업, 휴무·중지 없음 — 대부분의 케이스가 쓰는 기본 조건. */
    private List<ScheduledOrderSlot> calculateWithDefaultHours(OrderMethod orderMethod, LocalDateTime now) {
        return calculate(shop(true), orderMethod, now,
            List.of(hours(LocalTime.of(9, 0), LocalTime.of(22, 0))),
            List.of(), List.of(), List.of(), List.of());
    }

    @Test
    @DisplayName("예약주문 미운영 가게는 빈 목록")
    void empty_whenScheduledOrderDisabled() {
        List<ScheduledOrderSlot> slots = calculate(shop(false), OrderMethod.DELIVERY, at(10, 0),
            List.of(hours(LocalTime.of(9, 0), LocalTime.of(22, 0))),
            List.of(), List.of(), List.of(), List.of());

        assertThat(slots).isEmpty();
    }

    @Test
    @DisplayName("영업시간 미등록 가게는 fail-safe로 빈 목록")
    void empty_whenBusinessHoursMissing() {
        List<ScheduledOrderSlot> slots = calculate(shop(true), OrderMethod.DELIVERY, at(10, 0),
            List.of(), List.of(), List.of(), List.of(), List.of());

        assertThat(slots).isEmpty();
    }

    @Test
    @DisplayName("TABLE·RESERVATION은 예약주문을 지원하지 않아 빈 목록")
    void empty_whenOrderMethodNotSupported() {
        assertThat(calculateWithDefaultHours(OrderMethod.TABLE, at(10, 0))).isEmpty();
        assertThat(calculateWithDefaultHours(OrderMethod.RESERVATION, at(10, 0))).isEmpty();
    }

    @Test
    @DisplayName("배달은 리드타임 2시간 — now=10:00이면 첫 슬롯이 12:00이고 30분 범위다")
    void delivery_firstSlotIsTwoHoursAfterNow() {
        List<ScheduledOrderSlot> slots = calculateWithDefaultHours(OrderMethod.DELIVERY, at(10, 0));

        assertThat(slots).isNotEmpty();
        assertThat(slots.getFirst().startAt()).isEqualTo(at(12, 0));
        assertThat(slots.getFirst().endAt()).isEqualTo(at(12, 30));
    }

    @Test
    @DisplayName("포장은 리드타임 1시간 — now=10:00이면 첫 슬롯이 11:00이고 단일 시각이다")
    void takeout_firstSlotIsOneHourAfterNow() {
        List<ScheduledOrderSlot> slots = calculateWithDefaultHours(OrderMethod.TAKEOUT, at(10, 0));

        assertThat(slots).isNotEmpty();
        assertThat(slots.getFirst().startAt()).isEqualTo(at(11, 0));
        assertThat(slots.getFirst().endAt()).isEqualTo(at(11, 0));
    }

    @Test
    @DisplayName("오픈 전 조회는 '영업 시작 + 리드타임'이 하한 — now=07:00·09:00 오픈이면 첫 슬롯 11:00")
    void delivery_leadTimeIsMeasuredFromOpenTimeWhenQueriedBeforeOpen() {
        List<ScheduledOrderSlot> slots = calculateWithDefaultHours(OrderMethod.DELIVERY, at(7, 0));

        assertThat(slots.getFirst().startAt()).isEqualTo(at(11, 0));
    }

    @Test
    @DisplayName("30분 단위로 올림한다 — now=10:07이면 첫 슬롯 12:30")
    void firstSlotIsCeiledToThirtyMinutes() {
        List<ScheduledOrderSlot> slots = calculateWithDefaultHours(OrderMethod.DELIVERY, at(10, 7));

        assertThat(slots.getFirst().startAt()).isEqualTo(at(12, 30));
    }

    @Test
    @DisplayName("휴게시간 15:00~16:00이면 14:30 슬롯은 남고(끝이 15:00) 15:00·15:30 슬롯은 배제된다")
    void delivery_excludesSlotsOverlappingBreakTime() {
        List<ShopBreakTime> breakTimes = List.of(
            ShopBreakTime.reconstitute(1L, SHOP_ID, DayType.DAILY, LocalTime.of(15, 0), LocalTime.of(16, 0))
        );

        List<ScheduledOrderSlot> slots = calculate(shop(true), OrderMethod.DELIVERY, at(10, 0),
            List.of(hours(LocalTime.of(9, 0), LocalTime.of(22, 0))),
            breakTimes, List.of(), List.of(), List.of());

        List<LocalDateTime> startAts = slots.stream().map(ScheduledOrderSlot::startAt).toList();
        assertThat(startAts).contains(at(14, 30));
        assertThat(startAts).doesNotContain(at(15, 0), at(15, 30));
        // 휴게시간이 끝나는 16:00부터는 다시 예약 가능하다.
        assertThat(startAts).contains(at(16, 0));
    }

    @Test
    @DisplayName("배달 마지막 슬롯은 영업 종료 30분 전 — 22:00 마감이면 21:30이 마지막이다")
    void delivery_lastSlotEndsExactlyAtClosing() {
        List<ScheduledOrderSlot> slots = calculateWithDefaultHours(OrderMethod.DELIVERY, at(10, 0));

        ScheduledOrderSlot last = slots.getLast();
        assertThat(last.startAt()).isEqualTo(at(21, 30));
        assertThat(last.endAt()).isEqualTo(at(22, 0));
    }

    @Test
    @DisplayName("포장 마지막 슬롯도 21:30 — 영업시간이 [open, close) 반열림이라 22:00은 포함되지 않는다")
    void takeout_lastSlotIsBeforeClosing() {
        List<ScheduledOrderSlot> slots = calculateWithDefaultHours(OrderMethod.TAKEOUT, at(10, 0));

        assertThat(slots.getLast().startAt()).isEqualTo(at(21, 30));
    }

    @Test
    @DisplayName("24시간 영업은 주문 시각 +24시간까지 슬롯을 만든다")
    void open24Hours_generatesSlotsUpToTwentyFourHoursAhead() {
        List<ScheduledOrderSlot> slots = calculate(shop(true), OrderMethod.DELIVERY, at(10, 0),
            List.of(hours24()), List.of(), List.of(), List.of(), List.of());

        assertThat(slots.getFirst().startAt()).isEqualTo(at(12, 0));
        // 하한 12:00 ~ 상한 익일 10:00, 30분 그리드 → 45개
        assertThat(slots).hasSize(45);
        assertThat(slots.getLast().startAt()).isEqualTo(at(10, 0).plusDays(1));
    }

    @Test
    @DisplayName("자정 넘김 영업(18:00~02:00)은 익일 새벽까지 슬롯을 만든다")
    void overnightBusinessHours_generatesSlotsIntoNextDay() {
        List<ScheduledOrderSlot> slots = calculate(shop(true), OrderMethod.DELIVERY, at(19, 0),
            List.of(hours(LocalTime.of(18, 0), LocalTime.of(2, 0))),
            List.of(), List.of(), List.of(), List.of());

        assertThat(slots.getFirst().startAt()).isEqualTo(at(21, 0));

        ScheduledOrderSlot last = slots.getLast();
        assertThat(last.startAt()).isEqualTo(at(1, 30).plusDays(1));
        assertThat(last.endAt()).isEqualTo(at(2, 0).plusDays(1));
    }

    @Test
    @DisplayName("정기휴무 요일이면 빈 목록")
    void empty_onRegularClosedDay() {
        List<ShopClosedDay> closedDays = List.of(
            ShopClosedDay.reconstitute(1L, SHOP_ID, ClosedDayType.EVERY_WEEK_MONDAY)
        );

        List<ScheduledOrderSlot> slots = calculate(shop(true), OrderMethod.DELIVERY, at(10, 0),
            List.of(hours(LocalTime.of(9, 0), LocalTime.of(22, 0))),
            List.of(), closedDays, List.of(), List.of());

        assertThat(slots).isEmpty();
    }

    @Test
    @DisplayName("임시휴무 기간이면 빈 목록")
    void empty_duringTemporaryClosure() {
        List<ShopTemporaryClosure> closures = List.of(
            ShopTemporaryClosure.reconstitute(1L, SHOP_ID, MONDAY, MONDAY.plusDays(2), LocalDateTime.now())
        );

        List<ScheduledOrderSlot> slots = calculate(shop(true), OrderMethod.DELIVERY, at(10, 0),
            List.of(hours(LocalTime.of(9, 0), LocalTime.of(22, 0))),
            List.of(), List.of(), closures, List.of());

        assertThat(slots).isEmpty();
    }

    @Test
    @DisplayName("임시중지 구간의 슬롯은 배제되고, 중지가 끝난 이후 슬롯은 유효하다")
    void excludesSlotsDuringSuspensionButKeepsSlotsAfterItEnds() {
        List<ShopSuspension> suspensions = List.of(
            ShopSuspension.reconstitute(
                1L, SHOP_ID, SuspensionReason.SHOP_CIRCUMSTANCE, null,
                at(13, 0), at(15, 0), null, LocalDateTime.now(), LocalDateTime.now()
            )
        );

        List<ScheduledOrderSlot> slots = calculate(shop(true), OrderMethod.DELIVERY, at(10, 0),
            List.of(hours(LocalTime.of(9, 0), LocalTime.of(22, 0))),
            List.of(), List.of(), List.of(), suspensions);

        List<LocalDateTime> startAts = slots.stream().map(ScheduledOrderSlot::startAt).toList();
        assertThat(startAts).contains(at(12, 0), at(12, 30));
        assertThat(startAts).doesNotContain(at(13, 0), at(14, 0), at(14, 30));
        assertThat(startAts).contains(at(15, 0));
    }

    /** 13:00~15:00 배달만 임시중지. */
    private List<ShopSuspension> deliveryOnlySuspension() {
        return List.of(
            ShopSuspension.reconstitute(
                1L, SHOP_ID, SuspensionReason.SHOP_CIRCUMSTANCE, OrderMethod.DELIVERY,
                at(13, 0), at(15, 0), null, LocalDateTime.now(), LocalDateTime.now()
            )
        );
    }

    @Test
    @DisplayName("배달만 임시중지해도 포장 슬롯은 그대로 유지된다 — 결함 A′의 회귀 방어선")
    void deliverySuspension_doesNotRemoveTakeoutSlots() {
        List<ScheduledOrderSlot> slots = calculate(shop(true), OrderMethod.TAKEOUT, at(10, 0),
            List.of(hours(LocalTime.of(9, 0), LocalTime.of(22, 0))),
            List.of(), List.of(), List.of(), deliveryOnlySuspension());

        List<LocalDateTime> startAts = slots.stream().map(ScheduledOrderSlot::startAt).toList();
        assertThat(startAts).contains(at(13, 0), at(14, 0), at(14, 30));
    }

    @Test
    @DisplayName("배달만 임시중지하면 배달 슬롯에서는 중지 구간이 배제된다")
    void deliverySuspension_removesDeliverySlots() {
        List<ScheduledOrderSlot> slots = calculate(shop(true), OrderMethod.DELIVERY, at(10, 0),
            List.of(hours(LocalTime.of(9, 0), LocalTime.of(22, 0))),
            List.of(), List.of(), List.of(), deliveryOnlySuspension());

        List<LocalDateTime> startAts = slots.stream().map(ScheduledOrderSlot::startAt).toList();
        assertThat(startAts).doesNotContain(at(13, 0), at(14, 0), at(14, 30));
        assertThat(startAts).contains(at(12, 0), at(15, 0));
    }

    @Test
    @DisplayName("배달을 배정하지 않은 가게는 배달 슬롯이 빈 목록 — 결함 F의 회귀 방어선")
    void empty_whenOrderMethodNotAssignedToShop() {
        List<ShopOrderMethod> takeoutOnly = List.of(
            ShopOrderMethod.reconstitute(1L, SHOP_ID, OrderMethod.TAKEOUT)
        );

        List<ScheduledOrderSlot> deliverySlots = calculate(shop(true), OrderMethod.DELIVERY, at(10, 0),
            List.of(hours(LocalTime.of(9, 0), LocalTime.of(22, 0))),
            List.of(), List.of(), List.of(), List.of(), takeoutOnly);
        List<ScheduledOrderSlot> takeoutSlots = calculate(shop(true), OrderMethod.TAKEOUT, at(10, 0),
            List.of(hours(LocalTime.of(9, 0), LocalTime.of(22, 0))),
            List.of(), List.of(), List.of(), List.of(), takeoutOnly);

        assertThat(deliverySlots).isEmpty();
        assertThat(takeoutSlots).isNotEmpty();
    }
}
