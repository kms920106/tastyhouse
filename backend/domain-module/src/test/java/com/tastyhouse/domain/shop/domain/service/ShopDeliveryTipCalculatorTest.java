package com.tastyhouse.domain.shop.domain.service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.region.vo.AdminDongId;
import com.tastyhouse.domain.shop.model.DayType;
import com.tastyhouse.domain.shop.model.DeliveryTipDistanceUnit;
import com.tastyhouse.domain.shop.model.OrderMethod;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipHoliday;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipRegion;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipSchedule;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipSetting;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipTier;
import com.tastyhouse.domain.shop.service.ShopDeliveryTipBreakdown;
import com.tastyhouse.domain.shop.service.ShopDeliveryTipCalculator;
import com.tastyhouse.domain.shop.service.ShopDeliveryTipContext;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 배달팁 산출 순수 계산기 단위 테스트.
 *
 * <p>리포지토리·시계 주입이 0개라 fake가 필요 없다 — 거리·행정동·공휴일 여부를 이미 해석된 값으로 담은
 * {@code ShopDeliveryTipContext}를 직접 조립해 넣는다.
 */
class ShopDeliveryTipCalculatorTest {

    private static final ShopId SHOP_ID = ShopId.of(1L);
    private static final AdminDongId DONG_A = AdminDongId.of(100L);
    private static final AdminDongId DONG_B = AdminDongId.of(200L);

    /** 2026-08-03은 월요일이다. 요일 의존 케이스는 전부 이 날짜를 기준으로 삼는다. */
    private static final LocalDateTime MONDAY_19H = LocalDateTime.of(2026, 8, 3, 19, 0);

    private final ShopDeliveryTipCalculator calculator = new ShopDeliveryTipCalculator();

    @Nested
    @DisplayName("orderMethod")
    class OrderMethodGate {

        @Test
        @DisplayName("배달이 아닌 주문 방법은 모든 항목이 0원이다")
        void calculate_zeroForNonDelivery() {
            ShopDeliveryTipBreakdown breakdown = calculator.calculate(context(
                OrderMethod.TABLE, 20000, 3000.0, DONG_A, MONDAY_19H, true,
                distanceSetting(1000, DeliveryTipDistanceUnit.PER_500M, 500),
                List.of(tier(0, 0, 2000)),
                List.of(),
                List.of(scheduleTip(DayType.DAILY, LocalTime.of(18, 0), LocalTime.of(21, 0), 1000)),
                ShopDeliveryTipHoliday.of(SHOP_ID, 1500)
            ));

            assertThat(breakdown.totalTipAmount()).isZero();
            assertThat(breakdown.baseTipAmount()).isZero();
            assertThat(breakdown.distanceTipAmount()).isZero();
            assertThat(breakdown.scheduleTipAmount()).isZero();
            assertThat(breakdown.holidayTipAmount()).isZero();
        }

        @Test
        @DisplayName("context 자체가 null이면 전액 0원이다")
        void calculate_zeroForNullContext() {
            assertThat(calculator.calculate(null).totalTipAmount()).isZero();
        }
    }

    @Nested
    @DisplayName("구간별 기본 배달팁")
    class BaseTip {

        private final List<ShopDeliveryTipTier> tiers = List.of(
            tier(0, 5000, 2000),
            tier(1, 10000, 1500),
            tier(2, 15000, 1000)
        );

        @Test
        @DisplayName("조건을 만족하는 구간 중 하한 주문금액이 가장 큰 것을 고른다")
        void calculate_picksHighestCoveringTier() {
            assertThat(baseTipFor(tiers, 20000)).isEqualTo(1000);
            assertThat(baseTipFor(tiers, 12000)).isEqualTo(1500);
            assertThat(baseTipFor(tiers, 7000)).isEqualTo(2000);
        }

        @Test
        @DisplayName("구간 하한과 정확히 같은 주문금액은 그 구간을 적용한다(하한 포함)")
        void calculate_boundaryUsesThatTier() {
            assertThat(baseTipFor(tiers, 5000)).isEqualTo(2000);
            assertThat(baseTipFor(tiers, 10000)).isEqualTo(1500);
            assertThat(baseTipFor(tiers, 15000)).isEqualTo(1000);
        }

        @Test
        @DisplayName("최저 구간에도 미달하는 주문은 최저 구간의 팁(가장 비싼 팁)을 적용한다 — 거절하지 않는다")
        void calculate_belowLowestTierUsesLowestTierTip() {
            assertThat(baseTipFor(tiers, 4999)).isEqualTo(2000);
            assertThat(baseTipFor(tiers, 0)).isEqualTo(2000);
        }

        @Test
        @DisplayName("구간이 하나도 없으면 0원이다")
        void calculate_zeroWhenNoTiers() {
            assertThat(baseTipFor(List.of(), 20000)).isZero();
        }

        private int baseTipFor(List<ShopDeliveryTipTier> tiers, int orderAmount) {
            return calculator.calculate(deliveryContext(
                orderAmount, null, null, MONDAY_19H, false, null, tiers, List.of(), List.of(), null
            )).baseTipAmount();
        }
    }

    @Nested
    @DisplayName("거리별 추가 배달팁")
    class DistanceTip {

        @Test
        @DisplayName("설정 헤더의 할증 계산에 위임한다 — 기본배달거리 초과분을 단위로 올림한다")
        void calculate_delegatesToSetting() {
            ShopDeliveryTipBreakdown breakdown = calculator.calculate(deliveryContext(
                20000, 3000.0, null, MONDAY_19H, false,
                distanceSetting(1500, DeliveryTipDistanceUnit.PER_500M, 500),
                List.of(), List.of(), List.of(), null
            ));

            assertThat(breakdown.distanceTipAmount()).isEqualTo(1500);
        }

        @Test
        @DisplayName("거리를 알 수 없으면(주소 미확정) 0원이다")
        void calculate_zeroWhenDistanceUnknown() {
            ShopDeliveryTipBreakdown breakdown = calculator.calculate(deliveryContext(
                20000, null, null, MONDAY_19H, false,
                distanceSetting(1500, DeliveryTipDistanceUnit.PER_500M, 500),
                List.of(), List.of(), List.of(), null
            ));

            assertThat(breakdown.distanceTipAmount()).isZero();
        }
    }

    @Nested
    @DisplayName("지역별 추가 배달팁")
    class RegionTip {

        @Test
        @DisplayName("배달지 행정동과 일치하는 행의 금액을 부과한다")
        void calculate_matchesDeliveryAdminDong() {
            ShopDeliveryTipBreakdown breakdown = calculator.calculate(deliveryContext(
                20000, null, DONG_A, MONDAY_19H, false,
                regionSetting(),
                List.of(),
                List.of(regionTip(DONG_A, 800), regionTip(DONG_B, 1200)),
                List.of(), null
            ));

            assertThat(breakdown.regionTipAmount()).isEqualTo(800);
        }

        @Test
        @DisplayName("일치하는 행정동이 없으면 0원이다")
        void calculate_zeroWhenNoRegionMatch() {
            ShopDeliveryTipBreakdown breakdown = calculator.calculate(deliveryContext(
                20000, null, AdminDongId.of(999L), MONDAY_19H, false,
                regionSetting(),
                List.of(),
                List.of(regionTip(DONG_A, 800)),
                List.of(), null
            ));

            assertThat(breakdown.regionTipAmount()).isZero();
        }

        @Test
        @DisplayName("배달지 행정동이 null이면 0원이다")
        void calculate_zeroWhenAdminDongUnknown() {
            ShopDeliveryTipBreakdown breakdown = calculator.calculate(deliveryContext(
                20000, null, null, MONDAY_19H, false,
                regionSetting(),
                List.of(),
                List.of(regionTip(DONG_A, 800)),
                List.of(), null
            ));

            assertThat(breakdown.regionTipAmount()).isZero();
        }
    }

    @Nested
    @DisplayName("공휴일 · 시간별 우선순위")
    class HolidayAndSchedule {

        @Test
        @DisplayName("공휴일 팁이 붙으면 시간별은 합산이 아니라 대체된다(시간별 0원)")
        void calculate_holidayReplacesSchedule() {
            ShopDeliveryTipBreakdown breakdown = calculator.calculate(deliveryContext(
                20000, null, null, MONDAY_19H, true, null, List.of(), List.of(),
                List.of(scheduleTip(DayType.DAILY, LocalTime.of(18, 0), LocalTime.of(21, 0), 1000)),
                ShopDeliveryTipHoliday.of(SHOP_ID, 1500)
            ));

            assertThat(breakdown.holidayTipAmount()).isEqualTo(1500);
            assertThat(breakdown.scheduleTipAmount()).isZero();
            assertThat(breakdown.totalTipAmount()).isEqualTo(1500);
        }

        @Test
        @DisplayName("공휴일이 아니면 공휴일 팁이 설정돼 있어도 시간별이 그대로 적용된다")
        void calculate_scheduleAppliesWhenNotHoliday() {
            ShopDeliveryTipBreakdown breakdown = calculator.calculate(deliveryContext(
                20000, null, null, MONDAY_19H, false, null, List.of(), List.of(),
                List.of(scheduleTip(DayType.DAILY, LocalTime.of(18, 0), LocalTime.of(21, 0), 1000)),
                ShopDeliveryTipHoliday.of(SHOP_ID, 1500)
            ));

            assertThat(breakdown.holidayTipAmount()).isZero();
            assertThat(breakdown.scheduleTipAmount()).isEqualTo(1000);
        }

        @Test
        @DisplayName("공휴일이지만 공휴일 팁 설정이 없으면 시간별이 적용된다")
        void calculate_scheduleAppliesWhenNoHolidayTipConfigured() {
            ShopDeliveryTipBreakdown breakdown = calculator.calculate(deliveryContext(
                20000, null, null, MONDAY_19H, true, null, List.of(), List.of(),
                List.of(scheduleTip(DayType.DAILY, LocalTime.of(18, 0), LocalTime.of(21, 0), 1000)),
                null
            ));

            assertThat(breakdown.holidayTipAmount()).isZero();
            assertThat(breakdown.scheduleTipAmount()).isEqualTo(1000);
        }
    }

    @Nested
    @DisplayName("시간별 구체성 우선 선택")
    class ScheduleSpecificity {

        @Test
        @DisplayName("DAILY와 MONDAY가 모두 걸리는 월요일에는 MONDAY 하나만 적용한다(합산 아님)")
        void calculate_specificDayWinsOverDaily() {
            ShopDeliveryTipBreakdown breakdown = calculator.calculate(deliveryContext(
                20000, null, null, MONDAY_19H, false, null, List.of(), List.of(),
                List.of(
                    scheduleTip(DayType.DAILY, LocalTime.of(18, 0), LocalTime.of(21, 0), 1000),
                    scheduleTip(DayType.MONDAY, LocalTime.of(18, 0), LocalTime.of(21, 0), 2500)
                ),
                null
            ));

            assertThat(breakdown.scheduleTipAmount()).isEqualTo(2500);
            assertThat(breakdown.totalTipAmount()).isEqualTo(2500);
        }

        @Test
        @DisplayName("WEEKDAY와 DAILY가 모두 걸리면 더 구체적인 WEEKDAY를 적용한다")
        void calculate_weekGroupWinsOverDaily() {
            ShopDeliveryTipBreakdown breakdown = calculator.calculate(deliveryContext(
                20000, null, null, MONDAY_19H, false, null, List.of(), List.of(),
                List.of(
                    scheduleTip(DayType.DAILY, LocalTime.of(18, 0), LocalTime.of(21, 0), 1000),
                    scheduleTip(DayType.WEEKDAY, LocalTime.of(18, 0), LocalTime.of(21, 0), 1800)
                ),
                null
            ));

            assertThat(breakdown.scheduleTipAmount()).isEqualTo(1800);
        }

        @Test
        @DisplayName("적용 가능한 시간대가 없으면 0원이다")
        void calculate_zeroWhenNoScheduleCovers() {
            ShopDeliveryTipBreakdown breakdown = calculator.calculate(deliveryContext(
                20000, null, null, LocalDateTime.of(2026, 8, 3, 10, 0), false, null, List.of(), List.of(),
                List.of(scheduleTip(DayType.DAILY, LocalTime.of(18, 0), LocalTime.of(21, 0), 1000)),
                null
            ));

            assertThat(breakdown.scheduleTipAmount()).isZero();
        }

        @Test
        @DisplayName("자정 넘김 시간대는 자정 이후 시각에도 적용된다")
        void calculate_overnightSchedule() {
            ShopDeliveryTipSchedule overnight = scheduleTip(DayType.DAILY, LocalTime.of(22, 0), LocalTime.of(2, 0), 1200);

            ShopDeliveryTipBreakdown afterMidnight = calculator.calculate(deliveryContext(
                20000, null, null, LocalDateTime.of(2026, 8, 4, 1, 0), false,
                null, List.of(), List.of(), List.of(overnight), null
            ));
            ShopDeliveryTipBreakdown beforeMidnight = calculator.calculate(deliveryContext(
                20000, null, null, LocalDateTime.of(2026, 8, 3, 23, 0), false,
                null, List.of(), List.of(), List.of(overnight), null
            ));
            ShopDeliveryTipBreakdown outside = calculator.calculate(deliveryContext(
                20000, null, null, LocalDateTime.of(2026, 8, 4, 3, 0), false,
                null, List.of(), List.of(), List.of(overnight), null
            ));

            assertThat(afterMidnight.scheduleTipAmount()).isEqualTo(1200);
            assertThat(beforeMidnight.scheduleTipAmount()).isEqualTo(1200);
            assertThat(outside.scheduleTipAmount()).isZero();
        }
    }

    @Nested
    @DisplayName("breakdown 합산")
    class Breakdown {

        @Test
        @DisplayName("항목별 값이 각각 기록되고 총액은 항목 합과 일치한다")
        void calculate_totalEqualsSumOfItems() {
            ShopDeliveryTipBreakdown breakdown = calculator.calculate(deliveryContext(
                12000, 3000.0, null, MONDAY_19H, false,
                distanceSetting(1500, DeliveryTipDistanceUnit.PER_500M, 500),
                List.of(tier(0, 5000, 2000), tier(1, 10000, 1500)),
                List.of(),
                List.of(scheduleTip(DayType.MONDAY, LocalTime.of(18, 0), LocalTime.of(21, 0), 700)),
                null
            ));

            assertThat(breakdown.baseTipAmount()).isEqualTo(1500);
            assertThat(breakdown.distanceTipAmount()).isEqualTo(1500);
            assertThat(breakdown.regionTipAmount()).isZero();
            assertThat(breakdown.scheduleTipAmount()).isEqualTo(700);
            assertThat(breakdown.holidayTipAmount()).isZero();
            assertThat(breakdown.totalTipAmount()).isEqualTo(1500 + 1500 + 0 + 700 + 0);
        }
    }

    private static ShopDeliveryTipContext deliveryContext(
        int orderAmountAfterProductDiscount,
        Double deliveryDistanceMeters,
        AdminDongId deliveryAdminDongId,
        LocalDateTime orderedAt,
        boolean publicHoliday,
        ShopDeliveryTipSetting setting,
        List<ShopDeliveryTipTier> tiers,
        List<ShopDeliveryTipRegion> regionTips,
        List<ShopDeliveryTipSchedule> scheduleTips,
        ShopDeliveryTipHoliday holidayTip
    ) {
        return context(
            OrderMethod.DELIVERY, orderAmountAfterProductDiscount, deliveryDistanceMeters, deliveryAdminDongId,
            orderedAt, publicHoliday, setting, tiers, regionTips, scheduleTips, holidayTip
        );
    }

    private static ShopDeliveryTipContext context(
        OrderMethod orderMethod,
        int orderAmountAfterProductDiscount,
        Double deliveryDistanceMeters,
        AdminDongId deliveryAdminDongId,
        LocalDateTime orderedAt,
        boolean publicHoliday,
        ShopDeliveryTipSetting setting,
        List<ShopDeliveryTipTier> tiers,
        List<ShopDeliveryTipRegion> regionTips,
        List<ShopDeliveryTipSchedule> scheduleTips,
        ShopDeliveryTipHoliday holidayTip
    ) {
        return ShopDeliveryTipContext.of(
            orderMethod, orderAmountAfterProductDiscount, deliveryDistanceMeters, deliveryAdminDongId,
            orderedAt, publicHoliday, setting, tiers, regionTips, scheduleTips, holidayTip
        );
    }

    private static ShopDeliveryTipTier tier(int tierOrder, int minOrderAmount, int tipAmount) {
        return ShopDeliveryTipTier.of(SHOP_ID, tierOrder, minOrderAmount, tipAmount);
    }

    private static ShopDeliveryTipRegion regionTip(AdminDongId adminDongId, int tipAmount) {
        return ShopDeliveryTipRegion.of(SHOP_ID, adminDongId, tipAmount);
    }

    private static ShopDeliveryTipSchedule scheduleTip(DayType dayType, LocalTime startTime, LocalTime endTime, int tipAmount) {
        return ShopDeliveryTipSchedule.of(SHOP_ID, dayType, startTime, endTime, tipAmount);
    }

    private static ShopDeliveryTipSetting distanceSetting(
        int baseDistanceMeters,
        DeliveryTipDistanceUnit unit,
        int surchargeAmount
    ) {
        ShopDeliveryTipSetting setting = ShopDeliveryTipSetting.of(SHOP_ID);
        setting.changeToDistance(baseDistanceMeters, unit, surchargeAmount);
        return setting;
    }

    private static ShopDeliveryTipSetting regionSetting() {
        ShopDeliveryTipSetting setting = ShopDeliveryTipSetting.of(SHOP_ID);
        setting.changeToRegion();
        return setting;
    }
}
