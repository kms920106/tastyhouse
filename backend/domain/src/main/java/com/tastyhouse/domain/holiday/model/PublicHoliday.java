package com.tastyhouse.domain.holiday.model;

import java.time.LocalDate;

public class PublicHoliday {
    private final Long id;
    private final LocalDate holidayDate;
    private final String name;

    private PublicHoliday(Long id, LocalDate holidayDate, String name) {
        this.id = id;
        this.holidayDate = holidayDate;
        this.name = name;
    }

    public static PublicHoliday reconstitute(Long id, LocalDate holidayDate, String name) {
        return new PublicHoliday(id, holidayDate, name);
    }

    public Long getId() {
        return this.id;
    }

    public LocalDate getHolidayDate() {
        return this.holidayDate;
    }

    public String getName() {
        return this.name;
    }
}
