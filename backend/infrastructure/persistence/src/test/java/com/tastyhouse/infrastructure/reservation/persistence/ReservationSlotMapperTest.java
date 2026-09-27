package com.tastyhouse.infrastructure.reservation.persistence;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.reservation.model.ReservationSlot;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationSlotMapperTest {

    @Test
    @DisplayName("ReservationSlot → 엔티티 변환 시 모든 컬럼 값이 채워진다")
    void toEntity() {
        ReservationSlot slot = ReservationSlot.reconstitute(
            71L, ShopId.of(72L), LocalDate.of(2026, 4, 6), LocalTime.of(19, 0), 10, 3, 5L);

        ReservationSlotJpaEntity entity = ReservationSlotMapper.toEntity(slot);

        assertThat(entity.getShopId()).isEqualTo(72L);
        assertThat(entity.getSlotDate()).isEqualTo(LocalDate.of(2026, 4, 6));
        assertThat(entity.getSlotTime()).isEqualTo(LocalTime.of(19, 0));
        assertThat(entity.getCapacity()).isEqualTo(10);
        assertThat(entity.getReservedCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("엔티티 → ReservationSlot 변환 시 version을 포함한 모든 필드가 복원된다")
    void toDomain() {
        ReservationSlotJpaEntity entity = ReservationSlotJpaEntity.create(
            72L, LocalDate.of(2026, 4, 6), LocalTime.of(19, 0), 10, 3);
        ReflectionTestUtils.setField(entity, "id", 71L);
        ReflectionTestUtils.setField(entity, "version", 5L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.of(2026, 4, 1, 9, 0));
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 4, 1, 9, 30));

        ReservationSlot slot = ReservationSlotMapper.toDomain(entity);

        ReservationSlot expected = ReservationSlot.reconstitute(
            71L, ShopId.of(72L), LocalDate.of(2026, 4, 6), LocalTime.of(19, 0), 10, 3, 5L);
        assertThat(slot).usingRecursiveComparison().isEqualTo(expected);
    }
}
