package com.tastyhouse.infrastructure.jpa.reservation.persistence;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import com.tastyhouse.domain.reservation.model.ReservationSlot;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shared.port.out.OptimisticLockConflictException;
import com.tastyhouse.application.shared.port.out.UniqueConstraintConflictException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReservationSlotPersistenceAdapterTest {

    private final ReservationSlotJpaRepository slotJpaRepository = mock(ReservationSlotJpaRepository.class);

    private final ReservationSlotPersistenceAdapter adapter = new ReservationSlotPersistenceAdapter(null, slotJpaRepository);

    @Test
    @DisplayName("새 슬롯 insert의 유니크 제약 위반은 포트 예외 UniqueConstraintConflictException으로 번역된다")
    void translatesUniqueViolation() {
        DataIntegrityViolationException cause = new DataIntegrityViolationException("uk_reservation_slot");
        when(slotJpaRepository.save(any())).thenThrow(cause);

        assertThatThrownBy(() -> adapter.saveImmediately(newSlot()))
            .isInstanceOf(UniqueConstraintConflictException.class)
            .hasCause(cause);
    }

    @Test
    @DisplayName("낙관적 락 충돌은 포트 예외 OptimisticLockConflictException으로 번역된다")
    void translatesOptimisticLockConflict() {
        when(slotJpaRepository.save(any()))
            .thenThrow(new ObjectOptimisticLockingFailureException(ReservationSlotJpaEntity.class, 1L));

        assertThatThrownBy(() -> adapter.saveImmediately(newSlot()))
            .isInstanceOf(OptimisticLockConflictException.class);
    }

    private static ReservationSlot newSlot() {
        return ReservationSlot.of(ShopId.of(1L), LocalDate.of(2026, 10, 10), LocalTime.of(12, 0), 4);
    }
}
