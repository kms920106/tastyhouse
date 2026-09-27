package com.tastyhouse.infrastructure.holiday.persistence;

import com.tastyhouse.application.holiday.port.out.write.PublicHolidayState;

final class PublicHolidayMapper {
    private PublicHolidayMapper() {
    }

    static PublicHolidayState toState(PublicHolidayJpaEntity entity) {
        return new PublicHolidayState(
            entity.getId(),
            entity.getHolidayDate(),
            entity.getName()
        );
    }
}
