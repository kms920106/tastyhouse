package com.tastyhouse.core.domain.shop.application;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Component;

import com.tastyhouse.core.domain.shop.domain.model.ClosedDayType;
import com.tastyhouse.core.domain.shop.domain.model.DayType;
import com.tastyhouse.core.domain.shop.domain.model.Shop;
import com.tastyhouse.core.domain.shop.domain.model.ShopBreakTime;
import com.tastyhouse.core.domain.shop.domain.model.ShopBusinessHour;
import com.tastyhouse.core.domain.shop.domain.model.ShopClosedDay;
import com.tastyhouse.core.domain.shop.domain.model.ShopOperatingStatus;
import com.tastyhouse.core.domain.shop.domain.model.ShopSuspension;
import com.tastyhouse.core.domain.shop.domain.model.ShopTemporaryClosure;

/**
 * 가게 실시간 영업 상태(영업중/준비중)를 계산하는 순수 계산기.
 *
 * <p>리포지토리에 의존하지 않는 순수 로직이라 Spring/DB 없이 단위 테스트할 수 있다.
 * 조회·조립은 {@link ShopOperatingStatusQueryService}가 담당하고, 이 계산기는 넘겨받은 값만으로 판정한다.
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
@Component
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
            .anyMatch(closedDay -> matchesClosedDay(closedDay.getClosedDayType(), today));
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
        boolean openToday = todayHour != null
            && (Boolean.TRUE.equals(todayHour.getIs24Hours())
                || (!Boolean.TRUE.equals(todayHour.getIsClosed())
                    && todayHour.getOpenTime() != null && todayHour.getCloseTime() != null
                    && isWithinRange(time, todayHour.getOpenTime(), todayHour.getCloseTime())));

        // 전일 영업시간이 자정을 넘겨 오늘 새벽까지 이어지는 경우
        LocalDateTime yesterday = now.minusDays(1);
        ShopBusinessHour yesterdayHour = selectApplicableHour(businessHours, yesterday.getDayOfWeek(), false);
        boolean openFromYesterday = yesterdayHour != null
            && !Boolean.TRUE.equals(yesterdayHour.getIs24Hours())
            && !Boolean.TRUE.equals(yesterdayHour.getIsClosed())
            && yesterdayHour.getOpenTime() != null && yesterdayHour.getCloseTime() != null
            && crossesMidnight(yesterdayHour.getOpenTime(), yesterdayHour.getCloseTime())
            && time.isBefore(yesterdayHour.getCloseTime());

        return openToday || openFromYesterday;
    }

    private boolean isWithinBreakTime(List<ShopBreakTime> breakTimes, LocalDateTime now, boolean publicHoliday) {
        LocalTime time = now.toLocalTime();
        return breakTimes.stream().anyMatch(breakTime -> {
            if (breakTime.getStartTime() == null || breakTime.getEndTime() == null) {
                return false;
            }
            if (!matchesDayType(breakTime.getDayType(), now.getDayOfWeek(), publicHoliday)) {
                return false;
            }
            return isWithinRange(time, breakTime.getStartTime(), breakTime.getEndTime());
        });
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
            if (isSpecificDay(dayType, dayOfWeek)) {
                return hour;
            }
            if (dayType == DayType.HOLIDAY) {
                holiday = hour;
            } else if (dayType == DayType.WEEKEND && isWeekend(dayOfWeek)) {
                weekGroup = hour;
            } else if (dayType == DayType.WEEKDAY && !isWeekend(dayOfWeek)) {
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

    private boolean matchesDayType(DayType dayType, DayOfWeek dayOfWeek, boolean publicHoliday) {
        return switch (dayType) {
            case DAILY -> true;
            case WEEKDAY -> !isWeekend(dayOfWeek);
            case WEEKEND -> isWeekend(dayOfWeek);
            case HOLIDAY -> publicHoliday;
            default -> isSpecificDay(dayType, dayOfWeek);
        };
    }

    private boolean isSpecificDay(DayType dayType, DayOfWeek dayOfWeek) {
        return switch (dayType) {
            case MONDAY -> dayOfWeek == DayOfWeek.MONDAY;
            case TUESDAY -> dayOfWeek == DayOfWeek.TUESDAY;
            case WEDNESDAY -> dayOfWeek == DayOfWeek.WEDNESDAY;
            case THURSDAY -> dayOfWeek == DayOfWeek.THURSDAY;
            case FRIDAY -> dayOfWeek == DayOfWeek.FRIDAY;
            case SATURDAY -> dayOfWeek == DayOfWeek.SATURDAY;
            case SUNDAY -> dayOfWeek == DayOfWeek.SUNDAY;
            default -> false;
        };
    }

    private boolean isWeekend(DayOfWeek dayOfWeek) {
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }

    /**
     * 시각이 [start, end) 구간에 드는지 판단한다. end &lt; start면 자정을 넘기는 구간으로 본다.
     */
    private boolean isWithinRange(LocalTime time, LocalTime start, LocalTime end) {
        if (crossesMidnight(start, end)) {
            return !time.isBefore(start) || time.isBefore(end);
        }
        return !time.isBefore(start) && time.isBefore(end);
    }

    private boolean crossesMidnight(LocalTime start, LocalTime end) {
        return end.isBefore(start);
    }

    /**
     * {@link ClosedDayType}이 오늘 날짜와 매칭되는지 판단한다.
     * 매주 X요일, 매달 N째주 X요일(마지막 주 포함)을 지원한다.
     */
    private boolean matchesClosedDay(ClosedDayType type, LocalDate today) {
        if (type == ClosedDayType.NO_CLOSED_DAYS) {
            return false;
        }

        DayOfWeek targetDay = closedDayTargetDayOfWeek(type);
        if (targetDay == null || today.getDayOfWeek() != targetDay) {
            return false;
        }

        WeekOrdinal ordinal = closedDayWeekOrdinal(type);
        if (ordinal == WeekOrdinal.EVERY) {
            return true;
        }

        int weekOfMonth = ((today.getDayOfMonth() - 1) / 7) + 1;
        boolean isLastWeek = today.plusWeeks(1).getMonthValue() != today.getMonthValue();

        return switch (ordinal) {
            case FIRST -> weekOfMonth == 1;
            case SECOND -> weekOfMonth == 2;
            case THIRD -> weekOfMonth == 3;
            case FOURTH -> weekOfMonth == 4;
            case LAST -> isLastWeek;
            case EVERY -> throw new IllegalStateException("EVERY는 상위 분기에서 이미 처리됨");
        };
    }

    private DayOfWeek closedDayTargetDayOfWeek(ClosedDayType type) {
        String name = type.name();
        if (name.endsWith("MONDAY")) {
            return DayOfWeek.MONDAY;
        }
        if (name.endsWith("TUESDAY")) {
            return DayOfWeek.TUESDAY;
        }
        if (name.endsWith("WEDNESDAY")) {
            return DayOfWeek.WEDNESDAY;
        }
        if (name.endsWith("THURSDAY")) {
            return DayOfWeek.THURSDAY;
        }
        if (name.endsWith("FRIDAY")) {
            return DayOfWeek.FRIDAY;
        }
        if (name.endsWith("SATURDAY")) {
            return DayOfWeek.SATURDAY;
        }
        if (name.endsWith("SUNDAY")) {
            return DayOfWeek.SUNDAY;
        }
        return null;
    }

    private WeekOrdinal closedDayWeekOrdinal(ClosedDayType type) {
        String name = type.name();
        if (name.startsWith("EVERY_WEEK_")) {
            return WeekOrdinal.EVERY;
        }
        if (name.contains("FIRST_WEEK")) {
            return WeekOrdinal.FIRST;
        }
        if (name.contains("SECOND_WEEK")) {
            return WeekOrdinal.SECOND;
        }
        if (name.contains("THIRD_WEEK")) {
            return WeekOrdinal.THIRD;
        }
        if (name.contains("FOURTH_WEEK")) {
            return WeekOrdinal.FOURTH;
        }
        if (name.contains("LAST_WEEK")) {
            return WeekOrdinal.LAST;
        }
        return WeekOrdinal.EVERY;
    }

    private enum WeekOrdinal {
        EVERY, FIRST, SECOND, THIRD, FOURTH, LAST
    }
}
