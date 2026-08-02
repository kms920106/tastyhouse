package com.tastyhouse.domain.shop.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import com.tastyhouse.domain.shop.model.DayType;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.model.ShopBreakTime;
import com.tastyhouse.domain.shop.model.ShopBusinessHour;
import com.tastyhouse.domain.shop.model.ShopClosedDay;
import com.tastyhouse.domain.shop.model.ShopOperatingStatus;
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
 * 임시중지는 {@link ShopSuspension#isActive(LocalDateTime)}가 판정한다. 이 계산기에 남는 것은
 * <b>여러 애그리거트를 가로지르는 판정</b>(우선순위 순서, 오늘 적용할 영업시간 행 선택)뿐이며, 이는
 * 어느 한 애그리거트에도 속하지 않으므로 도메인 서비스 잔류가 정당하다.
 *
 * <p>판정 우선순위(하나라도 준비중 조건이면 즉시 PREPARING):
 * <ol>
 *   <li>폐업/노출정지 (방어적 — 목록·상세 조회에서 이미 필터됨)</li>
 *   <li>활성 임시중지({@link ShopSuspension#isActive(LocalDateTime)})</li>
 *   <li>공휴일 휴무 (공휴일이고 {@link Shop#isClosedOnPublicHolidays()})</li>
 *   <li>임시휴무 기간 내</li>
 *   <li>정기휴무 요일 매칭</li>
 *   <li>영업시간 외 / 휴무 표시 행</li>
 *   <li>휴게시간 구간 내</li>
 *   <li>그 외 → 영업중</li>
 * </ol>
 */
public class ShopOperatingStatusCalculator {

    public ShopOperatingStatus calculate(
        Shop shop,
        List<ShopBusinessHour> businessHours,
        List<ShopBreakTime> breakTimes,
        List<ShopClosedDay> closedDays,
        List<ShopTemporaryClosure> temporaryClosures,
        List<ShopSuspension> suspensions,
        boolean publicHoliday,
        LocalDateTime now
    ) {
        if (shop.isPermanentlyClosed() || shop.isHidden()) {
            return ShopOperatingStatus.PREPARING;
        }

        if (hasActiveSuspension(suspensions, now)) {
            return ShopOperatingStatus.PREPARING;
        }

        if (publicHoliday && shop.isClosedOnPublicHolidays()) {
            return ShopOperatingStatus.PREPARING;
        }

        LocalDate today = now.toLocalDate();
        if (isTemporarilyClosed(temporaryClosures, today)) {
            return ShopOperatingStatus.PREPARING;
        }

        if (isRegularClosedDay(closedDays, today)) {
            return ShopOperatingStatus.PREPARING;
        }

        if (!isWithinBusinessHours(businessHours, now, publicHoliday)) {
            return ShopOperatingStatus.PREPARING;
        }

        if (isWithinBreakTime(breakTimes, now, publicHoliday)) {
            return ShopOperatingStatus.PREPARING;
        }

        return ShopOperatingStatus.OPEN;
    }

    private boolean hasActiveSuspension(List<ShopSuspension> suspensions, LocalDateTime now) {
        return suspensions.stream().anyMatch(suspension -> suspension.isActive(now));
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
     */
    private ShopBusinessHour selectApplicableHour(List<ShopBusinessHour> businessHours, DayOfWeek dayOfWeek, boolean publicHoliday) {
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
