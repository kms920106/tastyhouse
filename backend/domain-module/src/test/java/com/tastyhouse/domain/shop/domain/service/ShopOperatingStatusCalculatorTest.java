package com.tastyhouse.domain.shop.domain.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import com.tastyhouse.domain.shop.service.ShopOperatingStatusCalculator;
import com.tastyhouse.domain.shop.service.ShopOperatingStatusContext;
import com.tastyhouse.domain.shop.service.ShopOperatingStatusResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.shop.model.ClosedDayType;
import com.tastyhouse.domain.shop.model.DayType;
import com.tastyhouse.domain.shop.model.OrderMethod;
import com.tastyhouse.domain.shop.model.OrderUnavailableReason;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.model.ShopBreakTime;
import com.tastyhouse.domain.shop.model.ShopBusinessHour;
import com.tastyhouse.domain.shop.model.ShopClosedDay;
import com.tastyhouse.domain.shop.model.ShopOperatingStatus;
import com.tastyhouse.domain.shop.model.ShopSuspension;
import com.tastyhouse.domain.shop.model.ShopTemporaryClosure;
import com.tastyhouse.domain.shop.model.SuspensionReason;

import static org.assertj.core.api.Assertions.assertThat;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.shop.vo.StationId;

/**
 * 영업 상태 계산기 순수 단위 테스트. Spring/JPA 컨텍스트 없이 우선순위·경계 케이스를 검증한다.
 *
 * <p>주문유형별 판정(유형별 임시중지가 가게 전체를 멈추지 않는다)의 회귀 방어선도 함께 담는다.
 */
class ShopOperatingStatusCalculatorTest {

    private final ShopOperatingStatusCalculator calculator = new ShopOperatingStatusCalculator();

    // 2026-07-27은 월요일
    private static final LocalDateTime MONDAY_NOON = LocalDateTime.of(2026, 7, 27, 12, 0);

    private Shop shop() {
        return shop(false, false);
    }

    private Shop hiddenShop() {
        return shop(true, false);
    }

    private Shop permanentlyClosedShop() {
        return shop(false, true);
    }

    private Shop shop(boolean hidden, boolean permanentlyClosed) {
        return Shop.reconstitute(
            1L, null, StationId.of(1L), "가게", BigDecimal.valueOf(37.5), BigDecimal.valueOf(127.0),
            4.5, "도로명", "지번", "02-000-0000", null, null,
            permanentlyClosed, hidden, false, 0, false, LocalDateTime.now(), LocalDateTime.now()
        );
    }

    /** 매일 09:00 ~ 22:00 영업. */
    private List<ShopBusinessHour> dailyBusinessHours() {
        return List.of(
            ShopBusinessHour.reconstitute(1L, ShopId.of(1L), DayType.DAILY, LocalTime.of(9, 0), LocalTime.of(22, 0), false, false)
        );
    }

    /** MONDAY_NOON을 포함하는 활성 임시중지. */
    private ShopSuspension activeSuspension(OrderMethod orderMethod) {
        return ShopSuspension.reconstitute(
            1L, ShopId.of(1L), SuspensionReason.SHOP_CIRCUMSTANCE, orderMethod,
            MONDAY_NOON.minusHours(1), MONDAY_NOON.plusHours(1), null, null, null
        );
    }

    private ShopOperatingStatusResult calculate(
        Shop shop,
        List<ShopBusinessHour> businessHours,
        List<ShopBreakTime> breakTimes,
        List<ShopClosedDay> closedDays,
        List<ShopTemporaryClosure> temporaryClosures,
        List<ShopSuspension> suspensions,
        OrderMethod orderMethod,
        LocalDateTime now
    ) {
        return calculator.calculate(ShopOperatingStatusContext.of(
            shop, businessHours, breakTimes, closedDays, temporaryClosures, suspensions, orderMethod, false, now
        ));
    }

    /** 영업시간만 주어진 기본 형태 — 가게 전체 판정. */
    private ShopOperatingStatusResult calculateShopWide(Shop shop, List<ShopBusinessHour> hours, LocalDateTime now) {
        return calculate(shop, hours, List.of(), List.of(), List.of(), List.of(), null, now);
    }

    @Test
    @DisplayName("영업시간 내이고 휴무·중지 없으면 영업중이고 사유가 없다")
    void open_withinBusinessHours() {
        ShopOperatingStatusResult result = calculateShopWide(shop(), dailyBusinessHours(), MONDAY_NOON);

        assertThat(result.status()).isEqualTo(ShopOperatingStatus.OPEN);
        assertThat(result.isOpen()).isTrue();
        assertThat(result.unavailableReason()).isNull();
    }

    @Test
    @DisplayName("영업시간 밖이면 준비중 + OUT_OF_BUSINESS_HOURS")
    void preparing_outsideBusinessHours() {
        List<ShopBusinessHour> hours = List.of(
            ShopBusinessHour.reconstitute(1L, ShopId.of(1L), DayType.DAILY, LocalTime.of(9, 0), LocalTime.of(11, 0), false, false)
        );

        ShopOperatingStatusResult result = calculateShopWide(shop(), hours, MONDAY_NOON);

        assertThat(result.status()).isEqualTo(ShopOperatingStatus.PREPARING);
        assertThat(result.unavailableReason()).isEqualTo(OrderUnavailableReason.OUT_OF_BUSINESS_HOURS);
    }

    @Test
    @DisplayName("24시간 영업이면 시간과 무관하게 영업중")
    void open_24hours() {
        List<ShopBusinessHour> hours = List.of(
            ShopBusinessHour.reconstitute(1L, ShopId.of(1L), DayType.DAILY, null, null, false, true)
        );

        ShopOperatingStatusResult result =
            calculateShopWide(shop(), hours, LocalDateTime.of(2026, 7, 27, 3, 0));

        assertThat(result.isOpen()).isTrue();
    }

    @Test
    @DisplayName("전일 영업시간이 자정을 넘겨 새벽까지 이어지면 영업중")
    void open_crossMidnightFromYesterday() {
        // 매일 20:00 ~ 02:00 (자정 넘김) → 월요일 01:00은 일요일 영업의 연장
        List<ShopBusinessHour> hours = List.of(
            ShopBusinessHour.reconstitute(1L, ShopId.of(1L), DayType.DAILY, LocalTime.of(20, 0), LocalTime.of(2, 0), false, false)
        );

        ShopOperatingStatusResult result =
            calculateShopWide(shop(), hours, LocalDateTime.of(2026, 7, 27, 1, 0));

        assertThat(result.isOpen()).isTrue();
    }

    @Test
    @DisplayName("전체 대상 임시중지는 영업시간 내여도 준비중 + SUSPENDED")
    void preparing_activeSuspension() {
        ShopOperatingStatusResult result = calculate(
            shop(), dailyBusinessHours(), List.of(), List.of(), List.of(),
            List.of(activeSuspension(null)), null, MONDAY_NOON
        );

        assertThat(result.status()).isEqualTo(ShopOperatingStatus.PREPARING);
        assertThat(result.unavailableReason()).isEqualTo(OrderUnavailableReason.SUSPENDED);
    }

    @Test
    @DisplayName("전체 대상 임시중지는 특정 주문유형 판정에서도 준비중")
    void preparing_shopWideSuspension_forSpecificOrderMethod() {
        ShopOperatingStatusResult result = calculate(
            shop(), dailyBusinessHours(), List.of(), List.of(), List.of(),
            List.of(activeSuspension(null)), OrderMethod.TAKEOUT, MONDAY_NOON
        );

        assertThat(result.unavailableReason()).isEqualTo(OrderUnavailableReason.SUSPENDED);
    }

    @Test
    @DisplayName("배달만 임시중지한 가게의 전체 상태는 영업중 — 결함 A의 회귀 방어선")
    void open_shopWide_whenOnlyDeliverySuspended() {
        ShopOperatingStatusResult result = calculate(
            shop(), dailyBusinessHours(), List.of(), List.of(), List.of(),
            List.of(activeSuspension(OrderMethod.DELIVERY)), null, MONDAY_NOON
        );

        assertThat(result.status()).isEqualTo(ShopOperatingStatus.OPEN);
        assertThat(result.unavailableReason()).isNull();
    }

    @Test
    @DisplayName("배달만 임시중지하면 배달 유형만 준비중이고 포장은 영업중")
    void deliverySuspension_affectsOnlyDelivery() {
        List<ShopSuspension> suspensions = List.of(activeSuspension(OrderMethod.DELIVERY));

        ShopOperatingStatusResult delivery = calculate(
            shop(), dailyBusinessHours(), List.of(), List.of(), List.of(), suspensions, OrderMethod.DELIVERY, MONDAY_NOON
        );
        ShopOperatingStatusResult takeout = calculate(
            shop(), dailyBusinessHours(), List.of(), List.of(), List.of(), suspensions, OrderMethod.TAKEOUT, MONDAY_NOON
        );

        assertThat(delivery.status()).isEqualTo(ShopOperatingStatus.PREPARING);
        assertThat(delivery.unavailableReason()).isEqualTo(OrderUnavailableReason.SUSPENDED);
        assertThat(takeout.isOpen()).isTrue();
    }

    @Test
    @DisplayName("임시휴무 기간 내면 준비중 + TEMPORARILY_CLOSED")
    void preparing_temporaryClosure() {
        List<ShopTemporaryClosure> closures = List.of(
            ShopTemporaryClosure.reconstitute(1L, ShopId.of(1L), LocalDate.of(2026, 7, 26), LocalDate.of(2026, 7, 28), null)
        );

        ShopOperatingStatusResult result = calculate(
            shop(), dailyBusinessHours(), List.of(), List.of(), closures, List.of(), null, MONDAY_NOON
        );

        assertThat(result.unavailableReason()).isEqualTo(OrderUnavailableReason.TEMPORARILY_CLOSED);
    }

    @Test
    @DisplayName("매주 월요일 정기휴무면 월요일에 준비중 + REGULAR_CLOSED_DAY")
    void preparing_everyMondayClosed() {
        List<ShopClosedDay> closedDays = List.of(
            ShopClosedDay.reconstitute(1L, ShopId.of(1L), ClosedDayType.EVERY_WEEK_MONDAY)
        );

        ShopOperatingStatusResult result = calculate(
            shop(), dailyBusinessHours(), List.of(), closedDays, List.of(), List.of(), null, MONDAY_NOON
        );

        assertThat(result.unavailableReason()).isEqualTo(OrderUnavailableReason.REGULAR_CLOSED_DAY);
    }

    @Test
    @DisplayName("매달 넷째 주 월요일 정기휴무는 4째 주 월요일에만 준비중")
    void fourthWeekMonday() {
        List<ShopClosedDay> closedDays = List.of(
            ShopClosedDay.reconstitute(1L, ShopId.of(1L), ClosedDayType.EVERY_MONTH_FOURTH_WEEK_MONDAY)
        );

        // 2026-07-27은 넷째 주 월요일((27-1)/7+1 = 4) → 준비중
        assertThat(calculate(shop(), dailyBusinessHours(), List.of(), closedDays, List.of(), List.of(), null,
            LocalDateTime.of(2026, 7, 27, 12, 0)).status()).isEqualTo(ShopOperatingStatus.PREPARING);

        // 2026-07-06은 첫째 주 월요일 → 영업중
        assertThat(calculate(shop(), dailyBusinessHours(), List.of(), closedDays, List.of(), List.of(), null,
            LocalDateTime.of(2026, 7, 6, 12, 0)).status()).isEqualTo(ShopOperatingStatus.OPEN);
    }

    @Test
    @DisplayName("휴게시간 구간 내면 준비중 + BREAK_TIME")
    void preparing_withinBreakTime() {
        List<ShopBreakTime> breakTimes = List.of(
            ShopBreakTime.reconstitute(1L, ShopId.of(1L), DayType.DAILY, LocalTime.of(11, 30), LocalTime.of(13, 0))
        );

        ShopOperatingStatusResult result = calculate(
            shop(), dailyBusinessHours(), breakTimes, List.of(), List.of(), List.of(), null, MONDAY_NOON
        );

        assertThat(result.unavailableReason()).isEqualTo(OrderUnavailableReason.BREAK_TIME);
    }

    @Test
    @DisplayName("노출정지 가게는 방어적으로 준비중 + HIDDEN")
    void preparing_hiddenShop() {
        ShopOperatingStatusResult result = calculateShopWide(hiddenShop(), dailyBusinessHours(), MONDAY_NOON);

        assertThat(result.status()).isEqualTo(ShopOperatingStatus.PREPARING);
        assertThat(result.unavailableReason()).isEqualTo(OrderUnavailableReason.HIDDEN);
    }

    @Test
    @DisplayName("폐업 가게는 준비중 + PERMANENTLY_CLOSED")
    void preparing_permanentlyClosedShop() {
        ShopOperatingStatusResult result =
            calculateShopWide(permanentlyClosedShop(), dailyBusinessHours(), MONDAY_NOON);

        assertThat(result.unavailableReason()).isEqualTo(OrderUnavailableReason.PERMANENTLY_CLOSED);
    }

    @Test
    @DisplayName("폐업과 영업시간 밖이 동시면 먼저 걸린 PERMANENTLY_CLOSED가 사유다")
    void reasonPriority_permanentlyClosedWinsOverBusinessHours() {
        List<ShopBusinessHour> hours = List.of(
            ShopBusinessHour.reconstitute(1L, ShopId.of(1L), DayType.DAILY, LocalTime.of(9, 0), LocalTime.of(11, 0), false, false)
        );

        ShopOperatingStatusResult result = calculateShopWide(permanentlyClosedShop(), hours, MONDAY_NOON);

        assertThat(result.unavailableReason()).isEqualTo(OrderUnavailableReason.PERMANENTLY_CLOSED);
    }

    @Test
    @DisplayName("영업시간 정보가 없으면 영업중으로 간주")
    void open_whenNoBusinessHours() {
        ShopOperatingStatusResult result = calculateShopWide(shop(), List.of(), MONDAY_NOON);

        assertThat(result.isOpen()).isTrue();
    }
}
