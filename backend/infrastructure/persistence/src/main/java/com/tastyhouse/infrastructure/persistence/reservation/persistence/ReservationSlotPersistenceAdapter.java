package com.tastyhouse.infrastructure.persistence.reservation.persistence;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.reservation.model.ReservationSlot;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.reservation.port.out.write.ReservationSlotPersistencePort;
import com.tastyhouse.application.shared.port.out.OptimisticLockConflictException;

@Repository
public class ReservationSlotPersistenceAdapter implements ReservationSlotPersistencePort {

    private final ReservationSlotJpaRepository slotJpaRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public ReservationSlotPersistenceAdapter(ReservationSlotJpaRepository slotJpaRepository) {
        this.slotJpaRepository = slotJpaRepository;
    }

    @Override
    public Optional<ReservationSlot> findByShopAndDateAndTime(ShopId shopId, LocalDate date, LocalTime time) {
        return slotJpaRepository.findByShopIdAndSlotDateAndSlotTime(shopId.value(), date, time)
            .map(ReservationSlotMapper::toDomain);
    }

    @Override
    public ReservationSlot save(ReservationSlot slot) {
        try {
            if (slot.getId() == null) {
                ReservationSlotJpaEntity saved = slotJpaRepository.save(ReservationSlotMapper.toEntity(slot));
                return ReservationSlotMapper.toDomain(saved);
            }

            ReservationSlotJpaEntity entity = slotJpaRepository.findById(slot.getId())
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 예약 슬롯입니다: " + slot.getId()));
            ReservationSlotMapper.applyChanges(entity, slot);
            return ReservationSlotMapper.toDomain(entity);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new OptimisticLockConflictException("예약 슬롯 낙관적 락 충돌", e);
        }
    }

    @Override
    public void saveAndFlush(ReservationSlot slot) {
        try {
            save(slot);
            entityManager.flush();
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new OptimisticLockConflictException("예약 슬롯 낙관적 락 충돌", e);
        }
    }
}
