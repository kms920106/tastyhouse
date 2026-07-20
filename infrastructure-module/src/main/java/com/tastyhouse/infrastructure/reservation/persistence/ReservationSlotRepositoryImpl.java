package com.tastyhouse.infrastructure.reservation.persistence;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.reservation.domain.model.ReservationSlot;
import com.tastyhouse.core.domain.reservation.domain.repository.ReservationSlotRepository;

@Repository
@RequiredArgsConstructor
public class ReservationSlotRepositoryImpl implements ReservationSlotRepository {

    private final ReservationSlotJpaRepository slotJpaRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<ReservationSlot> findByShopAndDateAndTime(Long shopId, LocalDate date, LocalTime time) {
        return slotJpaRepository.findByShopIdAndSlotDateAndSlotTime(shopId, date, time)
            .map(ReservationSlotMapper::toDomain);
    }

    @Override
    public List<ReservationSlot> findByShopAndDate(Long shopId, LocalDate date) {
        return slotJpaRepository.findByShopIdAndSlotDate(shopId, date).stream()
            .map(ReservationSlotMapper::toDomain)
            .toList();
    }

    @Override
    public ReservationSlot save(ReservationSlot slot) {
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
    }

    @Override
    public void saveAndFlush(ReservationSlot slot) {
        save(slot);
        entityManager.flush();
    }
}
