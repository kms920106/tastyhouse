package com.tastyhouse.domain.product.model;

import java.time.LocalTime;

import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shared.model.DayType;

/**
 * 메뉴 노출기간의 <b>요일·시간대</b> 축 순수 도메인 모델. 기간 축(시작일·종료일)은
 * {@code Product}가 직접 들고 있다(메뉴당 0..1이라 자식 테이블이 필요 없다).
 *
 * <p><b>행이 0건이면 "요일·시간 제약 없음"</b>으로 해석한다 — 그래서 기존 메뉴 백필이 불필요하다.
 *
 * <p>{@code startTime}·{@code endTime}이 모두 {@code null}이면 그 요일 종일 노출이다.
 * {@code endTime < startTime}이면 <b>자정을 넘긴다</b>(예: 22:00~02:00 야식) — 판정 시
 * 전일 행의 새벽 연장까지 확인해야 01:00에 야식 메뉴가 사라지지 않는다.
 */
public class ProductExposureHour {

    private final Long id;
    private final ProductId productId;
    private final DayType dayType;
    private final LocalTime startTime;
    private final LocalTime endTime;

    private ProductExposureHour(
        Long id,
        ProductId productId,
        DayType dayType,
        LocalTime startTime,
        LocalTime endTime
    ) {
        this.id = id;
        this.productId = productId;
        this.dayType = dayType;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public static ProductExposureHour of(
        ProductId productId,
        DayType dayType,
        LocalTime startTime,
        LocalTime endTime
    ) {
        return new ProductExposureHour(null, productId, dayType, startTime, endTime);
    }

    public static ProductExposureHour reconstitute(
        Long id,
        ProductId productId,
        DayType dayType,
        LocalTime startTime,
        LocalTime endTime
    ) {
        return new ProductExposureHour(id, productId, dayType, startTime, endTime);
    }

    public Long getId() {
        return this.id;
    }

    public ProductId getProductId() {
        return this.productId;
    }

    public DayType getDayType() {
        return this.dayType;
    }

    public LocalTime getStartTime() {
        return this.startTime;
    }

    public LocalTime getEndTime() {
        return this.endTime;
    }

    /** 종일 노출인지 — 시작·종료가 모두 없으면 그 요일 내내 노출한다. */
    public boolean isAllDay() {
        return this.startTime == null || this.endTime == null;
    }

    /** 자정을 넘기는 시간대인지({@code endTime < startTime}). */
    public boolean isOvernight() {
        return !isAllDay() && this.endTime.isBefore(this.startTime);
    }
}
