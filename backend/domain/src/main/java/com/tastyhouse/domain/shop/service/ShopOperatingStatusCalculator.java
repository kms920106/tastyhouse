package com.tastyhouse.domain.shop.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import com.tastyhouse.domain.shared.model.DayType;
import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.shop.model.OrderUnavailableReason;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.model.ShopBreakTime;
import com.tastyhouse.domain.shop.model.ShopBusinessHour;
import com.tastyhouse.domain.shop.model.ShopClosedDay;
import com.tastyhouse.domain.shop.model.ShopSuspension;
import com.tastyhouse.domain.shop.model.ShopTemporaryClosure;
import com.tastyhouse.domain.shop.model.ClosedDayType;

/**
 * 가게 실시간 영업 상태(영업중/준비중)를 계산하는 순수 계산기.
 *
 * <p>리포지토리에 의존하지 않는 순수 로직이라 Spring/DB 없이 단위 테스트할 수 있다.
 * 조회·조립은 {@link ShopOperatingStatusService}가 담당하고, 이 계산기는 넘겨받은 값만으로 판정한다.
 *
 * <p><b>역할은 오케스트레이션 한 가지다</b> — 개별 자식 애그리거트의 판정은 각자가 소유한다:
 * 영업시간 행은 {@link ShopBusinessHour#isOpenAt(LocalTime)}·
 * {@link ShopBusinessHour#extendsIntoNextDayAt(LocalTime)}, 휴게시간은
 * {@link ShopBreakTime#covers(LocalTime, DayOfWeek, boolean)}, 정기휴무는
 * {@link ClosedDayType#matches(java.time.LocalDate)},
 * 임시중지는 {@link ShopSuspension#isActive(LocalDateTime, OrderMethod)}가 판정한다. 이 계산기에 남는 것은
 * <b>여러 애그리거트를 가로지르는 판정</b>(우선순위 순서, 오늘 적용할 영업시간 행 선택)뿐이며, 이는
 * 어느 한 애그리거트에도 속하지 않으므로 도메인 서비스 잔류가 정당하다.
 *
 * <p>판정 우선순위(하나라도 준비중 조건이면 즉시 PREPARING, 그 사유 하나만 반환):
 * <ol>
 *   <li>폐업/노출정지 (방어적 — 목록·상세 조회에서 이미 필터됨)</li>
 *   <li>활성 임시중지({@link ShopSuspension#isActive(LocalDateTime, OrderMethod)}) —
 *       판정 대상 주문유형에 적용되는 건만 본다</li>
 *   <li>공휴일 휴무 (공휴일이고 {@link Shop#isClosedOnPublicHolidays()})</li>
 *   <li>임시휴무 기간 내</li>
 *   <li>정기휴무 요일 매칭</li>
 *   <li>영업시간 외 / 휴무 표시 행</li>
 *   <li>휴게시간 구간 내</li>
 *   <li>그 외 → 영업중</li>
 * </ol>
 *
 * <p>입력은 {@link ShopOperatingStatusContext}로 묶어 받고, 결과는 사유를 동반한
 * {@link ShopOperatingStatusResult}로 돌려준다.
 */
public class ShopOperatingStatusCalculator {

    /**
     * 영업 상태와 그 사유를 판정한다. 첫 번째로 걸린 준비중 조건 하나만 사유로 돌려준다.
     *
     * <p>{@code context.orderMethod()}가 null이면 <b>가게 전체</b> 판정이며, 이때 유형별 임시중지
     * ({@code ShopSuspension#orderMethod != null})는 가게 상태를 멈추지 않는다. 유형을 넘기면 그 유형에
     * 걸린 중지와 전체 대상 중지를 함께 본다.
     */
    public ShopOperatingStatusResult calculate(ShopOperatingStatusContext context) {
        Shop shop = context.shop();
        LocalDateTime now = context.now();
        boolean publicHoliday = context.publicHoliday();

        if (shop.isPermanentlyClosed()) {
            return ShopOperatingStatusResult.preparing(OrderUnavailableReason.PERMANENTLY_CLOSED);
        }

        if (shop.isHidden()) {
            return ShopOperatingStatusResult.preparing(OrderUnavailableReason.HIDDEN);
        }

        if (hasActiveSuspension(context.suspensions(), now, context.orderMethod())) {
            return ShopOperatingStatusResult.preparing(OrderUnavailableReason.SUSPENDED);
        }

        if (publicHoliday && shop.isClosedOnPublicHolidays()) {
            return ShopOperatingStatusResult.preparing(OrderUnavailableReason.PUBLIC_HOLIDAY_CLOSED);
        }

        LocalDate today = now.toLocalDate();
        if (isTemporarilyClosed(context.temporaryClosures(), today)) {
            return ShopOperatingStatusResult.preparing(OrderUnavailableReason.TEMPORARILY_CLOSED);
        }

        if (isRegularClosedDay(context.closedDays(), today)) {
            return ShopOperatingStatusResult.preparing(OrderUnavailableReason.REGULAR_CLOSED_DAY);
        }

        if (!isWithinBusinessHours(context.businessHours(), now, publicHoliday)) {
            return ShopOperatingStatusResult.preparing(OrderUnavailableReason.OUT_OF_BUSINESS_HOURS);
        }

        if (isWithinBreakTime(context.breakTimes(), now, publicHoliday)) {
            return ShopOperatingStatusResult.preparing(OrderUnavailableReason.BREAK_TIME);
        }

        return ShopOperatingStatusResult.open();
    }

    private boolean hasActiveSuspension(List<ShopSuspension> suspensions, LocalDateTime now, OrderMethod orderMethod) {
        return suspensions.stream().anyMatch(suspension -> suspension.isActive(now, orderMethod));
    }

    private boolean isTemporarilyClosed(List<ShopTemporaryClosure> temporaryClosures, LocalDate today) {
        return temporaryClosures.stream()
            .anyMatch(closure -> !today.isBefore(closure.getStartDate()) && !today.isAfter(closure.getEndDate()));
    }

    private boolean isRegularClosedDay(List<ShopClosedDay> closedDays, LocalDate today) {
        return closedDays.stream()
            .anyMatch(closedDay -> closedDay.getClosedDayType().matches(today));
    }

    /**
     * 오늘({@code today})에 적용되는 영업시간 행이 영업 상태인지 판단한다.
     * 영업시간 정보 자체가 없으면 영업중으로 간주한다(정보 미입력을 준비중으로 오판하지 않기 위함).
     * 자정을 넘기는 영업시간(close &lt; open)은 당일 행뿐 아니라 전일 행의 새벽 연장 구간도 확인한다.
     */
    private boolean isWithinBusinessHours(List<ShopBusinessHour> businessHours, LocalDateTime now, boolean publicHoliday) {
        if (businessHours.isEmpty()) {
            return true;
        }

        LocalTime time = now.toLocalTime();

        ShopBusinessHour todayHour = selectApplicableHour(businessHours, now.getDayOfWeek(), publicHoliday);
        boolean openToday = todayHour != null && todayHour.isOpenAt(time);

        // 전일 영업시간이 자정을 넘겨 오늘 새벽까지 이어지는 경우
        LocalDateTime yesterday = now.minusDays(1);
        ShopBusinessHour yesterdayHour = selectApplicableHour(businessHours, yesterday.getDayOfWeek(), false);
        boolean openFromYesterday = yesterdayHour != null && yesterdayHour.extendsIntoNextDayAt(time);

        return openToday || openFromYesterday;
    }

    private boolean isWithinBreakTime(List<ShopBreakTime> breakTimes, LocalDateTime now, boolean publicHoliday) {
        LocalTime time = now.toLocalTime();
        return breakTimes.stream()
            .anyMatch(breakTime -> breakTime.covers(time, now.getDayOfWeek(), publicHoliday));
    }

    /**
     * 주어진 요일에 적용할 영업시간 행을 구체성 우선으로 선택한다:
     * 개별 요일 &gt; 주말/평일 &gt; 공휴일(공휴일일 때) &gt; 매일. 없으면 null.
     *
     * <p><b>{@code public}인 이유</b>: 예약주문 슬롯 계산({@link ScheduledOrderSlotCalculator})이 "오늘의
     * 영업 시작·종료 시각"을 알아야 리드타임 하한과 예약 상한을 구할 수 있는데, 그 선택 규칙(구체성 우선
     * 순위)은 이 계산기가 이미 소유하고 있다. 복제하면 규칙이 두 벌이 되어 요일 구분을 추가할 때 한쪽만
     * 고쳐지므로, 소유자를 그대로 두고 노출만 넓힌다.
     */
    public ShopBusinessHour selectApplicableHour(List<ShopBusinessHour> businessHours, DayOfWeek dayOfWeek, boolean publicHoliday) {
        ShopBusinessHour daily = null;
        ShopBusinessHour holiday = null;
        ShopBusinessHour weekGroup = null;
        for (ShopBusinessHour hour : businessHours) {
            DayType dayType = hour.getDayType();
            if (dayType.isSpecificDay(dayOfWeek)) {
                return hour;
            }
            if (dayType == DayType.HOLIDAY) {
                holiday = hour;
            } else if (dayType == DayType.WEEKEND && dayType.appliesTo(dayOfWeek, publicHoliday)) {
                weekGroup = hour;
            } else if (dayType == DayType.WEEKDAY && dayType.appliesTo(dayOfWeek, publicHoliday)) {
                weekGroup = hour;
            } else if (dayType == DayType.DAILY) {
                daily = hour;
            }
        }
        if (weekGroup != null) {
            return weekGroup;
        }
        if (publicHoliday && holiday != null) {
            return holiday;
        }
        return daily;
    }
}
