package com.tastyhouse.infrastructure.jpa.reservation.persistence;

import com.tastyhouse.domain.reservation.model.ReservationSlot;
import com.tastyhouse.domain.shop.vo.ShopId;

final class ReservationSlotMapper {

    private ReservationSlotMapper() {
    }

    static ReservationSlot toDomain(ReservationSlotJpaEntity entity) {
        return ReservationSlot.reconstitute(
            entity.getId(),
            entity.getShopId() == null ? null : ShopId.of(entity.getShopId()),
            entity.getSlotDate(),
            entity.getSlotTime(),
            entity.getCapacity(),
            entity.getReservedCount(),
            entity.getVersion()
        );
    }

    static ReservationSlotJpaEntity toEntity(ReservationSlot slot) {
        return ReservationSlotJpaEntity.create(
            slot.getShopId() == null ? null : slot.getShopId().value(),
            slot.getSlotDate(),
            slot.getSlotTime(),
            slot.getCapacity(),
            slot.getReservedCount()
        );
    }

    static void applyChanges(ReservationSlotJpaEntity entity, ReservationSlot slot) {
        entity.applyChanges(slot.getReservedCount());
    }
}
