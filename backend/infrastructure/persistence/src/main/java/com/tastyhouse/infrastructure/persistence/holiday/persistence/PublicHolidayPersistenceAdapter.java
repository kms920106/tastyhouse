package com.tastyhouse.infrastructure.persistence.holiday.persistence;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.holiday.model.PublicHoliday;
import com.tastyhouse.application.holiday.port.out.write.PublicHolidayPersistencePort;

@Repository
public class PublicHolidayPersistenceAdapter implements PublicHolidayPersistencePort {

    private final PublicHolidayJpaRepository publicHolidayJpaRepository;

    public PublicHolidayPersistenceAdapter(PublicHolidayJpaRepository publicHolidayJpaRepository) {
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
