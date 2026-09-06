package com.tastyhouse.domain.holiday.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.tastyhouse.domain.holiday.model.PublicHoliday;
import com.tastyhouse.domain.holiday.repository.PublicHolidayRepository;

/**
 * 법정 공휴일 판정 도메인 서비스.
 *
 * <p>"오늘이 공휴일인가"라는 질문 하나를 캘린더 테이블에 위임해 답한다. 배달팁 계산기
 * ({@code ShopDeliveryTipCalculator})는 순수 함수라 이 판정 결과를 <b>이미 해석된 boolean</b>으로
 * 받으며, 그 변환을 수행하는 것이 이 서비스의 역할이다.
 *
 * <p><b>영업상태 판정({@code ShopOperatingStatusService})에는 아직 연결하지 않는다.</b>
 * 그쪽의 {@code PUBLIC_HOLIDAY = false} 고정을 걷어내는 순간 지금까지 저장만 되고 동작하지 않던
 * {@code Shop.closedOnPublicHolidays}와 {@code DayType.HOLIDAY} 영업시간 행이 한꺼번에 살아나
 * 명절에 영업하는 가게가 대량으로 숨겨질 수 있다. 폭발 반경 격리를 위해 영업상태 연동은 독립 PR로
 * 분리한다.
 *
 * <p>일요일 처리 규칙은 {@link PublicHoliday} Javadoc 참고 — 캘린더가 일요일 자체를 담지 않는다는
 * 데이터 규칙 하나로 처리되며, 이 서비스에는 요일 분기가 없다.
 *
 * <p>{@code @Service}/{@code @Transactional} 없는 순수 POJO이며, 빈 등록은 infrastructure-module의
 * {@code HolidayDomainConfig}가 담당한다.
 */
public class PublicHolidayCalendar {

    private final PublicHolidayRepository publicHolidayRepository;

    public PublicHolidayCalendar(PublicHolidayRepository publicHolidayRepository) {
        this.publicHolidayRepository = publicHolidayRepository;
    }

    /**
     * 주어진 날짜가 법정 공휴일인지 판정한다.
     *
     * <p>평범한 일요일은 캘린더에 없어 {@code false}, 법정공휴일과 겹친 일요일은 캘린더에 있어
     * {@code true}가 된다 — 요일 분기 없이 데이터만으로 두 규칙이 함께 성립한다.
     */
    public boolean isPublicHoliday(LocalDate date) {
        if (date == null) {
            return false;
        }
        return publicHolidayRepository.existsByHolidayDate(date);
    }

    /**
     * {@code [from, to]} 구간(양끝 포함)의 공휴일 날짜 집합. 달력 화면처럼 여러 날을 한 번에 판정할 때
     * 날짜마다 쿼리하지 않도록 한 번에 읽어 온다.
     */
    public Set<LocalDate> findBetween(LocalDate from, LocalDate to) {
        if (from == null || to == null || from.isAfter(to)) {
            return Set.of();
        }
        List<PublicHoliday> holidays = publicHolidayRepository.findAllByHolidayDateBetween(from, to);
        return holidays.stream()
            .map(PublicHoliday::getHolidayDate)
            .collect(Collectors.toUnmodifiableSet());
    }
}
