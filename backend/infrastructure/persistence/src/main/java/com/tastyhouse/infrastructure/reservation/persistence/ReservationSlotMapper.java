package com.tastyhouse.infrastructure.reservation.persistence;

import com.tastyhouse.domain.reservation.model.ReservationSlot;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class ReservationSlotMapper {
    private ReservationSlotMapper() {
    }

    static ReservationSlot toDomain(ReservationSlotJpaEntity entity) {
        return ReservationSlot.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getShopId(), ShopId::of),
            entity.getSlotDate(),
            entity.getSlotTime(),
            entity.getCapacity(),
            entity.getReservedCount(),
            entity.getVersion()
        );
    }

    static ReservationSlotJpaEntity toEntity(ReservationSlot domain) {
        return ReservationSlotJpaEntity.create(
            IdMapping.raw(domain.getShopId(), ShopId::value),
            domain.getSlotDate(),
            domain.getSlotTime(),
            domain.getCapacity(),
            domain.getReservedCount()
        );
    }

    static void applyChanges(ReservationSlotJpaEntity entity, ReservationSlot domain) {
        entity.applyChanges(domain.getReservedCount());
    }
}
