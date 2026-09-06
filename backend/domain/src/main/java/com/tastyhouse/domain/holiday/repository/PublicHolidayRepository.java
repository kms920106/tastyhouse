package com.tastyhouse.domain.holiday.repository;

import java.time.LocalDate;
import java.util.List;

import com.tastyhouse.domain.holiday.model.PublicHoliday;

/**
 * 법정 공휴일 캘린더 조회 포트.
 *
 * <p>read-only 애그리거트라 저장·삭제가 없다(시드 SQL이 소유). 여기 남는 조회는 전부
 * {@code PublicHolidayCalendar}가 배달팁 공휴일 판정에 쓰는 것이므로 write 포트 잔류 기준을 만족한다.
 */
public interface PublicHolidayRepository {

    /** 해당 날짜가 법정 공휴일 캘린더에 있는지. */
    boolean existsByHolidayDate(LocalDate holidayDate);

    /** {@code [from, to]} 구간(양끝 포함)의 공휴일 전체. */
    List<PublicHoliday> findAllByHolidayDateBetween(LocalDate from, LocalDate to);
}
