package com.tastyhouse.infrastructure.holiday.persistence;

import com.tastyhouse.domain.holiday.model.PublicHoliday;

final class PublicHolidayMapper {

    private PublicHolidayMapper() {
    }

    static PublicHoliday toDomain(PublicHolidayJpaEntity entity) {
        return PublicHoliday.reconstitute(
            entity.getId(),
            entity.getHolidayDate(),
            entity.getName(),
            entity.isSubstitute()
        );
    }
}
