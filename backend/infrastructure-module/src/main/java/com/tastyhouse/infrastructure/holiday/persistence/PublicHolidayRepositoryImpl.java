package com.tastyhouse.infrastructure.holiday.persistence;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.holiday.model.PublicHoliday;
import com.tastyhouse.domain.holiday.repository.PublicHolidayRepository;

/**
 * 법정 공휴일 캘린더 조회 어댑터.
 *
 * <p>read-only 마스터라 저장·삭제 경로가 없다 — 캘린더는 {@code insert.sql} 시드가 소유한다.
 */
@Repository
public class PublicHolidayRepositoryImpl implements PublicHolidayRepository {

    private final PublicHolidayJpaRepository publicHolidayJpaRepository;

    public PublicHolidayRepositoryImpl(PublicHolidayJpaRepository publicHolidayJpaRepository) {
        this.publicHolidayJpaRepository = publicHolidayJpaRepository;
    }

    @Override
    public boolean existsByHolidayDate(LocalDate holidayDate) {
        return publicHolidayJpaRepository.existsByHolidayDate(holidayDate);
    }

    @Override
    public List<PublicHoliday> findAllByHolidayDateBetween(LocalDate from, LocalDate to) {
        return publicHolidayJpaRepository.findAllByHolidayDateBetween(from, to).stream()
            .map(PublicHolidayMapper::toDomain)
            .toList();
    }
}
