package com.tastyhouse.infrastructure.jpa.reservation.persistence;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.reservation.model.Reservation;
import com.tastyhouse.domain.reservation.model.ReservationStatus;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationMapperTest {

    @Test
    @DisplayName("Reservation → 엔티티 변환 시 enum은 name, VO는 value로 컬럼에 채워진다")
    void toEntity() {
        Reservation reservation = Reservation.reconstitute(
            61L, MemberId.of(62L), ShopId.of(63L),
            LocalDate.of(2026, 4, 5), LocalTime.of(18, 30), 4,
            ReservationStatus.CONFIRMED, "창가 자리",
            LocalDateTime.of(2026, 4, 1, 9, 0));

        ReservationJpaEntity entity = ReservationMapper.toEntity(reservation);

        assertThat(entity.getMemberId()).isEqualTo(62L);
        assertThat(entity.getShopId()).isEqualTo(63L);
        assertThat(entity.getReservationDate()).isEqualTo(LocalDate.of(2026, 4, 5));
        assertThat(entity.getReservationTime()).isEqualTo(LocalTime.of(18, 30));
        assertThat(entity.getPartySize()).isEqualTo(4);
        assertThat(entity.getStatus()).isEqualTo("CONFIRMED");
        assertThat(entity.getRequest()).isEqualTo("창가 자리");
    }

    @Test
    @DisplayName("엔티티 → Reservation 변환 시 id·생성 시각을 포함한 모든 필드가 복원된다")
    void toDomain() {
        ReservationJpaEntity entity = ReservationJpaEntity.create(
            62L, 63L, LocalDate.of(2026, 4, 5), LocalTime.of(18, 30), 4, "CONFIRMED", "창가 자리");
        ReflectionTestUtils.setField(entity, "id", 61L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.of(2026, 4, 1, 9, 0));
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 4, 1, 9, 30));

        Reservation reservation = ReservationMapper.toDomain(entity);

        Reservation expected = Reservation.reconstitute(
            61L, MemberId.of(62L), ShopId.of(63L),
            LocalDate.of(2026, 4, 5), LocalTime.of(18, 30), 4,
            ReservationStatus.CONFIRMED, "창가 자리",
            LocalDateTime.of(2026, 4, 1, 9, 0));
        assertThat(reservation).usingRecursiveComparison().isEqualTo(expected);
    }
}
