package com.tastyhouse.application.holiday.store;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.holiday.model.PublicHoliday;

import static org.assertj.core.api.Assertions.assertThat;

class PublicHolidayStateMapperTest {

    @Test
    @DisplayName("PublicHoliday → PublicHolidayState → PublicHoliday 왕복 시 모든 필드가 보존된다")
    void roundTrip() {
        PublicHoliday original = PublicHoliday.reconstitute(12L, LocalDate.of(2026, 10, 3), "개천절");

        PublicHoliday restored = PublicHolidayStateMapper.toDomain(PublicHolidayStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
