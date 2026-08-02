package com.tastyhouse.domain.reservation.domain.model;

import com.tastyhouse.domain.reservation.model.ReservationStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationStatusTest {

    @Test
    @DisplayName("PENDING·CONFIRMED·COMPLETED는 재예약을 차단한다")
    void isBlocking_trueForPendingConfirmedCompleted() {
        assertThat(ReservationStatus.PENDING.isBlocking()).isTrue();
        assertThat(ReservationStatus.CONFIRMED.isBlocking()).isTrue();
        assertThat(ReservationStatus.COMPLETED.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("취소·거절된 예약은 재예약을 차단하지 않는다")
    void isBlocking_falseForCanceledRejected() {
        assertThat(ReservationStatus.CANCELED.isBlocking()).isFalse();
        assertThat(ReservationStatus.REJECTED.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("blockingStatuses는 isBlocking과 동일한 집합을 반환한다")
    void blockingStatuses_matchesIsBlocking() {
        assertThat(ReservationStatus.blockingStatuses())
            .containsExactlyInAnyOrder(
                ReservationStatus.PENDING,
                ReservationStatus.CONFIRMED,
                ReservationStatus.COMPLETED
            );

        for (ReservationStatus status : ReservationStatus.values()) {
            assertThat(ReservationStatus.blockingStatuses().contains(status))
                .isEqualTo(status.isBlocking());
        }
    }
}
