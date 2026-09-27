package com.tastyhouse.application.holiday.port.out.write;

import java.time.LocalDate;

public record PublicHolidayState(
    Long id,
    LocalDate holidayDate,
    String name
) {
}
