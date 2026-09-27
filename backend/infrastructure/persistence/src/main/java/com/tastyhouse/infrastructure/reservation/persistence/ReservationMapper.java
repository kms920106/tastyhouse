package com.tastyhouse.infrastructure.reservation.persistence;

import com.tastyhouse.application.reservation.port.out.write.ReservationState;

final class ReservationMapper {
    private ReservationMapper() {
    }

    static ReservationState toState(ReservationJpaEntity entity) {
        return new ReservationState(
            entity.getId(),
            entity.getMemberId(),
            entity.getShopId(),
            entity.getReservationDate(),
            entity.getReservationTime(),
            entity.getPartySize(),
            entity.getStatus(),
            entity.getRequest(),
            entity.getCreatedAt()
        );
    }

    static ReservationJpaEntity toEntity(ReservationState state) {
        return ReservationJpaEntity.create(
            state.memberId(),
            state.shopId(),
            state.reservationDate(),
            state.reservationTime(),
            state.partySize(),
            state.status(),
            state.request()
        );
    }

    static void applyChanges(ReservationJpaEntity entity, ReservationState state) {
        entity.applyChanges(state.status());
    }
}
