package com.tastyhouse.core.domain.shop.domain.service;

import java.time.LocalTime;

import com.tastyhouse.core.domain.shop.domain.model.ClosedDayType;
import com.tastyhouse.core.domain.shop.domain.model.DayType;
import com.tastyhouse.core.domain.shop.domain.model.ShopBreakTime;
import com.tastyhouse.core.domain.shop.domain.model.ShopBusinessHour;
import com.tastyhouse.core.domain.shop.domain.model.ShopClosedDay;
import com.tastyhouse.core.domain.shop.domain.repository.ShopDetailRepository;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;

/**
 * 가게 영업시간·휴게시간·정기휴무 규격 불변식(도메인 서비스).
 *
 * <p>영업시간은 PDF 규격(5분 단위, 최소 1시간~최대 23시간 55분)을 만족해야 하고, <b>휴게시간은 같은
 * 요일 영업시간 범위 안</b>이어야 하며(다른 애그리거트인 영업시간을 읽어 검증), 정기휴무는 가게당
 * 최대 {@value #MAX_REGULAR_CLOSED_DAY_COUNT}건까지만 등록할 수 있다. 이 규칙들은 등록 액터(점주·관리자)가
 * 달라도 동일해야 하므로 도메인 계층에 둔다(분류 C/D — 휴게시간 검증은 영업시간 애그리거트를 함께 읽는
 * 크로스 애그리거트 규칙, 영업시간 규격 검증은 무상태 정책).
 *
 * <p>{@code @Service}/{@code @Transactional} 없는 순수 POJO이며(공통 지침 패턴 1), 빈 등록은
 * infrastructure-module의 {@code DomainServiceConfig}가 담당한다. 트랜잭션 경계는 이 서비스를 호출하는
 * 소비 모듈의 command 서비스가 선언한다.
 */
public class ShopBusinessHourService {

    private static final int MAX_REGULAR_CLOSED_DAY_COUNT = 15;

    private final ShopDetailRepository shopDetailRepository;

    public ShopBusinessHourService(ShopDetailRepository shopDetailRepository) {
        this.shopDetailRepository = shopDetailRepository;
    }

    public ShopBusinessHour createBusinessHour(
        Long shopId,
        DayType dayType,
        LocalTime openTime,
        LocalTime closeTime,
        Boolean isClosed,
        Boolean is24Hours
    ) {
        validateBusinessHour(openTime, closeTime, isClosed, is24Hours);
        ShopBusinessHour businessHour = ShopBusinessHour.of(shopId, dayType, openTime, closeTime, isClosed, is24Hours);
        return shopDetailRepository.saveBusinessHour(businessHour);
    }

    public void updateBusinessHour(
        Long id,
        DayType dayType,
        LocalTime openTime,
        LocalTime closeTime,
        Boolean isClosed,
        Boolean is24Hours
    ) {
        validateBusinessHour(openTime, closeTime, isClosed, is24Hours);
        ShopBusinessHour businessHour = shopDetailRepository.findBusinessHourById(id)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.SHOP_BUSINESS_HOUR_NOT_FOUND));
        businessHour.update(dayType, openTime, closeTime, isClosed, is24Hours);
        shopDetailRepository.saveBusinessHour(businessHour);
    }

    public void deleteBusinessHour(Long id) {
        shopDetailRepository.deleteBusinessHourById(id);
    }

    public ShopBreakTime createBreakTime(Long shopId, DayType dayType, LocalTime startTime, LocalTime endTime) {
        validateBreakTimeWithinBusinessHours(shopId, dayType, startTime, endTime);
        ShopBreakTime breakTime = ShopBreakTime.of(shopId, dayType, startTime, endTime);
        return shopDetailRepository.saveBreakTime(breakTime);
    }

    public void updateBreakTime(Long id, DayType dayType, LocalTime startTime, LocalTime endTime) {
        ShopBreakTime breakTime = shopDetailRepository.findBreakTimeById(id)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.SHOP_BREAK_TIME_NOT_FOUND));
        validateBreakTimeWithinBusinessHours(breakTime.getShopId(), dayType, startTime, endTime);
        breakTime.update(dayType, startTime, endTime);
        shopDetailRepository.saveBreakTime(breakTime);
    }

    public void deleteBreakTime(Long id) {
        shopDetailRepository.deleteBreakTimeById(id);
    }

    /**
     * 정기휴무를 등록한다. 가게당 최대 {@value #MAX_REGULAR_CLOSED_DAY_COUNT}건을 넘을 수 없다.
     */
    public ShopClosedDay createClosedDay(Long shopId, ClosedDayType closedDayType) {
        if (shopDetailRepository.findClosedDaysByShopId(shopId).size() >= MAX_REGULAR_CLOSED_DAY_COUNT) {
            throw new BusinessException(ErrorCode.SHOP_REGULAR_CLOSED_DAY_LIMIT_EXCEEDED);
        }
        ShopClosedDay closedDay = ShopClosedDay.of(shopId, closedDayType);
        return shopDetailRepository.saveClosedDay(closedDay);
    }

    public void deleteClosedDay(Long id) {
        shopDetailRepository.deleteClosedDayById(id);
    }

    /**
     * 영업시간 PDF 규격을 검증한다: 휴무/24시간이면 시간 검증 생략, 그 외에는 5분 단위·최소 1시간~최대 23시간 55분.
     * 자정 넘김(종료 &lt; 시작)은 허용하며 다음날로 넘어간 것으로 계산한다.
     */
    private void validateBusinessHour(LocalTime openTime, LocalTime closeTime, Boolean isClosed, Boolean is24Hours) {
        if (Boolean.TRUE.equals(isClosed) || Boolean.TRUE.equals(is24Hours)) {
            return;
        }
        if (openTime == null || closeTime == null) {
            throw new BusinessException(ErrorCode.SHOP_BUSINESS_HOUR_INVALID_RANGE);
        }
        if (isNotFiveMinuteUnit(openTime) || isNotFiveMinuteUnit(closeTime)) {
            throw new BusinessException(ErrorCode.SHOP_BUSINESS_HOUR_INVALID_UNIT);
        }
        long durationMinutes = minutesBetween(openTime, closeTime);
        if (durationMinutes < 60 || durationMinutes > 23 * 60 + 55) {
            throw new BusinessException(ErrorCode.SHOP_BUSINESS_HOUR_INVALID_RANGE);
        }
    }

    /**
     * 휴게시간이 같은 요일 영업시간 범위 안에 있는지 검증한다(자정 넘김 반영). 영업시간과 완전히 동일하면 거부한다.
     */
    private void validateBreakTimeWithinBusinessHours(Long shopId, DayType dayType, LocalTime breakStart, LocalTime breakEnd) {
        if (breakStart == null || breakEnd == null) {
            throw new BusinessException(ErrorCode.SHOP_BREAK_TIME_OUT_OF_BUSINESS_HOURS);
        }
        ShopBusinessHour businessHour = shopDetailRepository.findBusinessHoursByShopId(shopId).stream()
            .filter(bh -> bh.getDayType() == dayType)
            .findFirst()
            .orElseThrow(() -> new BusinessException(ErrorCode.SHOP_BREAK_TIME_OUT_OF_BUSINESS_HOURS));
        if (Boolean.TRUE.equals(businessHour.getIsClosed())) {
            throw new BusinessException(ErrorCode.SHOP_BREAK_TIME_OUT_OF_BUSINESS_HOURS);
        }
        if (Boolean.TRUE.equals(businessHour.getIs24Hours())) {
            return; // 24시간 영업이면 어떤 휴게시간도 범위 내
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

    private boolean isNotFiveMinuteUnit(LocalTime time) {
        return time.getMinute() % 5 != 0 || time.getSecond() != 0 || time.getNano() != 0;
    }

    /**
     * open→close 경과 분. 자정 넘김(close ≤ open)이면 다음날로 넘어간 것으로 24시간을 더해 계산한다.
     */
    private long minutesBetween(LocalTime open, LocalTime close) {
        int openMin = open.getHour() * 60 + open.getMinute();
        int closeMin = close.getHour() * 60 + close.getMinute();
        int diff = closeMin - openMin;
        if (diff <= 0) {
            diff += 24 * 60;
        }
        return diff;
    }

    /**
     * target이 [open, close] 영업 구간 밖에 있는지 판정한다(자정 넘김 구간이면 두 조각으로 나눠 판정).
     */
    private boolean isOutside(LocalTime open, LocalTime close, LocalTime target) {
        if (open == null || close == null) {
            return true;
        }
        if (open.isBefore(close)) {
            return target.isBefore(open) || target.isAfter(close);
        }
        // 자정 넘김: open~24:00 또는 00:00~close
        return target.isBefore(open) && target.isAfter(close);
    }
}
