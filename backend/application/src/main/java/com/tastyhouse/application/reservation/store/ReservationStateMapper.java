package com.tastyhouse.application.reservation.store;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.reservation.model.Reservation;
import com.tastyhouse.domain.reservation.model.ReservationStatus;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.reservation.port.out.write.ReservationState;

final class ReservationStateMapper {
    private ReservationStateMapper() {
    }

    static Reservation toDomain(ReservationState state) {
        return Reservation.reconstitute(
            state.id(),
            state.memberId() == null ? null : MemberId.of(state.memberId()),
            state.shopId() == null ? null : ShopId.of(state.shopId()),
            state.reservationDate(),
            state.reservationTime(),
            state.partySize(),
            state.status() == null ? null : ReservationStatus.valueOf(state.status()),
            state.request(),
            state.createdAt()
        );
    }

    static ReservationState toState(Reservation reservation) {
        return new ReservationState(
            reservation.getId(),
            reservation.getMemberId() == null ? null : reservation.getMemberId().value(),
            reservation.getShopId() == null ? null : reservation.getShopId().value(),
            reservation.getReservationDate(),
            reservation.getReservationTime(),
            reservation.getPartySize(),
            reservation.getStatus() == null ? null : reservation.getStatus().name(),
            reservation.getRequest(),
            reservation.getCreatedAt()
        );
    }
}
