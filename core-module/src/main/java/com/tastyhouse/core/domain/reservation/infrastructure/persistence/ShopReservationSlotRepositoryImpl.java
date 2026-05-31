package com.tastyhouse.core.domain.reservation.infrastructure.persistence;

import com.tastyhouse.core.domain.reservation.domain.model.ShopReservationSlot;
import com.tastyhouse.core.domain.reservation.domain.repository.ShopReservationSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ShopReservationSlotRepositoryImpl implements ShopReservationSlotRepository {

    private final ShopReservationSlotJpaRepository slotJpaRepository;

    @Override
    public Optional<ShopReservationSlot> findByShopAndDateAndTime(Long shopId, LocalDate date, LocalTime time) {
        return slotJpaRepository.findByShopIdAndSlotDateAndSlotTime(shopId, date, time);
    }

    @Override
    public List<ShopReservationSlot> findByShopAndDate(Long shopId, LocalDate date) {
        return slotJpaRepository.findByShopIdAndSlotDate(shopId, date);
    }

    @Override
    public ShopReservationSlot save(ShopReservationSlot slot) {
        return slotJpaRepository.save(slot);
    }
}
