package com.tastyhouse.domain.shop.service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.region.vo.AdminDongId;
import com.tastyhouse.domain.shared.model.DayType;
import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipHoliday;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipRegion;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipSchedule;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipSetting;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipTier;

/**
 * 고객이 부담할 배달팁을 산출하는 순수 계산기.
 *
 * <p><b>리포지토리 주입 0개, 인스턴스 상태 0개</b>다({@code ShopOperatingStatusCalculator}와 동일 형태).
 * 좌표→거리 변환과 날짜→공휴일 판정은 호출부가 끝내고 {@link ShopDeliveryTipContext}에 이미 해석된
 * 값으로 담아 넘기므로, 이 계산기는 Spring·DB·시계 없이 단위 테스트할 수 있다.
 *
 * <p>최종 배달팁 = <b>구간별 기본 팁 + 추가 배달팁</b>이며, 추가 배달팁의 항목 간 관계는 다음과 같다.
 * <ol>
 *   <li><b>거리별 | 지역별</b> — 상호 배타라 둘 중 하나만 0이 아니다(설정 헤더가 배타성의 소유자).</li>
 *   <li><b>공휴일 &gt; 시간별</b> — 공휴일 팁이 붙으면 시간별은 <b>적용하지 않는다</b>(합산이 아니라 대체).
 *       합산하면 명절 저녁에 같은 성격의 할증이 두 번 붙는다.</li>
 *   <li><b>시간별은 구체성 우선으로 하나만</b> — 개별 요일 &gt; 주말/평일 &gt; 매일. 합산하면
 *       DAILY와 MONDAY를 함께 설정한 점주에게 의도치 않은 이중 부과가 된다.</li>
 * </ol>
 */
public class ShopDeliveryTipCalculator {

    /**
     * 배달팁을 항목별로 산출한다. 배달({@link OrderMethod#DELIVERY})이 아닌 주문 방법은 전액 0이다.
     */
    public ShopDeliveryTipBreakdown calculate(ShopDeliveryTipContext context) {
        if (context == null || context.orderMethod() != OrderMethod.DELIVERY) {
            return ShopDeliveryTipBreakdown.none();
        }

        int baseTipAmount = calculateBaseTip(context.tiers(), context.orderAmountAfterProductDiscount());
        int distanceTipAmount = calculateDistanceTip(context.setting(), context.deliveryDistanceMeters());
        int regionTipAmount = calculateRegionTip(
            context.setting(), context.regionTips(), context.deliveryAdminDongId()
        );
        int holidayTipAmount = calculateHolidayTip(context.publicHoliday(), context.holidayTip());

        // 공휴일 팁이 붙으면 시간별은 대체된다 — 위 클래스 Javadoc의 우선순위 (2) 참고.
        int scheduleTipAmount = holidayTipAmount > 0
            ? 0
            : calculateScheduleTip(context.scheduleTips(), context.orderedAt(), context.publicHoliday());

        return ShopDeliveryTipBreakdown.of(
            baseTipAmount,
            distanceTipAmount,
            regionTipAmount,
            scheduleTipAmount,
            holidayTipAmount
        );
    }

    /**
     * 구간별 기본 배달팁 — 조건을 만족하는 구간 중 하한 주문금액이 <b>가장 큰</b> 것을 고른다.
     *
     * <p>최저 구간에도 미달하는 주문은 <b>최저 구간의 팁</b>(구간 단조성상 가장 비싼 팁)을 적용한다.
     * 거절하지 않는 이유는 최소주문금액을 설정하지 않은 가게에서 주문이 통째로 막히기 때문이다 —
     * 배달팁 설정과 최소주문금액 설정은 서로 독립이다.
     */
    private int calculateBaseTip(List<ShopDeliveryTipTier> tiers, int orderAmount) {
        if (tiers.isEmpty()) {
            return 0;
        }

        return tiers.stream()
            .filter(tier -> tier.covers(orderAmount))
            .max(Comparator.comparingInt(ShopDeliveryTipTier::getMinOrderAmount))
            .or(() -> tiers.stream().min(Comparator.comparingInt(ShopDeliveryTipTier::getMinOrderAmount)))
            .map(ShopDeliveryTipTier::getTipAmount)
            .orElse(0);
    }

    /**
     * 거리별 추가 배달팁 — 거리를 알 수 없으면(주소 미확정 등) 0이다.
     *
     * <p>할증 계산 자체는 설정 헤더가 소유한다({@link ShopDeliveryTipSetting#calculateDistanceSurcharge}).
     */
    private int calculateDistanceTip(ShopDeliveryTipSetting setting, Double distanceMeters) {
        if (setting == null || !setting.usesDistance() || distanceMeters == null) {
            return 0;
        }
        return setting.calculateDistanceSurcharge(distanceMeters);
    }

    /**
     * 지역별 추가 배달팁 — 배달지 행정동과 일치하는 행의 금액. 일치하는 행이 없으면 0이다.
     */
    private int calculateRegionTip(
        ShopDeliveryTipSetting setting,
        List<ShopDeliveryTipRegion> regionTips,
        AdminDongId deliveryAdminDongId
    ) {
        if (setting == null || !setting.usesRegion() || deliveryAdminDongId == null) {
            return 0;
        }

        return regionTips.stream()
            .filter(regionTip -> regionTip.matches(deliveryAdminDongId))
            .findFirst()
            .map(ShopDeliveryTipRegion::getTipAmount)
            .orElse(0);
    }

    /**
     * 공휴일 추가 배달팁 — 공휴일이고 설정이 있을 때만 부과한다.
     *
     * <p>평범한 일요일이 여기 걸리지 않는 것은 캘린더가 일요일 자체를 담지 않기 때문이며
     * ({@code PublicHoliday} Javadoc), 이 메서드에는 요일 분기가 없다.
     */
    private int calculateHolidayTip(boolean publicHoliday, ShopDeliveryTipHoliday holidayTip) {
        if (!publicHoliday || holidayTip == null) {
            return 0;
        }
        return holidayTip.getTipAmount();
    }

    /**
     * 시간별 추가 배달팁 — 적용 가능한 행 중 <b>하나만</b> 골라 그 금액을 쓴다.
     */
    private int calculateScheduleTip(
        List<ShopDeliveryTipSchedule> scheduleTips,
        LocalDateTime orderedAt,
        boolean publicHoliday
    ) {
        if (scheduleTips.isEmpty() || orderedAt == null) {
            return 0;
        }

        return selectApplicableSchedule(scheduleTips, orderedAt, publicHoliday)
            .map(ShopDeliveryTipSchedule::getTipAmount)
            .orElse(0);
    }

    /**
     * 주어진 시각에 적용할 시간별 배달팁 행을 <b>구체성 우선</b>으로 하나 선택한다:
     * 개별 요일 &gt; 주말/평일 &gt; 매일.
     *
     * <p>{@code ShopOperatingStatusCalculator#selectApplicableHour}와 같은 규칙이어야 같은 도메인에서
     * 요일 해석 규칙이 두 벌이 되지 않는다. 다만 {@code HOLIDAY} 분기는 없다 — 시간별 배달팁은
     * {@code HOLIDAY} 요일 구분을 애초에 저장할 수 없고(공휴일은 전용 애그리거트 담당),
     * 공휴일 팁이 붙는 경우 이 메서드는 호출되지도 않는다.
     */
    private Optional<ShopDeliveryTipSchedule> selectApplicableSchedule(
        List<ShopDeliveryTipSchedule> scheduleTips,
        LocalDateTime orderedAt,
        boolean publicHoliday
    ) {
        LocalTime time = orderedAt.toLocalTime();
        DayOfWeek dayOfWeek = orderedAt.getDayOfWeek();

        ShopDeliveryTipSchedule daily = null;
        ShopDeliveryTipSchedule weekGroup = null;

        for (ShopDeliveryTipSchedule scheduleTip : scheduleTips) {
            if (!scheduleTip.covers(time, dayOfWeek, publicHoliday)) {
                continue;
            }

            DayType dayType = scheduleTip.getDayType();
            if (dayType.isSpecificDay(dayOfWeek)) {
                return Optional.of(scheduleTip);
            }
            if (dayType == DayType.WEEKEND || dayType == DayType.WEEKDAY) {
                weekGroup = scheduleTip;
            } else if (dayType == DayType.DAILY) {
                daily = scheduleTip;
            }
        }

        if (weekGroup != null) {
            return Optional.of(weekGroup);
        }
        return Optional.ofNullable(daily);
    }
}
