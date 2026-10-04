package com.tastyhouse.application.holiday.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.tastyhouse.domain.holiday.model.PublicHoliday;
import com.tastyhouse.application.holiday.port.out.write.PublicHolidayPersistencePort;

@Service
public class PublicHolidayCalendar {

    private final PublicHolidayPersistencePort publicHolidayPersistencePort;

    public PublicHolidayCalendar(PublicHolidayPersistencePort publicHolidayPersistencePort) {
        this.publicHolidayPersistencePort = publicHolidayPersistencePort;
    }

    public boolean isPublicHoliday(LocalDate date) {
        if (date == null) {
            return false;
        }
        return publicHolidayPersistencePort.existsByHolidayDate(date);
    }

    public Set<LocalDate> findBetween(LocalDate from, LocalDate to) {
        if (from == null || to == null || from.isAfter(to)) {
            return Set.of();
        }
        List<PublicHoliday> holidays = publicHolidayPersistencePort.findAllByHolidayDateBetween(from, to);
        return holidays.stream()
            .map(PublicHoliday::getHolidayDate)
            .collect(Collectors.toUnmodifiableSet());
    }
}
