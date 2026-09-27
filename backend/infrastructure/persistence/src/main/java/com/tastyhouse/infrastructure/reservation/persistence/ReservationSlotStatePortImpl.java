package com.tastyhouse.infrastructure.reservation.persistence;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Repository;

import com.tastyhouse.application.reservation.port.out.write.ReservationSlotState;
import com.tastyhouse.application.reservation.port.out.write.ReservationSlotStatePort;
import com.tastyhouse.application.shared.port.out.OptimisticLockConflictException;

@Repository
public class ReservationSlotStatePortImpl implements ReservationSlotStatePort {
    private final ReservationSlotJpaRepository slotJpaRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public ReservationSlotStatePortImpl(ReservationSlotJpaRepository slotJpaRepository) {
        this.slotJpaRepository = slotJpaRepository;
    }

    @Override
    public Optional<ReservationSlotState> findByShopAndDateAndTime(Long shopId, LocalDate date, LocalTime time) {
        return slotJpaRepository.findByShopIdAndSlotDateAndSlotTime(shopId, date, time)
            .map(ReservationSlotMapper::toState);
    }

    @Override
    public ReservationSlotState save(ReservationSlotState state) {
        try {
            if (state.id() == null) {
                ReservationSlotJpaEntity saved = slotJpaRepository.save(ReservationSlotMapper.toEntity(state));
                return ReservationSlotMapper.toState(saved);
            }

            ReservationSlotJpaEntity entity = slotJpaRepository.findById(state.id())
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 예약 슬롯입니다: " + state.id()));
            ReservationSlotMapper.applyChanges(entity, state);
            return ReservationSlotMapper.toState(entity);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new OptimisticLockConflictException("예약 슬롯 낙관적 락 충돌", e);
        }
    }

    @Override
    public void saveAndFlush(ReservationSlotState state) {
        try {
            save(state);
            entityManager.flush();
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new OptimisticLockConflictException("예약 슬롯 낙관적 락 충돌", e);
        }
    }
}
