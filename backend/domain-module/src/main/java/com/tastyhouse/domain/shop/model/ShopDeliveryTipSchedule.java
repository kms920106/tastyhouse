package com.tastyhouse.domain.shop.model;

import java.time.DayOfWeek;
import java.time.LocalTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.shared.model.DayType;

/**
 * 시간별 추가 배달팁 순수 도메인 모델 (요일 구분 + 시간대).
 *
 * <p>요일 체계는 {@link DayType}을 재사용한다 — 다만 <b>{@link DayType#HOLIDAY}는 금지</b>한다.
 * 공휴일은 전용 애그리거트({@link ShopDeliveryTipHoliday})가 담당하므로 두 경로가 겹치면 같은 금액이
 * 두 번 붙는다.
 *
 * <p>시간 포함 판정({@link #covers})은 {@code ShopBreakTime#covers}와 동형이다 — 같은 도메인에서
 * 자정 넘김 해석이 두 벌이 되지 않도록 반열림 구간 규칙을 그대로 따른다.
 */
public class ShopDeliveryTipSchedule {

    private final Long id;
    private final ShopId shopId;
    private DayType dayType;
    private LocalTime startTime;
    private LocalTime endTime;
    private int tipAmount;

    private ShopDeliveryTipSchedule(
        Long id,
        ShopId shopId,
        DayType dayType,
        LocalTime startTime,
        LocalTime endTime,
        int tipAmount
    ) {
        this.id = id;
        this.shopId = shopId;
        this.dayType = dayType;
        this.startTime = startTime;
        this.endTime = endTime;
        this.tipAmount = tipAmount;
    }

    /**
     * 신규 시간별 배달팁을 생성한다.
     *
     * <p>금액은 {@code 0 ~ 10,000원}, 시작·종료 시각은 필수이며 서로 같을 수 없고
     * ({@code start == end}는 길이 0 또는 24시간으로 해석이 갈린다), 요일 구분은
     * {@link DayType#HOLIDAY}일 수 없다.
     */
    public static ShopDeliveryTipSchedule of(
        ShopId shopId,
        DayType dayType,
        LocalTime startTime,
        LocalTime endTime,
        int tipAmount
    ) {
        validateSchedule(dayType, startTime, endTime, tipAmount);

        return new ShopDeliveryTipSchedule(null, shopId, dayType, startTime, endTime, tipAmount);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이며 검증하지 않는다.
     */
    public static ShopDeliveryTipSchedule reconstitute(
        Long id,
        ShopId shopId,
        DayType dayType,
        LocalTime startTime,
        LocalTime endTime,
        int tipAmount
    ) {
        return new ShopDeliveryTipSchedule(id, shopId, dayType, startTime, endTime, tipAmount);
    }

    /** 시간대·금액을 변경한다 — 생성과 같은 검증 한 벌을 강제한다. */
    public void update(DayType dayType, LocalTime startTime, LocalTime endTime, int tipAmount) {
        validateSchedule(dayType, startTime, endTime, tipAmount);

        this.dayType = dayType;
        this.startTime = startTime;
        this.endTime = endTime;
        this.tipAmount = tipAmount;
    }

    /**
     * 이 시간별 배달팁이 주어진 시각을 포함하는지 판정한다.
     *
     * <p>요일 구분이 오늘에 적용되지 않으면 포함하지 않는다({@link DayType#appliesTo}에 위임 — 이 매핑이
     * 여기 복제되면 상수를 추가할 때 두 곳을 함께 고쳐야 한다). 포함 구간은 {@code [startTime, endTime)}
     * 반열림이며, 종료가 시작보다 이르면 자정을 넘기는 구간으로 본다.
     */
    public boolean covers(LocalTime time, DayOfWeek dayOfWeek, boolean publicHoliday) {
        if (startTime == null || endTime == null) {
            return false;
        }
        if (!dayType.appliesTo(dayOfWeek, publicHoliday)) {
            return false;
        }
        if (endTime.isBefore(startTime)) {
            return !time.isBefore(startTime) || time.isBefore(endTime);
        }
        return !time.isBefore(startTime) && time.isBefore(endTime);
    }

    private static void validateSchedule(DayType dayType, LocalTime startTime, LocalTime endTime, int tipAmount) {
        if (dayType == null || dayType == DayType.HOLIDAY) {
            throw new BusinessException(ErrorCode.SHOP_DELIVERY_TIP_SCHEDULE_DAY_TYPE_NOT_ALLOWED);
        }
        if (startTime == null || endTime == null || startTime.equals(endTime)) {
            throw new BusinessException(ErrorCode.SHOP_DELIVERY_TIP_SCHEDULE_OVERLAP,
                "시간별 배달팁의 시작·종료 시각은 필수이며 서로 같을 수 없습니다.");
        }
        if (tipAmount < 0 || tipAmount > DeliveryTipPolicy.EXTRA_TIP_UPPER_BOUND) {
            throw new BusinessException(ErrorCode.SHOP_DELIVERY_TIP_EXTRA_AMOUNT_OUT_OF_RANGE,
                ErrorCode.SHOP_DELIVERY_TIP_EXTRA_AMOUNT_OUT_OF_RANGE.getDefaultMessage() + " 입력: " + tipAmount + "원");
        }
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
        return this.shopId;
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

    public int getTipAmount() {
        return this.tipAmount;
    }
}
