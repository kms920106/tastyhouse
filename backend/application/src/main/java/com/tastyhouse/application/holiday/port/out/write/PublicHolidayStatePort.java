package com.tastyhouse.application.holiday.port.out.write;

import java.time.LocalDate;
import java.util.List;

public interface PublicHolidayStatePort {
    boolean existsByHolidayDate(LocalDate holidayDate);

    List<PublicHolidayState> findAllByHolidayDateBetween(LocalDate from, LocalDate to);
}
