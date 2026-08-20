package com.tastyhouse.domain.product.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.tastyhouse.domain.product.model.ProductExposureHour;
import com.tastyhouse.domain.product.model.ProductHiddenReason;

/**
 * 메뉴가 지금 손님 메뉴판에 보이는지 판정하는 <b>순수 계산기</b>.
 *
 * <p>리포지토리도 시계도 갖지 않는다 — 판정 근거는 전부 {@link ProductExposureContext}로 받는다.
 *
 * <p><b>노출 = {@code visible} AND 기간 AND 요일·시간대. 품절은 직교한다.</b>
 * 품절 메뉴는 목록에 <b>남고</b> '품절' 뱃지만 붙으므로 이 계산기의 관심사가 아니다 —
 * 스케줄과 같은 축에 두면 시간 밖 메뉴가 '품절'로 표시되는 잘못된 UX가 된다.
 *
 * <p>판정 우선순위(먼저 걸리는 것이 사유가 된다):
 * <ol>
 *   <li>{@code visible = false} → {@code MANUALLY_HIDDEN}. <b>점주의 명시적 의사가 스케줄을 이긴다.</b></li>
 *   <li>기간 시작 전 → {@code BEFORE_EXPOSURE_PERIOD}</li>
 *   <li>기간 종료 후 → {@code AFTER_EXPOSURE_PERIOD} (종료일 <b>당일은 포함</b>)</li>
 *   <li>요일·시간대 밖 → {@code OUT_OF_EXPOSURE_HOURS}</li>
 * </ol>
 *
 * <p><b>시간대 행이 0건이면 "제약 없음"</b>이다 — 그래서 기존 메뉴 전부가 그대로 노출되어 백필이
 * 필요 없다.
 */
public class ProductExposureCalculator {

    /**
     * 지금 이 메뉴가 노출되는지 판정한다.
     */
    public ProductExposureResult calculate(ProductExposureContext context) {
        if (!context.visible()) {
            return ProductExposureResult.ofHidden(ProductHiddenReason.MANUALLY_HIDDEN);
        }

        LocalDate today = context.now().toLocalDate();
        if (context.exposureStartDate() != null && today.isBefore(context.exposureStartDate())) {
            return ProductExposureResult.ofHidden(ProductHiddenReason.BEFORE_EXPOSURE_PERIOD);
        }
        if (context.exposureEndDate() != null && today.isAfter(context.exposureEndDate())) {
            return ProductExposureResult.ofHidden(ProductHiddenReason.AFTER_EXPOSURE_PERIOD);
        }

        if (context.hours().isEmpty()) {
            return ProductExposureResult.ofExposed();
        }
        if (matchesHours(context)) {
            return ProductExposureResult.ofExposed();
        }
        return ProductExposureResult.ofHidden(ProductHiddenReason.OUT_OF_EXPOSURE_HOURS);
    }

    /**
     * 오늘 행과 <b>전일 행의 새벽 연장</b>을 함께 본다.
     *
     * <p>전일 확인을 빠뜨리면 {@code 22:00~02:00}로 설정한 야식 메뉴가 01:00에 사라진다 —
     * 01:00은 오늘 행의 22:00~24:00에도, 오늘 행의 00:00~02:00에도 걸리지 않고
     * <b>어제 행이 넘어온 구간</b>에만 해당하기 때문이다.
     */
    private boolean matchesHours(ProductExposureContext context) {
        DayOfWeek today = context.now().getDayOfWeek();
        LocalTime time = context.now().toLocalTime();

        for (ProductExposureHour hour : context.hours()) {
            if (hour.getDayType().appliesTo(today, context.publicHoliday())
                && coversToday(hour, time)) {
                return true;
            }
        }

        DayOfWeek yesterday = today.minus(1);
        for (ProductExposureHour hour : context.hours()) {
            if (hour.getDayType().appliesTo(yesterday, context.previousDayPublicHoliday())
                && coversAsOvernightTail(hour, time)) {
                return true;
            }
        }
        return false;
    }

    /** 오늘 시작한 구간이 지금 시각을 덮는지. 자정 넘김이면 시작 시각 이후 구간만 여기서 본다. */
    private boolean coversToday(ProductExposureHour hour, LocalTime time) {
        if (hour.isAllDay()) {
            return true;
        }
        if (hour.isOvernight()) {
            return !time.isBefore(hour.getStartTime());
        }
        return !time.isBefore(hour.getStartTime()) && time.isBefore(hour.getEndTime());
    }

    /** 어제 시작해 자정을 넘어온 구간의 <b>새벽 꼬리</b>가 지금 시각을 덮는지. */
    private boolean coversAsOvernightTail(ProductExposureHour hour, LocalTime time) {
        return hour.isOvernight() && time.isBefore(hour.getEndTime());
    }

    /**
     * 여러 메뉴를 한 번에 판정할 때 쓰는 편의 메서드 — 시간대 목록만 메뉴별로 갈아 끼운다.
     */
    public ProductExposureResult calculate(ProductExposureContext base, List<ProductExposureHour> hours) {
        return calculate(ProductExposureContext.of(
            base.visible(),
            base.exposureStartDate(),
            base.exposureEndDate(),
            hours,
            base.now(),
            base.publicHoliday(),
            base.previousDayPublicHoliday()
        ));
    }
}
