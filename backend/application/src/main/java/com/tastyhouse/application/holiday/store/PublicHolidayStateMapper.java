package com.tastyhouse.application.holiday.store;

import com.tastyhouse.domain.holiday.model.PublicHoliday;
import com.tastyhouse.application.holiday.port.out.write.PublicHolidayState;

final class PublicHolidayStateMapper {
    private PublicHolidayStateMapper() {
    }

    static PublicHoliday toDomain(PublicHolidayState state) {
        return PublicHoliday.reconstitute(
            state.id(),
            state.holidayDate(),
            state.name()
        );
    }

    static PublicHolidayState toState(PublicHoliday publicHoliday) {
        return new PublicHolidayState(
            publicHoliday.getId(),
            publicHoliday.getHolidayDate(),
            publicHoliday.getName()
        );
    }
}
