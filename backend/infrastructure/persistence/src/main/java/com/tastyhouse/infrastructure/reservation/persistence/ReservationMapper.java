package com.tastyhouse.infrastructure.reservation.persistence;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.reservation.model.Reservation;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class ReservationMapper {
    private ReservationMapper() {
    }

    static Reservation toDomain(ReservationJpaEntity entity) {
        return Reservation.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getMemberId(), MemberId::of),
            IdMapping.vo(entity.getShopId(), ShopId::of),
            entity.getReservationDate(),
            entity.getReservationTime(),
            entity.getPartySize(),
            entity.getStatus(),
            entity.getRequest(),
            entity.getCreatedAt()
        );
    }

    static ReservationJpaEntity toEntity(Reservation domain) {
        return ReservationJpaEntity.create(
            IdMapping.raw(domain.getMemberId(), MemberId::value),
            IdMapping.raw(domain.getShopId(), ShopId::value),
            domain.getReservationDate(),
            domain.getReservationTime(),
            domain.getPartySize(),
            domain.getStatus(),
            domain.getRequest()
        );
    }

    static void applyChanges(ReservationJpaEntity entity, Reservation domain) {
        entity.applyChanges(domain.getStatus());
    }
}
