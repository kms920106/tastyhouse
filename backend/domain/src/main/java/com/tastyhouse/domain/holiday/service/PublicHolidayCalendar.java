package com.tastyhouse.domain.holiday.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.tastyhouse.domain.holiday.model.PublicHoliday;
import com.tastyhouse.domain.holiday.repository.PublicHolidayRepository;

public class PublicHolidayCalendar {
    private final PublicHolidayRepository publicHolidayRepository;

    public PublicHolidayCalendar(PublicHolidayRepository publicHolidayRepository) {
        this.publicHolidayRepository = publicHolidayRepository;
    }

    public boolean isPublicHoliday(LocalDate date) {
        if (date == null) {
            return false;
        }
        return publicHolidayRepository.existsByHolidayDate(date);
    }

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
