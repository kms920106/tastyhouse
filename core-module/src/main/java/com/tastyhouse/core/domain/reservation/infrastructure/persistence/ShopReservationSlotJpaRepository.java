package com.tastyhouse.core.domain.reservation.infrastructure.persistence;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tastyhouse.core.domain.reservation.domain.model.ShopReservationSlot;

public interface ShopReservationSlotJpaRepository extends JpaRepository<ShopReservationSlot, Long> {

    // 낙관적 락(@Version)만으로 동시 차감 충돌을 감지하므로 별도 @Lock 은 두지 않는다.
    Optional<ShopReservationSlot> findByShopIdAndSlotDateAndSlotTime(Long shopId, LocalDate slotDate, LocalTime slotTime);

    List<ShopReservationSlot> findByShopIdAndSlotDate(Long shopId, LocalDate slotDate);
}
