package com.tastyhouse.domain.shop.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import com.tastyhouse.domain.shop.model.ShopBusinessHour;
import com.tastyhouse.domain.shop.model.ShopClosedDay;

/**
 * {@link ShopNextOpenTimeCalculator}의 입력.
 *
 * <p>리포지토리·시계를 갖지 않는 순수 계산기이므로 입력을 record 하나로 묶어 받는다
 * (리포지토리 없는 순수 계산기의 입력 규칙).
 *
 * <p><b>공휴일은 이미 해석된 값으로 받는다</b> — 계산기가 {@code PublicHolidayCalendar}를 주입받으면
 * 순수 함수가 아니게 되고 Spring·DB 없이 단위 테스트할 수 없다. 조회 구간(오늘~+7일)의 공휴일 집합을
 * 호출부가 {@code PublicHolidayCalendar#findBetween}으로 해석해 넘긴다
 * ({@code ShopOperatingStatusContext}가 {@code boolean publicHoliday}를 받는 것과 같은 형태).
 *
 * @param now            기준 시각
 * @param businessHours  가게의 영업시간 행 전체
 * @param closedDays     가게의 정기휴무 전체
 * @param publicHolidays 조회 구간(오늘~+7일)의 공휴일. 호출부가 해석해 넘긴다
 */
public record ShopNextOpenTimeContext(
    LocalDateTime now,
    List<ShopBusinessHour> businessHours,
    List<ShopClosedDay> closedDays,
    Set<LocalDate> publicHolidays
) {

    public ShopNextOpenTimeContext {
        businessHours = businessHours != null ? List.copyOf(businessHours) : List.of();
        closedDays = closedDays != null ? List.copyOf(closedDays) : List.of();
        publicHolidays = publicHolidays != null ? Set.copyOf(publicHolidays) : Set.of();
    }

    public static ShopNextOpenTimeContext of(
        LocalDateTime now,
        List<ShopBusinessHour> businessHours,
        List<ShopClosedDay> closedDays,
        Set<LocalDate> publicHolidays
    ) {
        return new ShopNextOpenTimeContext(
            now,
            businessHours,
            closedDays,
            publicHolidays
        );
    }
}
