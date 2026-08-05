package com.tastyhouse.infrastructure.reservation.persistence;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationSlotJpaRepository extends JpaRepository<ReservationSlotJpaEntity, Long> {

    // 낙관적 락(@Version)만으로 동시 차감 충돌을 감지하므로 별도 @Lock 은 두지 않는다.
    Optional<ReservationSlotJpaEntity> findByShopIdAndSlotDateAndSlotTime(Long shopId, LocalDate slotDate, LocalTime slotTime);
}
