package com.tastyhouse.infrastructure.persistence.reservation.persistence;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.reservation.model.ReservationSlot;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.reservation.port.out.write.ReservationSlotPersistencePort;
import com.tastyhouse.application.shared.port.out.OptimisticLockConflictException;

import static com.tastyhouse.infrastructure.persistence.reservation.persistence.QReservationSlotJpaEntity.reservationSlotJpaEntity;

@Repository
class ReservationSlotPersistenceAdapter implements ReservationSlotPersistencePort {

    private final JPAQueryFactory queryFactory;
    private final ReservationSlotJpaRepository slotJpaRepository;

    public ReservationSlotPersistenceAdapter(JPAQueryFactory queryFactory, ReservationSlotJpaRepository slotJpaRepository) {
        this.queryFactory = queryFactory;
        this.slotJpaRepository = slotJpaRepository;
    }

    @Override
    public Optional<ReservationSlot> findByShopAndDateAndTime(ShopId shopId, LocalDate date, LocalTime time) {
        ReservationSlotJpaEntity entity = queryFactory
            .selectFrom(reservationSlotJpaEntity)
            .where(
                reservationSlotJpaEntity.shopId.eq(shopId.value()),
                reservationSlotJpaEntity.slotDate.eq(date),
                reservationSlotJpaEntity.slotTime.eq(time)
            )
            .fetchOne();
        return Optional.ofNullable(entity).map(ReservationSlotMapper::toDomain);
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
            slotJpaRepository.flush();
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new OptimisticLockConflictException("예약 슬롯 낙관적 락 충돌", e);
        }
    }
}
