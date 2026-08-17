package com.tastyhouse.domain.shop.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.tastyhouse.domain.shop.model.ShopBusinessHour;
import com.tastyhouse.domain.shop.model.ShopClosedDay;

/**
 * "익일 가게 오픈 시간"을 산출하는 순수 계산기 — 품절 기간의 기본값에 쓴다.
 *
 * <p>리포지토리·시계를 갖지 않으므로 입력은 {@link ShopNextOpenTimeContext} 하나로 받는다.
 *
 * <p><b>요일별 영업시간 선택 규칙을 새로 짜지 않는다.</b>
 * {@link ShopOperatingStatusCalculator#selectApplicableHour}를 주입받아 재사용한다 —
 * 개별요일 &gt; 주말/평일 &gt; 공휴일 &gt; 매일의 구체성 우선 규칙을 이미 소유하고 있고 재사용 목적으로
 * {@code public}이다. 복제하면 요일 구분을 추가할 때 한쪽만 고쳐진다.
 *
 * <p><b>오늘 남은 영업시간은 후보가 아니다</b> — 기준이 "익일 가게 오픈 시간"이므로 검사는
 * {@code now + 1일}부터 시작한다. 지금 품절 처리한 것이 몇 시간 뒤 같은 날 풀리면 재료 소진 대응이
 * 되지 않는다.
 *
 * <p><b>{@code null} 폴백은 이 계산기 밖에서 한다</b> — "오픈 시각을 정할 수 없다"는 사실과 "그러면
 * 얼마로 할까"라는 정책은 서로 다른 판단이므로, 계산기가 정책을 삼키지 않게 한다. 호출부
 * ({@code ProductAvailabilityCommandService})가 {@code now + 24시간}으로 채운다.
 */
public class ShopNextOpenTimeCalculator {

    /** 익일부터 최대 며칠까지 영업일을 찾을지 — 품절 기간 상한(7일)과 같은 범위다. */
    private static final int SEARCH_DAYS = 7;

    private final ShopOperatingStatusCalculator shopOperatingStatusCalculator;

    public ShopNextOpenTimeCalculator(ShopOperatingStatusCalculator shopOperatingStatusCalculator) {
        this.shopOperatingStatusCalculator = shopOperatingStatusCalculator;
    }

    /**
     * 다음 영업일의 오픈 시각을 산출한다.
     *
     * <table>
     *   <caption>판정 규칙</caption>
     *   <tr><th>상황</th><th>반환</th></tr>
     *   <tr><td>내일부터 +7일 안에 영업일이 있음</td><td>그 날의 {@code openTime}</td></tr>
     *   <tr><td>해당 일이 정기휴무 또는 휴무 표시 행</td><td>건너뛰고 다음 날 검사</td></tr>
     *   <tr><td>{@code is24Hours}</td><td>오픈 시각이 정의되지 않으므로 건너뜀</td></tr>
     *   <tr><td>영업시간 미등록 / +7일 내 영업일 없음</td><td>{@code null}</td></tr>
     * </table>
     *
     * @return 다음 영업일의 오픈 시각. 산출할 수 없으면 {@code null}(폴백은 호출부의 몫)
     */
    public LocalDateTime calculate(ShopNextOpenTimeContext context) {
        if (context.businessHours().isEmpty()) {
            return null;
        }

        LocalDate today = context.now().toLocalDate();
        for (int offset = 1; offset <= SEARCH_DAYS; offset++) {
            LocalDate candidate = today.plusDays(offset);

            if (isClosedDay(context, candidate)) {
                continue;
            }

            boolean publicHoliday = context.publicHolidays().contains(candidate);
            ShopBusinessHour hour = shopOperatingStatusCalculator.selectApplicableHour(
                context.businessHours(), candidate.getDayOfWeek(), publicHoliday);

            if (hour == null || hour.isClosed()) {
                continue;
            }
            // 24시간 영업은 오픈 시각이라는 개념이 없어 자동해제 기준으로 쓸 수 없다.
            if (hour.is24Hours() || hour.getOpenTime() == null) {
                continue;
            }
            return LocalDateTime.of(candidate, hour.getOpenTime());
        }
        return null;
    }

    private boolean isClosedDay(ShopNextOpenTimeContext context, LocalDate date) {
        for (ShopClosedDay closedDay : context.closedDays()) {
            if (closedDay.getClosedDayType().matches(date)) {
                return true;
            }
        }
        return false;
    }
}
