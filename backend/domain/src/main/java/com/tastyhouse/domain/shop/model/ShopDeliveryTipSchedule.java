package com.tastyhouse.domain.shop.model;

import java.time.DayOfWeek;
import java.time.LocalTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.shared.model.DayType;

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

    public void update(DayType dayType, LocalTime startTime, LocalTime endTime, int tipAmount) {
        validateSchedule(dayType, startTime, endTime, tipAmount);

        this.dayType = dayType;
        this.startTime = startTime;
        this.endTime = endTime;
        this.tipAmount = tipAmount;
    }

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
