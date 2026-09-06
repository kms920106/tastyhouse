package com.tastyhouse.infrastructure.reservation.persistence;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationSlotJpaRepository extends JpaRepository<ReservationSlotJpaEntity, Long> {
    Optional<ReservationSlotJpaEntity> findByShopIdAndSlotDateAndSlotTime(Long shopId, LocalDate slotDate, LocalTime slotTime);
}
