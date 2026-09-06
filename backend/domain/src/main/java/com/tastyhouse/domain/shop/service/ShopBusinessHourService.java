package com.tastyhouse.domain.shop.service;

import java.time.LocalTime;

import com.tastyhouse.domain.shop.model.ClosedDayType;
import com.tastyhouse.domain.shared.model.DayType;
import com.tastyhouse.domain.shop.model.ShopBreakTime;
import com.tastyhouse.domain.shop.model.ShopBusinessHour;
import com.tastyhouse.domain.shop.model.ShopChangeActionType;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.model.ShopChangeType;
import com.tastyhouse.domain.shop.model.ShopClosedDay;
import com.tastyhouse.domain.shop.repository.ShopDetailRepository;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

public class ShopBusinessHourService {
    private static final int MAX_REGULAR_CLOSED_DAY_COUNT = 15;

    private final ShopDetailRepository shopDetailRepository;
    private final ShopChangeHistoryRecorder shopChangeHistoryRecorder;

    public ShopBusinessHourService(
        ShopDetailRepository shopDetailRepository,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder
    ) {
        this.shopDetailRepository = shopDetailRepository;
        this.shopChangeHistoryRecorder = shopChangeHistoryRecorder;
    }

    public ShopBusinessHour createBusinessHour(
        Long shopId,
        DayType dayType,
        LocalTime openTime,
        LocalTime closeTime,
        Boolean isClosed,
        Boolean is24Hours,
        ShopChangeActor actor
    ) {
        ShopBusinessHour businessHour = ShopBusinessHour.of(
            ShopId.of(shopId), dayType, openTime, closeTime, isClosed, is24Hours
        );
        ShopBusinessHour saved = shopDetailRepository.saveBusinessHour(businessHour);

        shopChangeHistoryRecorder.record(
            saved.getShopId(),
            ShopChangeType.BUSINESS_HOUR,
            ShopChangeActionType.CREATE,
            actor,
            null,
            describeBusinessHour(saved)
        );
        return saved;
    }

    public void updateBusinessHour(
        Long id,
        DayType dayType,
        LocalTime openTime,
        LocalTime closeTime,
        Boolean isClosed,
        Boolean is24Hours,
        ShopChangeActor actor
    ) {
        ShopBusinessHour businessHour = shopDetailRepository.findBusinessHourById(id)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_BUSINESS_HOUR_NOT_FOUND));
        String previousValue = describeBusinessHour(businessHour);

        businessHour.update(dayType, openTime, closeTime, isClosed, is24Hours);
        shopDetailRepository.saveBusinessHour(businessHour);

        shopChangeHistoryRecorder.record(
            businessHour.getShopId(),
            ShopChangeType.BUSINESS_HOUR,
            ShopChangeActionType.UPDATE,
            actor,
            previousValue,
            describeBusinessHour(businessHour)
        );
    }

    public void deleteBusinessHour(Long id, ShopChangeActor actor) {
        ShopBusinessHour businessHour = shopDetailRepository.findBusinessHourById(id).orElse(null);
        if (businessHour == null) {
            return;
        }
        String previousValue = describeBusinessHour(businessHour);

        shopDetailRepository.deleteBusinessHourById(id);

        shopChangeHistoryRecorder.record(
            businessHour.getShopId(),
            ShopChangeType.BUSINESS_HOUR,
            ShopChangeActionType.DELETE,
            actor,
            previousValue,
            null
        );
    }

    public ShopBreakTime createBreakTime(
        Long shopId,
        DayType dayType,
        LocalTime startTime,
        LocalTime endTime,
        ShopChangeActor actor
    ) {
        validateBreakTimeWithinBusinessHours(shopId, dayType, startTime, endTime);
        ShopBreakTime breakTime = ShopBreakTime.of(ShopId.of(shopId), dayType, startTime, endTime);
        ShopBreakTime saved = shopDetailRepository.saveBreakTime(breakTime);

        shopChangeHistoryRecorder.record(
            saved.getShopId(),
            ShopChangeType.BREAK_TIME,
            ShopChangeActionType.CREATE,
            actor,
            null,
            describeBreakTime(saved)
        );
        return saved;
    }

    public void updateBreakTime(
        Long id,
        DayType dayType,
        LocalTime startTime,
        LocalTime endTime,
        ShopChangeActor actor
    ) {
        ShopBreakTime breakTime = shopDetailRepository.findBreakTimeById(id)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_BREAK_TIME_NOT_FOUND));
        validateBreakTimeWithinBusinessHours(breakTime.getShopId().value(), dayType, startTime, endTime);
        String previousValue = describeBreakTime(breakTime);

        breakTime.update(dayType, startTime, endTime);
        shopDetailRepository.saveBreakTime(breakTime);

        shopChangeHistoryRecorder.record(
            breakTime.getShopId(),
            ShopChangeType.BREAK_TIME,
            ShopChangeActionType.UPDATE,
            actor,
            previousValue,
            describeBreakTime(breakTime)
        );
    }

    public void deleteBreakTime(Long id, ShopChangeActor actor) {
        ShopBreakTime breakTime = shopDetailRepository.findBreakTimeById(id).orElse(null);
        if (breakTime == null) {
            return;
        }
        String previousValue = describeBreakTime(breakTime);

        shopDetailRepository.deleteBreakTimeById(id);

        shopChangeHistoryRecorder.record(
            breakTime.getShopId(),
            ShopChangeType.BREAK_TIME,
            ShopChangeActionType.DELETE,
            actor,
            previousValue,
            null
        );
    }

    public ShopClosedDay createClosedDay(Long shopId, ClosedDayType closedDayType, ShopChangeActor actor) {
        if (shopDetailRepository.findClosedDaysByShopId(shopId).size() >= MAX_REGULAR_CLOSED_DAY_COUNT) {
            throw new BusinessException(ErrorCode.SHOP_REGULAR_CLOSED_DAY_LIMIT_EXCEEDED);
        }
        ShopClosedDay closedDay = ShopClosedDay.of(ShopId.of(shopId), closedDayType);
        ShopClosedDay saved = shopDetailRepository.saveClosedDay(closedDay);

        shopChangeHistoryRecorder.record(
            saved.getShopId(),
            ShopChangeType.CLOSED_DAY,
            ShopChangeActionType.CREATE,
            actor,
            null,
            describeClosedDay(saved)
        );
        return saved;
    }

    public void deleteClosedDay(Long id, ShopChangeActor actor) {
        ShopClosedDay closedDay = shopDetailRepository.findClosedDayById(id).orElse(null);
        if (closedDay == null) {
            return;
        }
        String previousValue = describeClosedDay(closedDay);

        shopDetailRepository.deleteClosedDayById(id);

        shopChangeHistoryRecorder.record(
            closedDay.getShopId(),
            ShopChangeType.CLOSED_DAY,
            ShopChangeActionType.DELETE,
            actor,
            previousValue,
            null
        );
    }

    private String describeBusinessHour(ShopBusinessHour businessHour) {
        String dayLabel = businessHour.getDayType().getDescription();
        if (businessHour.isClosed()) {
            return dayLabel + " 휴무";
        }
        if (businessHour.is24Hours()) {
            return dayLabel + " 24시간";
        }
        return dayLabel + " " + ShopChangeValueFormatter.timeRange(businessHour.getOpenTime(), businessHour.getCloseTime());
    }

    private String describeBreakTime(ShopBreakTime breakTime) {
        return breakTime.getDayType().getDescription() + " "
            + ShopChangeValueFormatter.timeRange(breakTime.getStartTime(), breakTime.getEndTime());
    }

    private String describeClosedDay(ShopClosedDay closedDay) {
        return closedDay.getClosedDayType().getDescription();
    }

    private void validateBreakTimeWithinBusinessHours(Long shopId, DayType dayType, LocalTime breakStart, LocalTime breakEnd) {
        if (breakStart == null || breakEnd == null) {
            throw new BusinessException(ErrorCode.SHOP_BREAK_TIME_OUT_OF_BUSINESS_HOURS);
        }
        ShopBusinessHour businessHour = shopDetailRepository.findBusinessHoursByShopId(shopId).stream()
            .filter(bh -> bh.getDayType() == dayType)
            .findFirst()
            .orElseThrow(() -> new BusinessException(ErrorCode.SHOP_BREAK_TIME_OUT_OF_BUSINESS_HOURS));
        if (businessHour.isClosed()) {
            throw new BusinessException(ErrorCode.SHOP_BREAK_TIME_OUT_OF_BUSINESS_HOURS);
        }
        if (businessHour.is24Hours()) {
            return;
        }
        LocalTime open = businessHour.getOpenTime();
        LocalTime close = businessHour.getCloseTime();
        if (open != null && close != null && open.equals(breakStart) && close.equals(breakEnd)) {
            throw new BusinessException(ErrorCode.SHOP_BREAK_TIME_EQUALS_BUSINESS_HOURS);
        }
        if (isOutside(open, close, breakStart) || isOutside(open, close, breakEnd)) {
            throw new BusinessException(ErrorCode.SHOP_BREAK_TIME_OUT_OF_BUSINESS_HOURS);
        }
    }

    private boolean isOutside(LocalTime open, LocalTime close, LocalTime target) {
        if (open == null || close == null) {
            return true;
        }
        if (open.isBefore(close)) {
            return target.isBefore(open) || target.isAfter(close);
        }

        return target.isBefore(open) && target.isAfter(close);
    }
}
