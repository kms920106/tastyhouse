package com.tastyhouse.infrastructure.reservation.persistence;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.reservation.model.ReservationSlot;
import com.tastyhouse.domain.reservation.repository.ReservationSlotRepository;
import com.tastyhouse.domain.shared.exception.OptimisticLockConflictException;
import com.tastyhouse.domain.shop.vo.ShopId;

@Repository
public class ReservationSlotRepositoryImpl implements ReservationSlotRepository {

    private final ReservationSlotJpaRepository slotJpaRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public ReservationSlotRepositoryImpl(ReservationSlotJpaRepository slotJpaRepository) {
        this.slotJpaRepository = slotJpaRepository;
    }

    @Override
    public Optional<ReservationSlot> findByShopAndDateAndTime(ShopId shopId, LocalDate date, LocalTime time) {
        return slotJpaRepository.findByShopIdAndSlotDateAndSlotTime(shopId, date, time)
            .map(ReservationSlotMapper::toDomain);
    }

    @Override
    public ReservationSlot save(ReservationSlot slot) {
        try {
            if (slot.getId() == null) {
                ReservationSlotJpaEntity saved = slotJpaRepository.save(ReservationSlotMapper.toEntity(slot));
                return ReservationSlotMapper.toDomain(saved);
            }

            // update 경로: managed 엔티티를 PK로 조회한 뒤 변경 필드만 복사해 dirty checking으로 flush한다.
            // managed 엔티티의 @Version이 flush 시 검증·증가되어 낙관적 락 동작을 그대로 보존한다.
            // detached merge는 @CreatedDate(updatable=false) 감사 필드 파손 위험이 있어 쓰지 않는다.
            ReservationSlotJpaEntity entity = slotJpaRepository.findById(slot.getId())
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 예약 슬롯입니다: " + slot.getId()));
            ReservationSlotMapper.applyChanges(entity, slot);
            return ReservationSlotMapper.toDomain(entity);
        } catch (ObjectOptimisticLockingFailureException e) {
            // core의 재시도 판별이 spring-orm 예외에 의존하지 않도록 프레임워크-프리 예외로 번역한다.
            throw new OptimisticLockConflictException("예약 슬롯 낙관적 락 충돌", e);
        }
    }

    @Override
    public void saveAndFlush(ReservationSlot slot) {
        // save(...)의 dirty checking 변경은 이 명시적 flush 시점에 @Version이 검증되므로
        // 낙관적 락 충돌도 여기서 발생한다. save·flush를 함께 감싸 번역한다.
        try {
            save(slot);
            entityManager.flush();
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new OptimisticLockConflictException("예약 슬롯 낙관적 락 충돌", e);
        }
    }
}
