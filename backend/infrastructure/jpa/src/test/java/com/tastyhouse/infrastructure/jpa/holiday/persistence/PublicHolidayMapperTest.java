package com.tastyhouse.infrastructure.jpa.holiday.persistence;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.holiday.model.PublicHoliday;

import static org.assertj.core.api.Assertions.assertThat;

class PublicHolidayMapperTest {

    @Test
    @DisplayName("엔티티 → PublicHoliday 변환 시 id를 포함한 모든 필드가 복원된다")
    void toDomainRestoresAllFields() {
        PublicHolidayJpaEntity entity = new PublicHolidayJpaEntity();
        ReflectionTestUtils.setField(entity, "id", 12L);
        ReflectionTestUtils.setField(entity, "holidayDate", LocalDate.of(2026, 10, 3));
        ReflectionTestUtils.setField(entity, "name", "개천절");

        PublicHoliday restored = PublicHolidayMapper.toDomain(entity);

        assertThat(restored).usingRecursiveComparison()
            .isEqualTo(PublicHoliday.reconstitute(12L, LocalDate.of(2026, 10, 3), "개천절"));
    }
}
