package com.tastyhouse.domain.holiday.repository;

import java.time.LocalDate;
import java.util.List;

import com.tastyhouse.domain.holiday.model.PublicHoliday;

public interface PublicHolidayRepository {
    boolean existsByHolidayDate(LocalDate holidayDate);

    List<PublicHoliday> findAllByHolidayDateBetween(LocalDate from, LocalDate to);
}
