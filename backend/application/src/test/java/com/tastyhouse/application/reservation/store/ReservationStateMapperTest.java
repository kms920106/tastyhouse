package com.tastyhouse.application.reservation.store;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.reservation.model.Reservation;
import com.tastyhouse.domain.reservation.model.ReservationSlot;
import com.tastyhouse.domain.reservation.model.ReservationStatus;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationStateMapperTest {

    @Test
    @DisplayName("Reservation → ReservationState → Reservation 왕복 시 모든 필드가 보존된다")
    void reservationRoundTrip() {
        Reservation original = Reservation.reconstitute(
            61L, MemberId.of(62L), ShopId.of(63L),
            LocalDate.of(2026, 4, 5), LocalTime.of(18, 30), 4,
            ReservationStatus.CONFIRMED, "창가 자리",
            LocalDateTime.of(2026, 4, 1, 9, 0));

        Reservation restored = ReservationStateMapper.toDomain(ReservationStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("ReservationSlot → ReservationSlotState → ReservationSlot 왕복 시 version을 포함한 모든 필드가 보존된다")
    void reservationSlotRoundTrip() {
        ReservationSlot original = ReservationSlot.reconstitute(
            71L, ShopId.of(72L), LocalDate.of(2026, 4, 6), LocalTime.of(19, 0), 10, 3, 5L);

        ReservationSlot restored = ReservationSlotStateMapper.toDomain(ReservationSlotStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
