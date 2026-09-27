package com.tastyhouse.application.holiday.store;

import java.time.LocalDate;
import java.util.List;

import com.tastyhouse.application.holiday.port.out.write.PublicHolidayStatePort;
import com.tastyhouse.domain.holiday.model.PublicHoliday;

public class PublicHolidayStore implements PublicHolidayRepository {
    private final PublicHolidayStatePort publicHolidayStatePort;

    public PublicHolidayStore(PublicHolidayStatePort publicHolidayStatePort) {
        this.publicHolidayStatePort = publicHolidayStatePort;
    }

    @Override
    public boolean existsByHolidayDate(LocalDate holidayDate) {
        return publicHolidayStatePort.existsByHolidayDate(holidayDate);
    }

    @Override
    public List<PublicHoliday> findAllByHolidayDateBetween(LocalDate from, LocalDate to) {
        return publicHolidayStatePort.findAllByHolidayDateBetween(from, to).stream()
            .map(PublicHolidayStateMapper::toDomain)
            .toList();
    }
}
