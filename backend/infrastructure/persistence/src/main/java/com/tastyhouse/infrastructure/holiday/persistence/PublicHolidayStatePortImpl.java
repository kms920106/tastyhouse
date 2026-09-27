package com.tastyhouse.infrastructure.holiday.persistence;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.tastyhouse.application.holiday.port.out.write.PublicHolidayState;
import com.tastyhouse.application.holiday.port.out.write.PublicHolidayStatePort;

@Repository
public class PublicHolidayStatePortImpl implements PublicHolidayStatePort {
    private final PublicHolidayJpaRepository publicHolidayJpaRepository;

    public PublicHolidayStatePortImpl(PublicHolidayJpaRepository publicHolidayJpaRepository) {
        this.publicHolidayJpaRepository = publicHolidayJpaRepository;
    }

    @Override
    public boolean existsByHolidayDate(LocalDate holidayDate) {
        return publicHolidayJpaRepository.existsByHolidayDate(holidayDate);
    }

    @Override
    public List<PublicHolidayState> findAllByHolidayDateBetween(LocalDate from, LocalDate to) {
        return publicHolidayJpaRepository.findAllByHolidayDateBetween(from, to).stream()
            .map(PublicHolidayMapper::toState)
            .toList();
    }
}
