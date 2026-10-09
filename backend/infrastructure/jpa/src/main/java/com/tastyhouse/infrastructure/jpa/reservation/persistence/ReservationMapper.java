package com.tastyhouse.infrastructure.jpa.reservation.persistence;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.reservation.model.Reservation;
import com.tastyhouse.domain.reservation.model.ReservationStatus;
import com.tastyhouse.domain.shop.vo.ShopId;

final class ReservationMapper {

    private ReservationMapper() {
    }

    static Reservation toDomain(ReservationJpaEntity entity) {
        return Reservation.reconstitute(
            entity.getId(),
            entity.getMemberId() == null ? null : MemberId.of(entity.getMemberId()),
            entity.getShopId() == null ? null : ShopId.of(entity.getShopId()),
            entity.getReservationDate(),
            entity.getReservationTime(),
            entity.getPartySize(),
            entity.getStatus() == null ? null : ReservationStatus.valueOf(entity.getStatus()),
            entity.getRequest(),
            entity.getCreatedAt()
        );
    }

    static ReservationJpaEntity toEntity(Reservation reservation) {
        return ReservationJpaEntity.create(
            reservation.getMemberId() == null ? null : reservation.getMemberId().value(),
            reservation.getShopId() == null ? null : reservation.getShopId().value(),
            reservation.getReservationDate(),
            reservation.getReservationTime(),
            reservation.getPartySize(),
            reservation.getStatus() == null ? null : reservation.getStatus().name(),
            reservation.getRequest()
        );
    }

    static void applyChanges(ReservationJpaEntity entity, Reservation reservation) {
        entity.applyChanges(reservation.getStatus() == null ? null : reservation.getStatus().name());
    }
}
